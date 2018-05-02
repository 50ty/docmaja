package com.docmala.plugins.ouput;

import com.docmala.parser.Block;
import com.docmala.parser.Document;
import com.docmala.parser.FormattedText;
import com.docmala.parser.blocks.*;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Html {

    HtmlDocument _html;
    Map<String, Integer> captionNumbers = new HashMap<>();
    String _css;
    String _js;

    public Html() {
        _css = getResourceFileAsString("codeHighlight.css");
        _js = getResourceFileAsString("codeHighlight.js");
        _js = _js.replaceAll("<script", "&lt;script").replaceAll("</script", "&lt;/script");
    }

    public String getResourceFileAsString(String resourceFileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    public HtmlDocument generate(Document document) {
        _html = new HtmlDocument();
        _html.head().append("<style>\n");
        _html.head().append(_css);
        _html.head().append("\n</style>\n");

        _html.head().append("<script>\n");
        _html.head().append(_js);
        _html.head().append("\n</script>\n");
        _html.head().append("\n<script>hljs.initHighlightingOnLoad();</script>\n");

        _html.body().append("<p>");
        for (Block part : document.content()) {
            generateBlock(part, document);
        }
        _html.body().append("</p>");
        return _html;
    }

    private String id(Block b) {
        return " id=\"line_" + String.valueOf(b.start.line()) + "\"";
    }

    void generateBlock(Block block, Document document) {
        if (block == null)
            return;

        if (block instanceof Headline) {
            generateHeadline((Headline) block, document);
        } else if (block instanceof List) {
            generateList((List) block, document);
        } else if (block instanceof Table) {
            generateTable((Table) block, document);
        } else if (block instanceof Image) {
            generateImage((Image) block, document);
        } else if (block instanceof Content) {
            generateContent((Content) block);
        } else if (block instanceof Code) {
            generateCode((Code) block, document);
        } else if (block instanceof NextParagraph) {
            _html.body().append("\n<p>");
        }
    }

    private void generateCaption(Caption caption, Document document) {
        if (caption != null) {
            _html.body().append("<figcaption>");
            Document.CaptionTypeData captionTypeData = document.captionTypeData().get(caption.type);
            if (!captionNumbers.containsKey(caption.type)) {
                captionNumbers.put(caption.type, 1);
            }
            Integer number = captionNumbers.get(caption.type);

            captionNumbers.put(caption.type, number + 1);

            String format = caption.type + " %s: ";
            if (captionTypeData != null) {
                format = captionTypeData.text;
            }
            _html.body().append(String.format(format, number.toString()));

            generateBlock(caption.content, document);

            _html.body().append("</figcaption>");
        }

    }

    private void generateImage(Image image, Document document) {
        _html.body().append("<figure>");
        _html.body().append("<img src=\"data:image/");
        _html.body().append(image.fileType);
        _html.body().append(";base64,");
        _html.body().append(Base64.getEncoder().encodeToString(image.data));
        _html.body().append("\">");

        generateCaption(image.caption, document);

        _html.body().append("</figure>");
    }

    void writeHeader() {

    }

    void generateEscapedText(String text) {
        text = text.replaceAll("<", "&lt;");
        text = text.replaceAll(">", "&gt;");
        _html.body().append(text);
    }

    void generateContent(Content content) {
        if (content == null)
            return;
        _html.body().append("<span");
        _html.body().append(id(content));
        _html.body().append(">");
        for (FormattedText text : content.content()) {
            if (text.bold) {
                _html.body().append("<b>");
            }
            if (text.italic) {
                _html.body().append("<i>");
            }
            if (text.underlined) {
                _html.body().append("<u>");
            }
            if (text.stroked) {
                _html.body().append("<del>");
            }
            if (text.monospaced) {
                _html.body().append("<tt>");
            }
            generateEscapedText(text.text);
            _html.body().append(" ");

            if (text.monospaced) {
                _html.body().append("</tt>");
            }
            if (text.stroked) {
                _html.body().append("</del>");
            }
            if (text.underlined) {
                _html.body().append("</u>");
            }
            if (text.italic) {
                _html.body().append("</i>");
            }
            if (text.bold) {
                _html.body().append("</b>");
            }
        }
        _html.body().append("</span>");
        //_html.body().append("</br>");
    }

    void generateHeadline(Headline headline, Document document) {
        if (headline.level <= 6) {
            _html.body().append("<h").append(headline.level).append(">");
            generateBlock(headline.content, document);
            _html.body().append("</h").append(headline.level).append(">");
        }
    }

    void generateCode(Code code, Document document) {

        _html.body().append("<figure>");
        if (code.type != null && !code.type.isEmpty()) {
            _html.body().append("<pre").append(id(code)).append("> <code class=\"").append(code.type).append("\">\n");
        } else {
            _html.body().append("<pre").append(id(code)).append("> <code>\n");
        }

        _html.body().append(code.code);
        _html.body().append("</code> </pre>\n");
        generateCaption(code.caption, document);
        _html.body().append("</figure>");
    }

    void generateListEntries(ArrayDeque<List> entries, Document document) {
        if (entries.isEmpty())
            return;

        String type = "ul";
        String style = "";
        switch (entries.getFirst().type) {
            case Points:
                type = "ul";
                break;
            case Numbers:
                type = "ol";
                break;
        }

        _html.body().append("<").append(type).append(" ").append(style).append(">\n");

        for (List entry : entries) {
            _html.body().append("<li> ");
            generateBlock(entry.content, document);
            generateListEntries(entry.entries, document);
            _html.body().append("</li> ");

        }

        _html.body().append("</").append(type).append(">\n");
    }

    void generateList(List list, Document document) {
        _html.body().append("<figure>");
        generateListEntries(list.entries.getFirst().entries, document);
        generateCaption(list.caption, document);
        _html.body().append("</figure>");

    }

    void generateTable(Table table, Document document) {
        _html.body().append("<table border=\"1\">\n");
        boolean firstRow = true;
        for (Table.Cell[] row : table.cells()) {
            boolean firstColumn = true;
            _html.body().append("<tr>");
            String end = "";

            for (Table.Cell cell: row) {
                if( cell == null ) {
                    continue;
                }
                StringBuilder span = new StringBuilder();
                if (cell.isHiddenBySpan) {
                    continue;
                }

                if (cell.columnSpan > 1) {
                    span.append(" colspan=\"").append(cell.columnSpan).append("\"");
                }
                if (cell.rowSpan > 1) {
                    span.append(" rowspan=\"").append(cell.rowSpan).append("\"");
                }
                if (cell.isHeading && firstRow) {
                    _html.body().append("<th scope=\"col\"").append(span).append(">\n");
                    end = "</th>";
                } else if (cell.isHeading && firstColumn) {
                    _html.body().append("<th scope=\"row\"").append(span).append(">\n");
                    end = "</th>";
                } else {
                    _html.body().append("<td").append(span).append(">\n");
                    end = "</td>";
                }

                for( Block block: cell.content ) {
                    generateBlock(block, document);
                }
                _html.body().append( end );

                firstColumn = false;
            }
            _html.body().append("</tr>\n");
            firstRow = false;
        }
        _html.body().append("</table>\n");
    }

    static public class HtmlDocument {
        StringBuffer _head = new StringBuffer();
        StringBuffer _body = new StringBuffer();

        public StringBuffer head() {
            return _head;
        }

        public StringBuffer body() {
            return _body;
        }

        public void write(String fileName) throws IOException {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            writer.write("<!doctype html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write(_head.toString());
            writer.write("\n");
            writer.write("</head>\n");

            writer.write("<body>\n");
            writer.write(_body.toString());
            writer.write("\n");
            writer.write("</body>\n");

            writer.close();
        }
    }

}

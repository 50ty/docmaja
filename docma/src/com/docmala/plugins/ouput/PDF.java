package com.docmala.plugins.ouput;

import com.docmala.parser.Block;
import com.docmala.parser.Document;
import com.docmala.parser.FormattedText;
import com.docmala.parser.blocks.*;
import com.docmala.parser.blocks.Image;
import com.docmala.parser.blocks.List;
import com.docmala.plugins.IOutput;
import com.docmala.plugins.IOutputDocument;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jdk.jshell.spi.ExecutionControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;


// http://kulatamicuda.github.io/iText-4.2.0/
public class PDF implements IOutput {
    public class PDFOutput implements IOutputDocument{
        java.util.List<Element> _elements;
        public PDFOutput (java.util.List<Element> elements){
            _elements  = elements;
        }

        @Override
        public void write(String fileName) throws FileNotFoundException, DocumentException {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.addAuthor("Bonnie und Clyde");
            document.open();
            for (Element element : _elements) {
                document.add(element);
            }
            document.close();
        }
    }

    @Override
    public IOutputDocument generate(Document document) {
        java.util.List<Element> elements = new ArrayList<Element>();

        Chapter lastChapter = null;
        Section lastH2 = null;
        Section lastH3 = null;
        Section lastH4 = null;
        Section lastH5 = null;
        Section lastH6 = null;
        Section currentHeadlineDocument = null;

        Paragraph lastParagraph =  null;

        for (Block block : document.content()) {
            if (block == null) {
                continue;
            }

            if (block instanceof Headline){
                String headlineText = "";
                Headline headline = (Headline) block;
                if (headline.level <= 6) {
                    Content content = (Content) headline.content;
                    for (FormattedText text : content.content()) {
                        headlineText += text.text;
                    }
                }
                if(headline.level == 1){
                    if(lastChapter != null){
                        elements.add(lastChapter);
                    }
                    lastChapter = new ChapterAutoNumber(headlineText);
                    lastChapter.setTriggerNewPage(false);
                    currentHeadlineDocument = lastChapter;
                }
                if(headline.level == 2){
                    if(lastChapter == null) lastChapter = new ChapterAutoNumber("");
                    lastH2 = lastChapter.addSection(headlineText);
                    currentHeadlineDocument = lastH2;
                    lastH3 = null;
                    lastH4 = null;
                    lastH5 = null;
                    lastH6 = null;
                }
                if(headline.level == 3){
                    if(lastChapter == null) lastChapter = new ChapterAutoNumber("");
                    if(lastH2 == null) lastH2 = lastChapter.addSection("");
                    lastH3 = lastH2.addSection(headlineText);
                    currentHeadlineDocument = lastH3;
                    lastH4 = null;
                    lastH5 = null;
                    lastH6 = null;
                }
                if(headline.level == 4){
                    if(lastChapter == null) lastChapter = new ChapterAutoNumber("");
                    if(lastH2 == null) lastH2 = lastChapter.addSection("");
                    if(lastH3 == null) lastH3 = lastH2.addSection("");
                    lastH4 = lastH3.addSection(headlineText);
                    currentHeadlineDocument = lastH4;
                    lastH5 = null;
                    lastH6 = null;
                }
                if(headline.level == 5){
                    if(lastChapter == null) lastChapter = new ChapterAutoNumber("");
                    if(lastH2 == null) lastH2 = lastChapter.addSection("");
                    if(lastH3 == null) lastH3 = lastH2.addSection("");
                    if(lastH4 == null) lastH4 = lastH3.addSection("");
                    lastH5 = lastH4.addSection(headlineText);
                    currentHeadlineDocument = lastH5;
                    lastH6 = null;
                }
                if(headline.level == 6){
                    if(lastChapter == null) lastChapter = new ChapterAutoNumber("");
                    if(lastH2 == null) lastH2 = lastChapter.addSection("");
                    if(lastH3 == null) lastH3 = lastH2.addSection("");
                    if(lastH4 == null) lastH4 = lastH3.addSection("");
                    if(lastH5 == null) lastH5 = lastH4.addSection("");
                    lastH6 = lastH5.addSection(headlineText);
                    currentHeadlineDocument = lastH6;
                }
            } else if (block instanceof com.docmala.parser.blocks.List) {
                //generateList((com.docmala.parser.blocks.List) block, document);
            } else if (block instanceof Image) {
                //generateImage((Image) block, document);
            } else if (block instanceof Content) {
                String tempContent = "";
                Content content = (Content) block;
                for (FormattedText text : content.content()) {
                    tempContent += text.text;
                }
                currentHeadlineDocument.add(new Paragraph(tempContent));

            } else if (block instanceof Code) {
                //generateCode((Code) block, document);
            } else if (block instanceof NextParagraph) {
                continue;
            }
        }

        elements.add(lastChapter);

        return new PDFOutput(elements);
    }
}

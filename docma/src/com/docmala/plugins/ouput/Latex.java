package com.docmala.plugins.ouput;

import com.docmala.parser.*;
import com.docmala.parser.blocks.*;
import com.docmala.plugins.IOutput;
import com.docmala.plugins.IOutputDocument;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.UUID;

public class Latex implements IOutput {

    public class LatexDocument implements IOutputDocument{
        StringBuffer _buffer;

        LatexDocument(StringBuffer buffer){
            _buffer = buffer;
        }

        @Override
        public void write(String fileName) throws IOException, InterruptedException {
            BufferedWriter writer = new BufferedWriter(new FileWriter(workDir.toString() + "/output.tex"));
            writer.write(_buffer.toString());
            writer.close();

            ProcessBuilder processBuilder = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", "--enable-write18" , "--shell-escape", "output.tex");
            processBuilder.directory(workDir);
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String readLine;
            boolean abort = false;
            while ((readLine = err.readLine()) != null)
            {
                System.out.println("INFO: " + readLine);
                System.out.println("ERROR: " + readLine);
                abort = true;
            }
            if(abort){
                throw new IOException("Latex Error");
            }
            while ((readLine = in.readLine()) != null)
            {
                System.out.println("INFO: " + readLine);
            }

            // Todo: check for Pygments to enable minted
            // Install sudo pacman -S pygmentize
            // Remove _minted_* folder after installation
        }

    }

    private long imageCounter = 0;
    private File workDir;
    private File sourceDir;

    public Latex(String inputFileName) throws Exception {
        String folderName = getHashFromDocumentName(inputFileName);
        workDir = new File("/tmp/docmala/" + folderName);
        if(!workDir.exists()){
            if(workDir.mkdirs())
                throw new Exception("Could not create workdir");
        }

        sourceDir = new File(inputFileName).getParentFile();
    }

    private String getHashFromDocumentName(String name){
        if(!name.isEmpty()){
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] md5dig = md5.digest(name.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : md5dig) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException ignored) {
            }
        }

        return UUID.randomUUID().toString();
    }


    @Override
    public LatexDocument generate(Document document) {

        StringBuffer content = new StringBuffer();

        generateDocumentHeader(document, content);

        for (Block block : document.content()) {
            generateBlock(content, block);
        }

        generateDocumentFooter(content);

        return new LatexDocument(content);
    }

    private void generateBlock(StringBuffer content, Block block) {
        if (block == null)
            return;
        if (block instanceof Headline) {
            generateHeadline(content, (Headline) block);
        } else if (block instanceof Content) {
            generateContent(content, (Content) block);
        } else if (block instanceof List) {
            generateList(content, (List) block);
        } else if (block instanceof Image) {
            generateImage(content, (Image) block);
        } else if (block instanceof Code) {
            generateCode(content, (Code) block);
        } else if (block instanceof NextParagraph) {
            generateNextParagraph(content);
        } else if (block instanceof Table) {
            generateTable(content, (Table) block);
        } else if (block instanceof Admonition) {
            generateAdmonition(content, (Admonition) block);
        }
    }

    private void generateContent(StringBuffer content, Content block) {
        StringBuilder tempContent = new StringBuilder();
        for (FormattedText text : block.content()) {
            if(text instanceof FormattedText.Link)
            {
                FormattedText.Link link = (FormattedText.Link) text;
                switch (link.type) {
                    case Web:
                        if(text.text.isEmpty())
                            tempContent.append("\\url{").append(link.url).append("} ");
                        else
                            tempContent.append("\\href{").append(link.url).append("}{").append(link.text).append("} ");
                        break;
                    case IntraFile:
                        tempContent.append("XXX");
                        break;
                    case InterFile:
                        tempContent.append("YYY");
                        break;
                }
            } else if (text instanceof FormattedText.Image) {
                FormattedText.Image image = (FormattedText.Image)text;

                String fileType = image.fileType;

                if(fileType.equals("svg+xml"))
                    fileType = "svg";

                imageCounter++;

                String fileName = String.valueOf(imageCounter) + "." + fileType;

                FileOutputStream writer = null;
                try {
                    writer = new FileOutputStream(workDir.toString() + "/" + fileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    if (writer != null) {
                        writer.write(image.data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(fileType.equals("svg")) {
                    tempContent.append("\\scalerel*{\\includesvg[clean,pdf]{").append(fileName).append("}}{B}").append(System.lineSeparator());
                } else {
                    tempContent.append("\\scalerel*{\\includegraphics[height=10pt]{").append(fileName).append("}}{B}").append(System.lineSeparator());
                }

            }
            else {
                if (text.bold)
                    tempContent.append("\\textbf{");
                if (text.italic)
                    tempContent.append("\\textit{");
                if (text.monospaced)
                    tempContent.append("\\texttt{");
                if (text.stroked)
                    tempContent.append("\\sout{");
                if (text.underlined)
                    tempContent.append("\\underline{");
                tempContent.append(text.text);
                if (text.bold)
                    tempContent.append("} ");
                if (text.italic)
                    tempContent.append("} ");
                if (text.monospaced)
                    tempContent.append("} ");
                if (text.stroked)
                    tempContent.append("} ");
                if (text.underlined)
                    tempContent.append("} ");
            }
        }
        content.append(tempContent).append(System.lineSeparator());
    }

    private void generateDocumentHeader(Document document, StringBuffer content) {
        content.append("\\documentclass[]{scrartcl}").append(System.lineSeparator());
        content.append("% Package definitions:").append(System.lineSeparator());
        content.append("\\usepackage[utf8]{inputenc}\n\\usepackage[T1]{fontenc}").append(System.lineSeparator());
        content.append("\\linespread{1.15}").append(System.lineSeparator());
        content.append("\\usepackage{geometry}").append(System.lineSeparator());
        content.append("\\geometry{a4paper,left=25mm,right=25mm, top=35mm, bottom=35mm,head=22.66618pt}").append(System.lineSeparator());
        content.append("\\usepackage{helvet}").append(System.lineSeparator());
        content.append("\\renewcommand{\\familydefault}{\\sfdefault}").append(System.lineSeparator());
        content.append("\\usepackage[automark]{scrlayer-scrpage}").append(System.lineSeparator());
        content.append("\\usepackage{lastpage}").append(System.lineSeparator());
        content.append("\\pagestyle{scrheadings}").append(System.lineSeparator());
        content.append("\\clearscrheadfoot{}").append(System.lineSeparator());
        if(document.metadata().containsKey("title")){
            content.append("\\ihead{").append(document.metadata().get("title").value.getFirst()).append("\\vspace{7px}}").append(System.lineSeparator());
        } else {
            content.append("\\ihead{" + "title not set" + "\\vspace{7px}}").append(System.lineSeparator());
        }
        content.append("\\ofoot[]{Page \\pagemark{} of \\pageref{LastPage}}").append(System.lineSeparator());
        content.append("\\setheadsepline[1.1\\textwidth]{1pt}").append(System.lineSeparator());
        content.append("\\setfootsepline[1.1\\textwidth]{1pt}").append(System.lineSeparator());
        content.append("\\usepackage[normalem]{ulem}").append(System.lineSeparator());
        content.append("\\usepackage{listings}").append(System.lineSeparator());
        content.append("\\usepackage{xcolor}").append(System.lineSeparator());
        content.append("\\usepackage[colorlinks,pdfborder={0 0 0},linkcolor={black},citecolor={blue!50!black},urlcolor={blue!80!black}]{hyperref}").append(System.lineSeparator());
        content.append("\\usepackage{multicol}").append(System.lineSeparator());
        content.append("\\usepackage{multirow}").append(System.lineSeparator());
        content.append("\\usepackage{relsize}").append(System.lineSeparator());
        content.append("\\usepackage{svg}").append(System.lineSeparator());
        content.append("\\usepackage[abspath]{currfile}").append(System.lineSeparator());
        content.append("\\usepackage{graphicx}").append(System.lineSeparator());
        content.append("\\usepackage{caption}").append(System.lineSeparator());
        content.append("\\usepackage{tikz}").append(System.lineSeparator());
        content.append("\\usepackage{aeguill}").append(System.lineSeparator());
        content.append("\\usepackage{scalerel}").append(System.lineSeparator());
        content.append("\\usepackage{tcolorbox}").append(System.lineSeparator());
        content.append("\\usepackage{minted}").append(System.lineSeparator());
        content.append("\\usemintedstyle{borland}").append(System.lineSeparator());
        content.append("\\definecolor{bluekeywords}{HTML}{1A237E}").append(System.lineSeparator());
        content.append("\\definecolor{greencomments}{HTML}{1B5E20}").append(System.lineSeparator());
        content.append("\\definecolor{redstrings}{HTML}{B71C1C}").append(System.lineSeparator());
        content.append("\\definecolor{backcolour}{rgb}{0.98,0.98,0.98}").append(System.lineSeparator());
        content.append("\\setcounter{secnumdepth}{5}").append(System.lineSeparator());
        content.append("\\setcounter{tocdepth}{").append("6").append("}").append(System.lineSeparator());
        if(document.metadata().containsKey("title")){
            content.append("\\title{").append(document.metadata().get("title").value.getFirst()).append("}").append(System.lineSeparator());
        } else {
            content.append("\\title{").append("title not set").append("}").append(System.lineSeparator());
        }
        if(document.metadata().containsKey("authors")){
            content.append("\\author{");
            for (String author : document.metadata().get("authors").value) {
                content.append(author).append("      ");
            }
            content.append("}").append(System.lineSeparator());
        } else {
            content.append("\\author{}").append(System.lineSeparator());
        }
        if(document.metadata().containsKey("latex.style")){
            content.append("\t\\usepackage{").append(sourceDir.toString()).append("/").append(document.metadata().get("latex.style").value.getFirst()).append("}").append(System.lineSeparator());
        }

        content.append("\\begin{document}").append(System.lineSeparator());
        content.append("\\maketitle").append(System.lineSeparator());
        content.append("\\tableofcontents").append(System.lineSeparator());
        content.append("\\newpage").append(System.lineSeparator());
    }

    private void generateDocumentFooter(StringBuffer content) {
        content.append("\\end{document}").append(System.lineSeparator());
    }

    private void generateNextParagraph(StringBuffer content) {
        content.append("$\\ $\\newline").append(System.lineSeparator());
    }

    private void generateCode(StringBuffer content, Code block) {
        content.append("\\begin{minted}[frame=lines, framesep=2mm, baselinestretch=1.2, linenos]{cpp}").append(System.lineSeparator());
        content.append(block.code.trim()).append(System.lineSeparator());
        content.append("\\end{minted}").append(System.lineSeparator());
    }

    private void generateImage(StringBuffer content, Image block) {
        String fileType = block.fileType;

        if(fileType.equals("svg+xml"))
            fileType = "svg";

        imageCounter++;

        String fileName = String.valueOf(imageCounter) + "." + fileType;

        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(workDir.toString() + "/" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (writer != null) {
                writer.write(block.data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        content.append("\\begin{figure}[htbp!]").append(System.lineSeparator());
        content.append("\\centering").append(System.lineSeparator());


        if(fileType.equals("svg"))
            content.append("\\includesvg[clean,pdf,pretex=\\relscale{0.9}]{").append(fileName).append("}").append(System.lineSeparator());
        else{
            content.append("\\includegraphics{").append(fileName).append("}").append(System.lineSeparator());
        }

        if(block.caption != null){
            StringBuilder capText = new StringBuilder();
            Caption caption = block.caption;
            Content capContent = (Content) caption.content;
            for (FormattedText text : capContent.content()) {
                capText.append(text.text);
            }

            content.append("\\caption{").append(capText).append("}").append(System.lineSeparator());
        }

        content.append("\\end{figure}").append(System.lineSeparator());
    }

    private void generateList(StringBuffer content, List block) {
        if(block.caption != null && !block.caption.toString().isEmpty())
            content.append("$\\ $\\newline\\textbf{").append(block.caption).append("} $\\ $\\\\").append(System.lineSeparator());
        generateListEntries(content, block.entries);
    }

    private void generateHeadline(StringBuffer content, Headline block) {
        StringBuilder headlineText = new StringBuilder();
        if (block.level <= 6) {
            Content hlcontent = (Content) block.content;
            for (FormattedText text : hlcontent.content()) {
                headlineText.append(text.text);
            }
        }
        if (block.level == 1) {
            content.append("\\section{").append(headlineText).append("}").append(System.lineSeparator());
        }
        else if (block.level == 2) {
            content.append("\\subsection{").append(headlineText).append("}").append(System.lineSeparator());
        }
        else if (block.level == 3) {
            content.append("\\subsubsection{").append(headlineText).append("}").append(System.lineSeparator());
        }
        else if (block.level == 4) {
            content.append("\\paragraph{").append(headlineText).append("} $\\ $\\\\").append(System.lineSeparator());
        }
        else if (block.level == 5) {
            content.append("\\subparagraph{").append(headlineText).append("} $\\ $\\\\").append(System.lineSeparator());
        }
        else if (block.level == 6) {
            content.append("$\\ $\\newline\\textbf{").append(headlineText).append("} $\\ $\\\\").append(System.lineSeparator());
        }
    }

    private void generateListEntries(StringBuffer content, ArrayDeque<List> entries) {
        if (entries.isEmpty())
            return;

        String listType = "itemize";

        switch (entries.getFirst().type) {
            case Points:
                break;
            case Numbers:
                listType = "enumerate";
                break;
        }

        content.append("\\begin{").append(listType).append("}").append(System.lineSeparator());

        for (List entry : entries) {
            Content listContent = (Content) entry.content;
            if (listContent != null) {
                content.append("\\item ");
                for (FormattedText text : listContent.content()) {
                    // todo format text
                    content.append(text.text);
                }
                content.append(System.lineSeparator());
            }
            generateListEntries(content, entry.entries);
        }

        content.append("\\end{").append(listType).append("}").append(System.lineSeparator());
    }

    private void generateTable(StringBuffer content, Table table) {
        StringBuilder capText = new StringBuilder();
        if(table.caption != null){
            Caption caption = table.caption;
            Content capContent = (Content) caption.content;
            for (FormattedText text : capContent.content()) {
                capText.append(text.text);
            }
        }

        content.append("\\begin{center}\\begin{tabular}{");
        boolean firstRow = true;
        for (Table.Cell[] row : table.cells()) {
            for (Table.Cell ignored : row) {
                if (firstRow)
                    content.append("l");
            }
            firstRow = false;
        }
        content.append("}").append(System.lineSeparator());
        int ignoreCauseHiddenByMultiCol = 0;

        firstRow = true;
        for (Table.Cell[] row : table.cells()) {
            boolean firstColumn = true;
            if (!firstRow) {
                content.append("\\\\").append(System.lineSeparator());
            }

            final String colSeperator = "&";

            for (Table.Cell cell: row) {
                if( cell == null ) {
                    continue;
                }

                String end = "";

                if (cell.isHiddenBySpan) {
                    if (ignoreCauseHiddenByMultiCol != 0) {
                        ignoreCauseHiddenByMultiCol--;
                    } else {
                        content.append("&");
                    }
                }
                if (!firstColumn) {
                    content.append(colSeperator);
                }
                if (cell.columnSpan > 1) {
                    content.append("\\multicolumn{").append(String.valueOf(cell.columnSpan)).append("}{l}{");
                    ignoreCauseHiddenByMultiCol = cell.columnSpan;
                    end += "}";
                }

                if (cell.rowSpan > 1) {
                    content.append("\\multirow{").append(String.valueOf(cell.rowSpan)).append("}{*}{");
                    end += "}";
                }
                if (cell.isHeading && firstRow) {
                    content.append("\\textbf{");
                    end = "}";
                } else if (cell.isHeading && firstColumn) {
                    content.append("\\textbf{");
                    end = "}";
                }

                for( Block block: cell.content ) {
                    generateContent(content, (Content) block);
                }

                content.append(end);

                firstColumn = false;
            }

            firstRow = false;
        }
        content.append("\\\\").append(System.lineSeparator());
        content.append("\\end{tabular}\\captionof{table}{").append(capText).append("}\\end{center}").append(System.lineSeparator());
    }

    private void generateAdmonition(StringBuffer content, Admonition block) {
        content.append("\\begin{tcolorbox}[sidebyside,halign upper=center, lefthand width=0.7cm, title={\\large ").append(block.type.name()).append("}]");
        // todo find a better way to handle path of admonition symbols
        content.append("\\includesvg[clean,pdf,height=20pt]{").append(sourceDir.toString()).append("/../docma/resources/ad/").append(block.type.name().toLowerCase()).append("}");

        content.append("\\tcblower").append(System.lineSeparator());

        for (Block part : block.content) {
            generateBlock(content, part);
        }
        content.append("\\end{tcolorbox}").append(System.lineSeparator());
    }
}

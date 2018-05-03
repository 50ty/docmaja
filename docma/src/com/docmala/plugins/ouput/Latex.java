package com.docmala.plugins.ouput;

import com.docmala.parser.Block;
import com.docmala.parser.Document;
import com.docmala.parser.FormattedText;
import com.docmala.parser.blocks.*;
import com.docmala.plugins.IOutput;
import com.docmala.plugins.IOutputDocument;
import com.lowagie.text.ChapterAutoNumber;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Base64;

public class Latex implements IOutput {

    public class LatexDocument implements IOutputDocument{
        StringBuffer _buffer;

        public LatexDocument(StringBuffer buffer){
            _buffer = buffer;
        }

        @Override
        public void write(String fileName) throws IOException, DocumentException, InterruptedException {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName+ ".tex"));
            writer.write(_buffer.toString());
            writer.close();
            // todo: Run process in a temp workdir
            Process process = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", "--enable-write18" , "--shell-escape", fileName + ".tex").start();
            process.waitFor();
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String readLine;
            boolean abort = false;
            while ((readLine = err.readLine()) != null)
            {
                System.out.println("ERROR: " + readLine);
                abort = true;
            }
            if(abort){
                throw new DocumentException("Latex Error");
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
    @Override
    public LatexDocument generate(Document document) {
        StringBuffer content = new StringBuffer();
        long imageCounter = 0;

        content.append("\\documentclass[]{scrartcl}" + System.lineSeparator());
        content.append("% Package definitions:" + System.lineSeparator());
        content.append("\\usepackage[utf8]{inputenc}\n\\usepackage[T1]{fontenc}" + System.lineSeparator());
        content.append("\\linespread{1.15}" + System.lineSeparator());
        content.append("\\usepackage{geometry}" + System.lineSeparator());
        content.append("\\geometry{a4paper,left=25mm,right=25mm, top=35mm, bottom=35mm,head=22.66618pt}" + System.lineSeparator());
        content.append("\\usepackage{helvet}" + System.lineSeparator());
        content.append("\\renewcommand{\\familydefault}{\\sfdefault}" + System.lineSeparator());
        content.append("\\usepackage[automark]{scrlayer-scrpage}" + System.lineSeparator());
        content.append("\\usepackage{lastpage}" + System.lineSeparator());
        content.append("\\pagestyle{scrheadings}" + System.lineSeparator());
        content.append("\\clearscrheadfoot{}" + System.lineSeparator());
        if(document.metadata().containsKey("title")){
            content.append("\\ihead{" + document.metadata().get("title").value.getFirst() + "\\vspace{7px}}" + System.lineSeparator());
        } else {
            content.append("\\ihead{" + "title not set" + "\\vspace{7px}}" + System.lineSeparator());
        }
        content.append("\\ofoot[]{Page \\pagemark{} of \\pageref{LastPage}}" + System.lineSeparator());
        content.append("\\setheadsepline[1.1\\textwidth]{1pt}" + System.lineSeparator());
        content.append("\\setfootsepline[1.1\\textwidth]{1pt}" + System.lineSeparator());
        content.append("\\usepackage[normalem]{ulem}" + System.lineSeparator());
        content.append("\\usepackage{listings}" + System.lineSeparator());
        content.append("\\usepackage{xcolor}" + System.lineSeparator());
        content.append("\\usepackage[colorlinks,pdfborder={0 0 0},linkcolor={black},citecolor={blue!50!black},urlcolor={blue!80!black}]{hyperref}" + System.lineSeparator());
        content.append("\\usepackage{multicol}" + System.lineSeparator());
        content.append("\\usepackage{multirow}" + System.lineSeparator());
        content.append("\\usepackage{relsize}" + System.lineSeparator());
        content.append("\\usepackage{svg}" + System.lineSeparator());
        content.append("\\usepackage{graphicx}" + System.lineSeparator());
        content.append("\\usepackage{caption}" + System.lineSeparator());
        content.append("\\usepackage{tikz}" + System.lineSeparator());
        content.append("\\usepackage{aeguill}" + System.lineSeparator());
        content.append("\\usepackage{minted}" + System.lineSeparator());
        content.append("\\usemintedstyle{borland}" + System.lineSeparator());
        content.append("\\definecolor{bluekeywords}{HTML}{1A237E}" + System.lineSeparator());
        content.append("\\definecolor{greencomments}{HTML}{1B5E20}" + System.lineSeparator());
        content.append("\\definecolor{redstrings}{HTML}{B71C1C}" + System.lineSeparator());
        content.append("\\definecolor{backcolour}{rgb}{0.98,0.98,0.98}" + System.lineSeparator());
        content.append("\\setcounter{secnumdepth}{5}" + System.lineSeparator());
        content.append("\\setcounter{tocdepth}{" +"6" + "}" + System.lineSeparator());
        if(document.metadata().containsKey("title")){
            content.append("\\title{" + document.metadata().get("title").value.getFirst() + "}" + System.lineSeparator());
        } else {
            content.append("\\title{" + "title not set" + "}" + System.lineSeparator());
        }
        if(document.metadata().containsKey("authors")){
            content.append("\\author{");
            for (String author : document.metadata().get("authors").value) {
                content.append(author + "      ");
            }
            content.append("}" + System.lineSeparator());
        } else {
            content.append("\\author{}" + System.lineSeparator());
        }
        if(document.metadata().containsKey("latex.style")){
            content.append("\t\\usepackage{" + document.metadata().get("latex.style").value.getFirst() + "}" + System.lineSeparator());
        }

        content.append("\\begin{document}" + System.lineSeparator());
        content.append("\\maketitle" + System.lineSeparator());
        content.append("\\tableofcontents" + System.lineSeparator());
        content.append("\\newpage" + System.lineSeparator());
        //content.append(text.str() << "" + System.lineSeparator());

        for (Block block : document.content()) {
            if (block == null) {
                continue;
            }
            if (block instanceof Headline) {
                Headline headline = (Headline) block;
                String headlineText = "";
                if (headline.level <= 6) {
                    Content hlcontent = (Content) headline.content;
                    for (FormattedText text : hlcontent.content()) {
                        headlineText += text.text;
                    }
                }
                if (headline.level == 1) {
                    content.append("\\section{" + headlineText + "}"+ System.lineSeparator());
                }
                else if (headline.level == 2) {
                    content.append("\\subsection{" + headlineText + "}"+ System.lineSeparator());
                }
                else if (headline.level == 3) {
                    content.append("\\subsubsection{" + headlineText + "}"+ System.lineSeparator());
                }
                else if (headline.level == 4) {
                    content.append("\\paragraph{" + headlineText + "} $\\ $\\\\"+ System.lineSeparator());
                }
                else if (headline.level == 5) {
                    content.append("\\subparagraph{" + headlineText + "} $\\ $\\\\"+ System.lineSeparator());
                }
                else if (headline.level == 6) {
                    content.append("$\\ $\\newline\\textbf{" + headlineText + "} $\\ $\\\\"+ System.lineSeparator());
                }
            } else if (block instanceof Content) {
                // todo print formatted
                String tempContent = "";
                Content cocontent = (Content) block;
                for (FormattedText text : cocontent.content()) {
                    tempContent += text.text;
                }
                content.append(tempContent + System.lineSeparator());
            } else if (block instanceof List) {
                List list = (List) block;
                if(list.caption != null && !list.caption.toString().isEmpty())
                    content.append("$\\ $\\newline\\textbf{" + list.caption + "} $\\ $\\\\"+ System.lineSeparator());
                generateListEntries(content, list.entries);
            } else if (block instanceof Image) {
                Image image = (Image) block;
                String fileType = image.fileType;

                if(fileType== "svg+xml")
                    fileType = "svg";

                imageCounter++;

                String fileName = String.valueOf(imageCounter) + "." + fileType;

                FileOutputStream writer = null;
                try {
                    writer = new FileOutputStream(fileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    writer.write(image.data);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                content.append("\\begin{figure}[htbp]" + System.lineSeparator());
                content.append("\\centering" + System.lineSeparator());


                if(fileType == "svg")
                    content.append("\\includesvg[clean,pdf,pretex=\\relscale{0.9}]{" + fileName + "}" + System.lineSeparator());
                else{
                    content.append("\\includegraphics{" + fileName + "}" + System.lineSeparator());
                }

                if(image.caption != null){
                    String capText = "";
                    Caption caption = (Caption) image.caption;
                    Content capContent = (Content) caption.content;
                    for (FormattedText text : capContent.content()) {
                        capText += text.text;
                    }

                    content.append("\\caption{" + capText  + "}" + System.lineSeparator());
                }

                content.append("\\end{figure}" + System.lineSeparator());


            } else if (block instanceof Code) {
                content.append("\\begin{minted}[frame=lines, framesep=2mm, baselinestretch=1.2, linenos]{cpp}" + System.lineSeparator());
                content.append(((Code)block).code.trim() + System.lineSeparator());
                content.append("\\end{minted}" + System.lineSeparator());
            } else if (block instanceof NextParagraph) {
                content.append("$\\ $\\newline"+ System.lineSeparator());
            } else if (block instanceof Table) {
                generateTable(content, (Table) block);
            }

        }


        content.append("\\end{document}" + System.lineSeparator());

        return new LatexDocument(content);
    }

    static void generateListEntries(StringBuffer content, ArrayDeque<List> entries) {
        if (entries.isEmpty())
            return;

        String listType = "itemize";

        switch (entries.getFirst().type) {
            case Points:
                listType = "itemize";
                break;
            case Numbers:
                listType = "enumerate";
                break;
        }

        content.append("\\begin{" + listType + "}" + System.lineSeparator());

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

        content.append("\\end{" + listType + "}" + System.lineSeparator());
    }

    void generateTable(StringBuffer content, Table table) {
        String capText = "";
        if(table.caption != null){
            Caption caption = (Caption) table.caption;
            Content capContent = (Content) caption.content;
            for (FormattedText text : capContent.content()) {
                capText += text.text;
            }
        }

        content.append("\\begin{center}\\begin{tabular}{");
        boolean firstRow = true;
        for (Table.Cell[] row : table.cells()) {
            for (Table.Cell cell: row) {
                if (firstRow)
                    content.append("l");
            }
            firstRow = false;
        }
        content.append("}" + System.lineSeparator());
        int ignoreCauseHiddenByMultiCol = 0;

        firstRow = true;
        for (Table.Cell[] row : table.cells()) {
            boolean firstColumn = true;
            if (!firstRow) {
                content.append("\\\\" + System.lineSeparator());
            }

            final String colSeperator = "&";

            for (Table.Cell cell: row) {
                if( cell == null ) {
                    continue;
                }

                String end = "";

                StringBuilder span = new StringBuilder();
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
                    content.append("\\multicolumn{" + String.valueOf(cell.columnSpan)
                            + "}{l}{");
                    ignoreCauseHiddenByMultiCol = cell.columnSpan;
                    end += "}";
                }

                if (cell.rowSpan > 1) {
                    content.append("\\multirow{" + String.valueOf(cell.rowSpan) + "}{*}{");
                    end += "}";
                }
                if (cell.isHeading && firstRow) {
                    content.append("\\textbf{");
                    end = "}";
                } else if (cell.isHeading && firstColumn) {
                    content.append("\\textbf{");
                    end = "}";
                }

                // todo print formatted
                for( Block block: cell.content ) {
                    String tempContent = "";
                    Content cocontent = (Content) block;
                    for (FormattedText text : cocontent.content()) {
                        tempContent += text.text;
                    }
                    content.append(tempContent + System.lineSeparator());
                }

                content.append(end);

                firstColumn = false;
            }

            firstRow = false;
        }
        content.append("\\\\" + System.lineSeparator());
        content.append("\\end{tabular}\\captionof{table}{"+ capText +"}\\end{center}" + System.lineSeparator());
    }



}

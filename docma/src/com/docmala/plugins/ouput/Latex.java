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
        }
    }
    @Override
    public LatexDocument generate(Document document) {
        StringBuffer content = new StringBuffer();

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
                String headlineText = "";
                Headline headline = (Headline) block;
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
                String tempContent = "";
                Content cocontent = (Content) block;
                for (FormattedText text : cocontent.content()) {
                    tempContent += text.text;
                }
                content.append(tempContent + System.lineSeparator());
            } else if (block instanceof List) {
                // todo
            } else if (block instanceof Image) {
                // todo
            } else if (block instanceof Code) {
                content.append("\\begin{minted}[frame=lines, framesep=2mm, baselinestretch=1.2, linenos]{cpp}" + System.lineSeparator());
                content.append(((Code)block).code.trim() + System.lineSeparator());
                content.append("\\end{minted}" + System.lineSeparator());
            } else if (block instanceof NextParagraph) {
                content.append("\\newpage"+ System.lineSeparator());
            }
        }


        content.append("\\end{document}" + System.lineSeparator());

        return new LatexDocument(content);
    }






}

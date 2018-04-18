package com.docmala.plugins.ouput;

import com.docmala.parser.Block;
import com.docmala.parser.Document;
import com.docmala.parser.FormattedText;
import com.docmala.parser.blocks.Content;
import com.docmala.parser.blocks.Headline;
import com.docmala.plugins.IOutput;
import com.docmala.plugins.IOutputDocument;
import com.lowagie.text.ChapterAutoNumber;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
            Process process = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", "--enable-write18", fileName + ".tex").start();
            process.waitFor();
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
        content.append("\\geometry{a4paper,left=25mm,right=25mm, top=35mm, bottom=35mm}" + System.lineSeparator());
        content.append("\\usepackage{helvet}" + System.lineSeparator());
        content.append("\\renewcommand{\\familydefault}{\\sfdefault}" + System.lineSeparator());
        content.append("\\usepackage[automark]{scrpage2}" + System.lineSeparator());
        content.append("\\usepackage{lastpage}" + System.lineSeparator());
        content.append("\\pagestyle{scrheadings}" + System.lineSeparator());
        content.append("\\clearscrheadfoot{}" + System.lineSeparator());
        content.append("\\ihead{" + "title" + "\\vspace{7px}}" + System.lineSeparator());
        content.append("\\ofoot[]{&\\ &\\\\Page \\pagemark{} of \\pageref{LastPage}}" + System.lineSeparator());
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
        content.append("\\definecolor{bluekeywords}{HTML}{1A237E}" + System.lineSeparator());
        content.append("\\definecolor{greencomments}{HTML}{1B5E20}" + System.lineSeparator());
        content.append("\\definecolor{redstrings}{HTML}{B71C1C}" + System.lineSeparator());
        content.append("\\definecolor{backcolour}{rgb}{0.98,0.98,0.98}" + System.lineSeparator());
        content.append("\\setcounter{secnumdepth}{5}" + System.lineSeparator());
        content.append("\\setcounter{tocdepth}{" +"6" + "}" + System.lineSeparator());
        content.append("\\title{" + "title" + "}" + System.lineSeparator());
        content.append("\\author{"  + "author" + "}" + System.lineSeparator());
        //content.append("\t\\usepackage{" << latexStyleDocument.substr(0, latexStyleDocument.find_last_of(".")) << "}" + System.lineSeparator());
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
            }else if (block instanceof Content) {
                String tempContent = "";
                Content cocontent = (Content) block;
                for (FormattedText text : cocontent.content()) {
                    tempContent += text.text;
                }
                content.append(tempContent + System.lineSeparator());

            }
        }


        content.append("\\end{document}" + System.lineSeparator());

        return new LatexDocument(content);
    }






}

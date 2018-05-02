package com.docmala;

import com.docmala.parser.ISourceProvider;
import com.docmala.parser.LocalFileSourceProvider;
import com.docmala.parser.Parser;
import com.docmala.plugins.IOutput;
import com.docmala.plugins.ouput.Html;
import com.docmala.plugins.ouput.Latex;
import com.docmala.plugins.ouput.PDF;
import com.lowagie.text.DocumentException;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        // write your code here
        Parser p = new Parser();

        try {
            ISourceProvider sourceProvider = new LocalFileSourceProvider(Paths.get("/"));//testdata/"));
            p.parse(sourceProvider, "/home/stefan/Desktop/docmaja-fork/testdata/test.docma");
            Html htmlOutput = new Html();
            Html.HtmlDocument doc = htmlOutput.generate(p.document());
            doc.write("/home/stefan/Desktop/docmaja-fork/testdata/test.html");

            //IOutput pdfOutput = new PDF();
            //pdfOutput.generate(p.document()).write("/home/stefan/Desktop/projects/docmaja/testdata/test.pdf");

            IOutput latexOutput = new Latex();
            latexOutput.generate(p.document()).write("/home/stefan/Desktop/docmaja-fork/testdata/test");

            for (Error error : p.errors()) {
                System.out.printf("%s:(%d,%d):%s%n", error.position().fileName(), error.position().line(), error.position().column(), error.message());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

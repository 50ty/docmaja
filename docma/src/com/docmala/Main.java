package com.docmala;

import com.docmala.parser.ISourceProvider;
import com.docmala.parser.LocalFileSourceProvider;
import com.docmala.parser.Parser;
import com.docmala.plugins.IOutput;
import com.docmala.plugins.ouput.Html;
import com.docmala.plugins.ouput.Latex;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        // write your code here
        Parser p = new Parser();

        try {
            String baseDir = "/home/stefan/projects/github/docmaja/testdata/";

            ISourceProvider sourceProvider = new LocalFileSourceProvider(Paths.get(baseDir));///home/michael/tmp/"));//testdata/"));
            //p.parse(sourceProvider, "/home/michael/tmp/DocmaPreviewPlugin.h:part1");
            p.parse(sourceProvider, "test.docma");
            Html htmlOutput = new Html();
            Html.HtmlDocument doc = htmlOutput.generate(p.document());
            doc.write(baseDir + "test.html");

            IOutput latexOutput = new Latex(baseDir + "test.docma");
            latexOutput.generate(p.document()).write(baseDir + "test");

            for (Error error : p.errors()) {
                System.out.printf("%s:(%d,%d):%s%n", error.position().fileName(), error.position().line(), error.position().column(), error.message());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

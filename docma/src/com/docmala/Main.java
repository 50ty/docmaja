package com.docmala;

import com.docmala.parser.ISourceProvider;
import com.docmala.parser.LocalFileSourceProvider;
import com.docmala.parser.Parser;
import com.docmala.plugins.ouput.Html;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        // write your code here
        Parser p = new Parser();

        try {
            ISourceProvider sourceProvider = new LocalFileSourceProvider(Paths.get("testdata/"));
            p.parse(sourceProvider, "test.docma");
            Html htmlOutput = new Html();
            Html.HtmlDocument doc = htmlOutput.generate(p.document());
            doc.write("/home/michael/temp/out.html");

            for (Error error : p.errors()) {
                System.out.printf("%s:(%d,%d):%s%n", error.position().fileName(), error.position().line(), error.position().column(), error.message());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

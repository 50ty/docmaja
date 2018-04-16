package com.docmala.plugins;

import com.docmala.parser.Document;
import com.lowagie.text.DocumentException;

import java.io.FileNotFoundException;

public interface IOutput {
    IOutputDocument generate(Document document);
}

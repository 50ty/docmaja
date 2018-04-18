package com.docmala.plugins;

import com.lowagie.text.DocumentException;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface IOutputDocument{
    void write(String fileName) throws IOException, DocumentException, InterruptedException;
}

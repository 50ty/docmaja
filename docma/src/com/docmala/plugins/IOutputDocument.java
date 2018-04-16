package com.docmala.plugins;

import com.lowagie.text.DocumentException;

import java.io.FileNotFoundException;

public interface IOutputDocument{
    void write(String fileName) throws FileNotFoundException, DocumentException;
}

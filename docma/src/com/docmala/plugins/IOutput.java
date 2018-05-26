package com.docmala.plugins;

import com.docmala.parser.Document;

public interface IOutput {
    IOutputDocument generate(Document document);
}

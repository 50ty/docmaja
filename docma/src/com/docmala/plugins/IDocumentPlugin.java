package com.docmala.plugins;

import com.docmala.Error;
import com.docmala.parser.*;

import java.util.ArrayDeque;

public interface IDocumentPlugin {
    BlockProcessing blockProcessing();

    ArrayDeque<Error> errors();

    void process(SourcePosition start, SourcePosition end, ArrayDeque<Parameter> parameters, DataBlock block, Document document, ISourceProvider sourceProvider);

    enum BlockProcessing {No, Required, Optional}
}

package com.docmala.plugins.document;

import com.docmala.Error;
import com.docmala.parser.*;
import com.docmala.parser.blocks.Content;
import com.docmala.parser.blocks.Image;
import com.docmala.plugins.IDocumentPlugin;

import java.io.IOException;
import java.util.ArrayDeque;

public class image implements IDocumentPlugin {
    ArrayDeque<Error> errors = new ArrayDeque<>();

    @Override
    public BlockProcessing blockProcessing() {
        return BlockProcessing.No;
    }

    @Override
    public ArrayDeque<Error> errors() {
        return errors;
    }

    @Override
    public void process(SourcePosition start, SourcePosition end, ArrayDeque<Parameter> parameters, DataBlock block, Document document, ISourceProvider sourceProvider) {
        Parameter file = null;
        Image.Builder image = new Image.Builder();

        for (Parameter parameter : parameters) {
            if (parameter.name().equals("file")) {
                file = parameter;
                break;
            }
        }

        if (file == null) {
            errors.addLast(new Error(parameters.getFirst().position(), "Parameter 'file' missing."));
            return;
        }


        try {
            byte[] data = sourceProvider.getBinary(file.value());
            String message = new String(data);
            // since the locally hosted html file is not able to open other files using javascript
            // we parse the returned message for a locally hosted indication tag and add some text instead of an image.
            if (message.startsWith("Included file:")) {
                ArrayDeque<FormattedText> content = new ArrayDeque<>();
                content.add(new FormattedText("Included image:" + message.substring(14), false, false, false, false, false));
                document.append(new Content(start, end, new ArrayDeque<>(), content));
                return;
            }
            image.setData(data);
        } catch (IOException e) {
            errors.addLast(new Error(file.position(), "Unable to open file: '" + file.value() + "'."));
        }

        image.setFileType(file.value().substring(file.value().lastIndexOf('.')+1));
        document.append(image.build());
    }

}

package com.docmala.plugins.document;

import com.docmala.Error;
import com.docmala.parser.*;
import com.docmala.parser.blocks.Caption;
import com.docmala.parser.blocks.Image;
import com.docmala.plugins.IDocumentPlugin;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.preproc.Defines;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class plantuml implements IDocumentPlugin {
    ArrayDeque<Error> errors = new ArrayDeque<>();

    @Override
    public BlockProcessing blockProcessing() {
        return BlockProcessing.Required;
    }

    @Override
    public ArrayDeque<Error> errors() {
        return errors;
    }

    @Override
    public void process(SourcePosition start, SourcePosition end, ArrayDeque<Parameter> parameters, DataBlock block, Document document, ISourceProvider sourceProvider) {
        errors.clear();

        StringBuilder data = new StringBuilder();
        data.append("@startuml\n").append(block.data).append("\n@enduml");

        final SourceStringReader sourceStringReader = new SourceStringReader(new Defines(), data.toString(), new ArrayList<>());
        final ByteArrayOutputStream svgImageData = new ByteArrayOutputStream();
        final ByteArrayOutputStream latexImageData = new ByteArrayOutputStream();

        try {
            final String svgResult = sourceStringReader.generateImage(svgImageData, 0, new FileFormatOption(FileFormat.SVG));
            if ("(error)".equalsIgnoreCase(svgResult)) {
                System.err.println("ERROR");
                final Diagram system = sourceStringReader.getBlocks().get(0).getDiagram();
                final PSystemError sys = (PSystemError) system;
                System.err.println(sys.getHigherErrorPosition());
                for (ErrorUml er : sys.getErrorsUml()) {
                    SourcePosition position = new SourcePosition(start);
                    position.addToLine(er.getPosition() + 1);
                    StringBuilder error = new StringBuilder();
                    error.append(er.getError());
                    if (er.getSuggest() != null) {
                        error.append(" Did you mean: ").append(er.getSuggest().getSuggestedLine());
                    }
                    errors.push(new Error(position, error.toString()));
                }
            }
            final String latexResult = sourceStringReader.generateImage(latexImageData, 0, new FileFormatOption(FileFormat.LATEX_NO_PREAMBLE));
            if ("(error)".equalsIgnoreCase(latexResult)) {
                System.err.println("ERROR");
                final Diagram system = sourceStringReader.getBlocks().get(0).getDiagram();
                final PSystemError sys = (PSystemError) system;
                System.err.println(sys.getHigherErrorPosition());
                for (ErrorUml er : sys.getErrorsUml()) {
                    SourcePosition position = new SourcePosition(start);
                    position.addToLine(er.getPosition() + 1);
                    StringBuilder error = new StringBuilder();
                    error.append(er.getError());
                    if (er.getSuggest() != null) {
                        error.append(" Did you mean: ").append(er.getSuggest().getSuggestedLine());
                    }
                    errors.push(new Error(position, error.toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        {
            Image.Builder imageBuilder = new Image.Builder();
            imageBuilder.setData(svgImageData.toByteArray());
            imageBuilder.setFileType("svg+xml");
            document.append(imageBuilder.build());
        }

        {
            Image.Builder imageBuilder = new Image.Builder();
            imageBuilder.setData(latexImageData.toByteArray());
            imageBuilder.setFileType("latex");
            document.append(imageBuilder.build());
        }


    }
}

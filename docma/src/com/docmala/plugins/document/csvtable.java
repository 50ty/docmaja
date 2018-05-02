package com.docmala.plugins.document;

import com.docmala.Error;
import com.docmala.parser.*;
import com.docmala.parser.blocks.Content;
import com.docmala.parser.blocks.Table;
import com.docmala.plugins.IDocumentPlugin;

import java.io.IOException;
import java.util.ArrayDeque;

public class csvtable implements IDocumentPlugin {
    ArrayDeque<Error> errors = new ArrayDeque<>();

    @Override
    public BlockProcessing blockProcessing() {
        return BlockProcessing.Optional;
    }

    @Override
    public ArrayDeque<Error> errors() {
        return errors;
    }

    Table.Cell buildCell(String value, SourcePosition start, SourcePosition end, boolean isHeading) {
        ArrayDeque<Block> blocks = new ArrayDeque<>();
        Content.Builder content = new Content.Builder();
        FormattedText.Builder text = new FormattedText.Builder();
        text.setText(value.toString());

        content.setStart(start);
        content.setEnd(end);

        ArrayDeque<FormattedText> contentArray = new ArrayDeque<>();
        contentArray.addLast(text.build());

        content.setContent(contentArray);
        blocks.addLast(content.build());
        return new Table.Cell(blocks, 1, 1, isHeading, false);
    }

    void parse(ISource source, Table.Builder builder, char separator, boolean hasHeader, boolean hasColumnHeader) {
        ISource.Window start = source.begin();

        StringBuilder value = new StringBuilder();
        ArrayDeque<ArrayDeque<Table.Cell>> lines = new ArrayDeque<>();
        ISource.Position begin = start.here().copy();
        int longestLineLength = 0;

        int row = 0;
        int col = 0;
        while (!start.here().isEof()) {
            ArrayDeque<Table.Cell> line = new ArrayDeque<>();
            boolean isQuoted = false;
            while (!start.here().isBlockEnd()) {
                if( start.here().equals('\"') ) {
                    isQuoted = !isQuoted;
                    start.moveForward();
                    continue;
                }
                if (start.here().equals(separator) && !isQuoted ) {
                    boolean isHeading = row == 0 && hasHeader || col == 0 && hasColumnHeader;
                    line.addLast(buildCell(value.toString(), begin, start.previous(), isHeading ));
                    begin = start.next().copy();
                    value = new StringBuilder();
                    start.moveForward();
                    start.skipWhitspaces();
                    col++;
                } else {
                    value.append(start.here().get());
                    start.moveForward();
                }
            }
            if( isQuoted ) {
                errors.addLast(new Error(start.here(), "Quotation not closed. Missing '\"'"));
            }
            boolean isHeading = row == 0 && hasHeader || col == 0 && hasColumnHeader;
            line.addLast(buildCell(value.toString(), begin, start.previous(), isHeading));
            if( line.size() > longestLineLength ) {
                longestLineLength = line.size();
            }
            value = new StringBuilder();
            lines.addLast(line);
            start.moveForward();
            start.skipWhitspaces();
            begin = start.next().copy();
            row++;
            col = 0;
        }

        Table.Cell[][] cells = new Table.Cell[lines.size()][longestLineLength];

        int i = 0;
        int j = 0;
        for( ArrayDeque<Table.Cell> l : lines ) {
            for( Table.Cell c : l ) {
                cells[i][j] = c;
                j++;
            }
            j = 0;
            i++;
        }
        builder.setCells(cells);
    }

    @Override
    public void process(SourcePosition start, SourcePosition end, ArrayDeque<Parameter> parameters, DataBlock block, Document document, ISourceProvider sourceProvider) {
        Table.Builder builder = new Table.Builder();
        builder.setStart(start);
        builder.setEnd(end);

        Parameter file = null;
        Parameter hasHeader = null;
        Parameter hasColumnHeader = null;
        char separator = ',';

        for (Parameter parameter : parameters) {
            if (parameter.name().equals("file")) {
                file = parameter;
            }
            if (parameter.name().equals("hasHeader")) {
                hasHeader = parameter;
            }
            if (parameter.name().equals("hasColumnHeader")) {
                hasColumnHeader = parameter;
            }
            if (parameter.name().equals("separator")) {
                char[] chars = parameter.value().toCharArray();
                if( chars.length != 1 ) {
                    errors.addLast( new Error(parameter.position(), "Separator has to be exactly one character."));
                } else {
                    separator = chars[0];
                }
            }
        }

        if( file == null && block == null ) {
            errors.addLast(new Error(start, "Data block ('--' ... '--') or 'file' parameter required"));
            return;
        } else if( file == null && block == null ) {
            errors.addLast(new Error(start, "Data block ('--' ... '--') and 'file' parameter can't be set at the same time"));
            return;
        }

            if( block != null ) {
            MemorySource source = new MemorySource(start.fileName(), block.data, start.line()+1);
            parse(source, builder, separator, hasHeader != null, hasColumnHeader != null);
        }
        else if( file != null ) {

            ISource source = null;
            try {
                source = sourceProvider.get(file.value());
            } catch (IOException e) {
                errors.addLast(new Error(start, "Unable to open file: " + file.value()));
            }
            parse(source, builder, separator, hasHeader != null, hasColumnHeader != null);
        }

        document.append(builder.build());
    }

}

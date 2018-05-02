package com.docmala.parser.blockParsers;

import com.docmala.Error;
import com.docmala.parser.*;
import com.docmala.plugins.IDocumentPlugin;

import java.util.ArrayDeque;

public class PluginParser implements IBlockParser {
    final ISourceProvider sourceProvider;
    final StringBuilder block = new StringBuilder();
    DataBlock dataBlock;
    ArrayDeque<Error> errors = new ArrayDeque<>();

    public PluginParser(ISourceProvider sourceProvider) {
        this.sourceProvider = sourceProvider;
    }

    boolean parseBlock(ISource.Window start) {
        block.setLength(0);
        ISource.Window begin = start.copy();
        start.skipWhitspaces();
        DataBlock.Builder dataBlockBuilder = new DataBlock.Builder();
        if (start.here().isNewLine()) {
            start.moveForward();
            int level = 0;
            while (start.here().equals('-')) {
                level++;
                start.moveForward();
            }

            if( level < 2 ) {
                start.setTo(begin);
                return false;
            }
            while (!start.here().isBlockEnd()) {
                start.moveForward();
            }

            dataBlockBuilder.setPosition(start.here());

            while (!start.here().isEof()) {
                int endLevel = 0;
                start.moveForward();

                if (start.here().equals('-')) {
                    while (start.here().equals('-')) {
                        endLevel++;
                        start.moveForward();
                    }

                    if (endLevel >= level) {
                        start.skipWhitspaces();
                        dataBlockBuilder.setData(block.toString());
                        dataBlock = dataBlockBuilder.build();
                        return true;
                    } else {
                        for (int i = 0; i < endLevel; i++) {
                            block.append('-');
                        }
                        block.append(start.here().get());
                        continue;
                    }
                }
                block.append(start.here().get());
            }
            return false;
        }
        return false;
    }

    @Override
    public ArrayDeque<Error> errors() {
        return errors;
    }

    @Override
    public boolean tryParse(ISource.Window start, IBlockHolder document) {
        if (start.here().equals('[') && !start.next().equals('[')) {
            errors.clear();
            dataBlock = null;
            final char[] end = {']', ','};
            ISource.Position begin = start.here();

            ParameterParser parameterParser = new ParameterParser();
            ArrayDeque<Parameter> parameters = new ArrayDeque<>();

            while (!start.here().isBlockEnd()) {
                start.skipWhitspaces();

                if (start.here().equals(']')) {
                    break;
                }
                start.moveForward();

                start = parameterParser.parse(start, end);
                parameters.addLast(parameterParser.parameter());
                errors.addAll(parameterParser.errors());
            }

            if (!start.here().equals(']')) {
                errors.addLast(new Error(start.here(), "Expected ']'"));
            } else {
                start.moveForward();
            }

            Parameter pluginParameter = parameters.getFirst();

            IDocumentPlugin plugin = null;
            try {
                Class<?> p = null;
                p = Class.forName("com.docmala.plugins.document." + pluginParameter.name());
                if (!IDocumentPlugin.class.isAssignableFrom(p)) {
                    throw new ClassNotFoundException();
                }
                plugin = (IDocumentPlugin) p.newInstance();
            } catch (ClassNotFoundException e) {
                errors.add(new Error(start.here(), "Plugin '" + pluginParameter.name() + "' not found."));
                return true;
            } catch (IllegalAccessException e) {
                errors.add(new Error(start.here(), "Plugin '" + pluginParameter.name() + "' cannot be accessed."));
                return true;
            } catch (InstantiationException e) {
                errors.add(new Error(start.here(), "Plugin '" + pluginParameter.name() + "' cannot be instanciated."));
                return true;
            }
            IDocumentPlugin.BlockProcessing blockProcessing = plugin.blockProcessing();

            if (blockProcessing != IDocumentPlugin.BlockProcessing.No) {
                if (!parseBlock(start)) {
                    if (blockProcessing == IDocumentPlugin.BlockProcessing.Required) {
                        errors.add(new Error(start.here(), "Plugin '" + pluginParameter.name() + "' requires a data block ('--')."));
                    }
                }
            }

            plugin.process(begin, start.here(), parameters, dataBlock, (Document) document, sourceProvider);
            errors.addAll(plugin.errors());

            return true;
        }
        return false;
    }
}

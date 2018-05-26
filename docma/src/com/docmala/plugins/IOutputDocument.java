package com.docmala.plugins;

import java.io.IOException;

public interface IOutputDocument{
    void write(String fileName) throws IOException, InterruptedException;
}

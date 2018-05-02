package com.docmala.server;

import com.docmala.parser.ISource;
import com.docmala.parser.ISourceProvider;
import com.docmala.parser.MemorySource;

import java.io.IOException;
import java.nio.charset.Charset;

public class RemoteSourceProvider implements ISourceProvider {
    final Requester requester;
    final String basePath;

    public RemoteSourceProvider(Requester requester) {
        this.requester = requester;
        basePath = "";
    }

    RemoteSourceProvider(Requester requester, String basePath) {
        this.requester = requester;
        this.basePath = basePath;
    }

    @Override
    public ISource get(String fileName) throws IOException {
        GetFileRequest.Result result = new GetFileRequest.Result();
        requester.sendRequest(new GetFileRequest(new GetFileRequest.Params(basePath + fileName)), result);
        result.waitForFinished();

        if (result.error != null) {
            throw new IOException(result.error);
        }

        if (result.content != null) {
            return new MemorySource(basePath + fileName, result.content);
        }
        return null;
    }

    @Override
    public byte[] getBinary(String fileName) throws IOException {
        GetFileRequest.Result result = new GetFileRequest.Result();
        requester.sendRequest(new GetFileRequest(new GetFileRequest.Params(basePath + fileName)), result);
        result.waitForFinished();
        if (result.error != null) {
            throw new IOException(result.error);
        }

        if (result.content != null) {
            return result.content.getBytes(Charset.forName("ISO-8859-1"));
        }

        return new byte[0];
    }

    @Override
    public ISourceProvider subProvider(String fileName) {
        String f = fileName.replace('\\', '/');
        int i = f.lastIndexOf('/');
        if (i == -1) {
            return this;
        } else {
            return new RemoteSourceProvider(requester, f.substring(0, i) + "/");
        }
    }

    @Override
    public String getFileName(String fileName) {
        //Todo: Should we handle the base path correctly and return a file name relative to the base path?
        String f = fileName.replace('\\', '/');
        int i = f.lastIndexOf('/');
        return fileName.substring(i + 1);
    }
}

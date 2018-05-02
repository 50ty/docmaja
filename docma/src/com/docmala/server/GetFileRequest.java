package com.docmala.server;

import com.jsoniter.ValueType;
import com.jsoniter.any.Any;

import java.nio.charset.Charset;
import java.util.Base64;

class GetFileRequest extends Requester.Request {

    public final Params params;

    public GetFileRequest(Params params) {
        super("getFile");
        this.params = params;
    }

    public static class Params {
        final String fileName;

        public Params(String fileName) {
            this.fileName = fileName;
        }
    }

    static class Result extends Requester.SynchronizedResultHandler {
        public String content = null;
        public String error = null;

        @Override
        public void finished(boolean isError, Any data) {
            if (!isError) {
                String base64 = data.toString("data");
                content = new String(Base64.getDecoder().decode(base64), Charset.forName("ISO-8859-1"));
            } else {
                if (data.valueType() != ValueType.INVALID) {
                    error = data.get("message").toString();
                } else {
                    error = "unknown";
                }
            }
            setFinished();
        }
    }
}

package com.open.request.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

public class DefaultResultHandler implements ResultHandler<byte[]> {
    @Override
    public Object handle(HttpResponse<byte[]> httpResponse) {
        int i = httpResponse.statusCode();
        if (i == 200 || i==201) {
            return gzipDecompression(httpResponse);
        } else {
            return httpResponse.body();
        }
    }
    @Override
    public HttpResponse.BodyHandler<byte[]> bodyHandler() {
        return HttpResponse.BodyHandlers.ofByteArray();
    }
}

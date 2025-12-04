package io.github.zaina01.request.handler;

import java.net.http.HttpResponse;

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

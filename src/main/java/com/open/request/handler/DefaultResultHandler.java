package com.open.request.handler;

import java.net.http.HttpResponse;

public class DefaultResultHandler implements ResultHandler<byte[]> {
    @Override
    public Object handle(HttpResponse<byte[]> httpResponse) {
        int i = httpResponse.statusCode();
        if (i == 200 || i==201) {
            return httpResponse.body();
        } else {
            return null;
        }
    }
    @Override
    public HttpResponse.BodyHandler<byte[]> bodyHandler() {
        return HttpResponse.BodyHandlers.ofByteArray();
    }
}

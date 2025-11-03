package com.open.request.handler;

import java.net.http.HttpResponse;

public interface ResultHandler<T> {
    Object handle(HttpResponse<T> httpResponse);
    
    HttpResponse.BodyHandler<T> bodyHandler();

    default Object executeHandle(HttpResponse<?> httpResponse) {
        return handle((HttpResponse<T>) httpResponse);
    }
}

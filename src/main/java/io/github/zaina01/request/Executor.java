package io.github.zaina01.request;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Executor {
    public static <T> HttpResponse<T> execute(HttpStatement httpStatement, HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler) {
        try {
            return httpStatement.getHttpClient().send(httpRequest, bodyHandler);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> CompletableFuture<HttpResponse<T>> executeAsync(HttpStatement httpStatement, HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler) {
        return httpStatement.getHttpClient().sendAsync(httpRequest, bodyHandler);
    }
}

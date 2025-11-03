package com.open.request;

import com.open.request.handler.DefaultResultHandler;
import com.open.request.handler.ResultHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestResultHandler {
    @Test
    public void testResultHandler() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        HttpRequest httpRequest=HttpRequest.newBuilder(URI.create("http://localhost:8080/request/test")).GET().build();
        Class<? extends ResultHandler<?>> resultHandlerClass = DefaultResultHandler.class;
        ResultHandler<?> resultHandler = resultHandlerClass.getDeclaredConstructor().newInstance();
        HttpResponse.BodyHandler<?> bodyHandler = resultHandler.bodyHandler();
        HttpResponse<?> httpResponse = HttpClient.newHttpClient().send(httpRequest, bodyHandler);
        Object test = resultHandler.executeHandle(httpResponse);

    }
    @Test
    public void testResultHandler2() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        Configuration config = new Configuration();
        config.addRequest(TestHttp.class);
        TestHttp request = config.getRequest(TestHttp.class);
        String user = request.get();
        System.out.println("user:"+user);
    }
}

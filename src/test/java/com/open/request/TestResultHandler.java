package com.open.request;

import com.open.request.handler.DefaultResultHandler;
import com.open.request.handler.ResultHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.InvocationInterceptor;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TestResultHandler {
    @Test
    public void testResultHandler() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("http://localhost:8080/request/test")).GET().build();
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
        String user = request.get("1", "2",HttpHeaders.newBuilder());
        System.out.println("user:" + user);
    }

    @Test
    void testType() {
        List<String> strings = new ArrayList<>();
        Method declaredMethod = TestHttp.class.getDeclaredMethods()[0];
        Type genericReturnType = declaredMethod.getGenericReturnType();
        System.out.println(genericReturnType);
        Class<?> aClass = strings.getClass();
        System.out.println(aClass);
    }

    @Test
    void testHeaders() throws Throwable {
        Class<TestHttp> testHttpClass = TestHttp.class;
        Configuration config = new Configuration();
        config.addRequest(testHttpClass);
        TestHttp request = config.getRequest(testHttpClass);
        HttpHeaders httpHeaders = HttpHeaders.newBuilder();
//        httpHeaders.setHeader("test", "test");
//        String string = request.get("1", "2", httpHeaders);
        HttpHeaders httpHeaders2 = HttpHeaders.newBuilder();
        httpHeaders2.setHeader("test", "test2");
//        httpHeaders2.allowedOverwrite();
        httpHeaders2.setHeader("ts", "ffff");
        String a = request.get("3", "4", httpHeaders2);
        System.out.println(a);
    }
}

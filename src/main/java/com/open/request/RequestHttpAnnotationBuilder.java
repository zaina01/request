package com.open.request;

import com.open.request.annotation.*;
import com.open.request.annotation.Request;
import com.open.request.enums.RequestType;
import com.open.request.handler.ResultHandler;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestHttpAnnotationBuilder {
    private static final Set<Class<? extends Annotation>> statementAnnotationTypes = Stream
            .of(Get.class, Post.class, Put.class, Delete.class)
            .collect(Collectors.toSet());

    private final Configuration configuration;
    private final Class<?> type;
    private final HttpBuilderAssistant assistant;

    public RequestHttpAnnotationBuilder(Configuration configuration, Class<?> type) {
        this.configuration = configuration;
        this.type = type;
        this.assistant = new HttpBuilderAssistant(configuration);
    }

    public void parse() {
        Request annotation = type.getAnnotation(Request.class);
        String url = annotation.url();
        if (url.isEmpty()) {
            url = annotation.value();
        }
        HttpClient httpClient = null;
        if (annotation.openSession()) {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();
        } else {
            httpClient = HttpClient.newHttpClient();
        }
        for (Method method : type.getMethods()) {
            if (!canHaveStatement(method)) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                setHttpDefaultHeader(method);
                continue;
            }
            parseStatement(method, url, httpClient);
        }
    }
    private void setHttpDefaultHeader(Method method) {
        if (!(method.getReturnType().isAssignableFrom(HttpHeaders.class)&&method.getParameterTypes().length==0)) {
            return;
        }
        if (!configuration.hasHttpDefaultHeaders(type)){
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
                MethodHandle methodHandle = lookup.unreflect(method);
                HttpHeaders headers = (HttpHeaders) methodHandle.invokeExact();
                configuration.addHttpDefaultHeader(type,headers);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static boolean canHaveStatement(Method method) {
        return !method.isBridge() && !method.isDefault();
    }

    void parseStatement(Method method, String url, HttpClient httpClient) {
        AnnotationWrapper annotationWrapper = null;
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (statementAnnotationTypes.contains(annotation.annotationType())) {
                annotationWrapper = new AnnotationWrapper(annotation);
                break;
            }
        }
        if (annotationWrapper == null) {
            throw new RuntimeException("Could not find a annotation");
        }
        boolean async = method.getAnnotation(RequestAsync.class) != null;
        if (annotationWrapper.url != null && (annotationWrapper.url.contains("https://") || annotationWrapper.url.contains("http://"))) {
            url = annotationWrapper.url;
        } else {
            url += annotationWrapper.url;
        }

        String httpStatementId = type.getName() + "." + method.getName();
        assistant.addHttpStatement(httpStatementId, annotationWrapper.requestType, httpClient, url, async, annotationWrapper.resultHandlerClass,annotationWrapper.enableDefaultHeaders);
    }

    private static class AnnotationWrapper {
        private final Annotation annotation;
        private final String url;
        private final RequestType requestType;
        private final Class<? extends ResultHandler<?>> resultHandlerClass;
        private final boolean enableDefaultHeaders;
        AnnotationWrapper(Annotation annotation) {
            this.annotation = annotation;
            if (annotation instanceof Post post) {
                url = post.value().isEmpty() ? post.url() : post.value();
                requestType = RequestType.Post;
                resultHandlerClass = post.handler();
                enableDefaultHeaders=post.enableDefaultHeaders();
            } else if (annotation instanceof Get get) {
                url = get.value().isEmpty() ? get.url() : get.value();
                requestType = RequestType.Get;
                resultHandlerClass = get.handler();
                enableDefaultHeaders=get.enableDefaultHeaders();
            } else if (annotation instanceof Put put) {
                url = put.value().isEmpty() ? put.url() : put.value();
                requestType = RequestType.Put;
                resultHandlerClass = put.handler();
                enableDefaultHeaders=put.enableDefaultHeaders();
            } else if (annotation instanceof Delete delete) {
                url = delete.value().isEmpty() ? delete.url() : delete.value();
                requestType = RequestType.Delete;
                resultHandlerClass = delete.handler();
                enableDefaultHeaders=delete.enableDefaultHeaders();
            } else {
                requestType = RequestType.UNKNOWN;
                resultHandlerClass = null;
                url = null;
                enableDefaultHeaders = false;
            }
        }

        Annotation getAnnotation() {
            return annotation;
        }

        public RequestType getRequestType() {
            return requestType;
        }

        public String getUrl() {
            return url;
        }

        public Class<? extends ResultHandler<?>> getResultHandlerClass() {
            return resultHandlerClass;
        }
    }
}

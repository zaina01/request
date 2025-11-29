package com.open.request;

import com.open.request.enums.RequestType;
import com.open.request.handler.ResultHandler;

import java.net.http.HttpClient;

public class HttpBuilderAssistant {
    private final Configuration configuration;

    //    private final String resource;
    public HttpBuilderAssistant(Configuration configuration) {
        this.configuration = configuration;
    }


    public HttpStatement addHttpStatement(String id, RequestType requestType, HttpClient httpClient, String url, boolean async, Class<? extends ResultHandler<?>> resultHandlerClass, boolean enableDefaultHeaders) {
        HttpStatement.Builder builder = new HttpStatement.Builder(id, requestType, httpClient, url);
        HttpStatement build = builder.async(async).resultHandler(resultHandlerClass).enableDefaultHeaders(enableDefaultHeaders).build();
        configuration.addHttpStatement(build);
        return build;
    }

}

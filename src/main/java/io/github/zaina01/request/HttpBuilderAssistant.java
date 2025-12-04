package io.github.zaina01.request;

import io.github.zaina01.request.enums.RequestType;
import io.github.zaina01.request.handler.ResultHandler;

import java.net.http.HttpClient;

public class HttpBuilderAssistant {
    private final Configuration configuration;

    //    private final String resource;
    public HttpBuilderAssistant(Configuration configuration) {
        this.configuration = configuration;
    }


    public HttpStatement addHttpStatement(String id, RequestType requestType, HttpClient httpClient, String url, boolean async, Class<? extends ResultHandler<?>> resultHandlerClass, boolean enableDefaultHeaders, long timeout) {
        HttpStatement.Builder builder = new HttpStatement.Builder(id, requestType, httpClient, url);
        HttpStatement build = builder.async(async).resultHandler(resultHandlerClass).enableDefaultHeaders(enableDefaultHeaders).timeout(timeout).build();
        configuration.addHttpStatement(build);
        return build;
    }

}

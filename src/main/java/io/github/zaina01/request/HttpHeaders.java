package io.github.zaina01.request;

import java.net.http.HttpRequest;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class HttpHeaders {
    private final TreeMap<String, List<String>> headersMap;
    private boolean allowedOverwrite = false;

    private HttpHeaders() {
        headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public static HttpHeaders newBuilder() {
        return new HttpHeaders();
    }

    public HttpHeaders allowedOverwrite(){
        this.allowedOverwrite = true;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public HttpHeaders addHeader(String name, String value) {
        headersMap.computeIfAbsent(name, _ -> new ArrayList<>(1))
                .add(value);
        return this;
    }
    @SuppressWarnings("UnusedReturnValue")
    public HttpHeaders setHeader(String name, String value) {
        List<String> values = new ArrayList<>(1);
        values.add(value);
        headersMap.put(name, values);
        return this;
    }
    @SuppressWarnings("UnusedReturnValue")
    private HttpHeaders addHeader(String nameValue) {
        Objects.requireNonNull(nameValue);
        String[] split = nameValue.split(":", 2);
        if (split.length != 2) throw new RuntimeException(String.format("不支持的格式: %s ", nameValue));
        this.addHeader(split[0].trim(), split[1].trim());
        return this;
    }
    public HttpHeaders addHeader(String... nameValues) {
        Objects.requireNonNull(nameValues);
        for (String nameValue : nameValues) {
            this.addHeader(nameValue);
        }
        return this;
    }
    public HttpHeaders headers(String... params) {
        requireNonNull(params);
        if (params.length == 0 || params.length % 2 != 0) {
            throw new IllegalArgumentException(String.format("wrong number, %d, of parameters", params.length));
        }
        for (int i = 0; i < params.length; i += 2) {
            String name  = params[i];
            String value = params[i + 1];
            addHeader(name, value);
        }
        return this;
    }
    public Map<String, List<String>> map() {
        return headersMap;
    }

    public void clear() {
        headersMap.clear();
    }


    @Override
    public String toString() {
        return "HttpHeaders{" +
                "headersMap=" + headersMap +
                '}';
    }

    public void build(HttpRequest.Builder requestBuilder) {
        headersMap.forEach((k, v) -> {
            if (allowedOverwrite){
                String first = v.getFirst();
                requestBuilder.setHeader(k, first);
                v.stream().skip(1).forEach(val -> requestBuilder.header(k, val));
            }else {
                v.forEach(val -> requestBuilder.header(k, val));
            }
        });
    }
}

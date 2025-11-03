package com.open.request.utils;

import com.open.request.json.Json;

import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HeadersUtil {

    private Map<String, String> headers=new HashMap<>();

    public HeadersUtil setHeaders(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HeadersUtil setHeaders(String ...headers) {
        Objects.requireNonNull(headers);
        if (headers.length == 0) {
            throw new RuntimeException("empty headers");
        }
        Arrays.stream(headers).forEach(this::setHeaders);
        return this;
    }
    public HeadersUtil setHeaders(String header) {
        Objects.requireNonNull(header);
        String[] split = header.split(":", 2);
        if ( split.length != 2) throw new RuntimeException( String.format("不支持的格式: %s ", header));
        this.headers.put(split[0], split[1]);
        return this;
    }
    public HeadersUtil DictConvertToHeaders(String dict){
//        String substring = dict.trim().substring(1, dict.length() - 1);
        Map<String,String> map = Json.parseObject(dict, Map.class);
        headers.putAll(map);
        return this;
    }
    public void build(HttpRequest.Builder builder) {
        headers.forEach(builder::header);
    }
    public void clear(HttpRequest.Builder builder) {
        headers.clear();
    }
}

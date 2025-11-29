package com.open.request;

import com.open.request.annotation.Get;
import com.open.request.annotation.Param;
import com.open.request.annotation.Request;

@Request("https://www.baidu.com")
public interface TestHttp {
    static HttpHeaders header() {
        return HttpHeaders.newBuilder()
                .setHeader("ts","ddd");
    }
    @Get
    String get(@Param("query") String query,HttpHeaders headers);
}

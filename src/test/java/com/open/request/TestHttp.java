package com.open.request;

import com.open.request.annotation.*;

@Request("https://www.baidu.com")
public interface TestHttp {
    static HttpHeaders header() {
        return HttpHeaders.newBuilder()
                .setHeader("ts","ddd");
    }
    @Get
    String get(@Param("query") String query,HttpHeaders headers);


    @Post("/{query}")
    String testPathVariable(@PathVariable("query") String query);
}

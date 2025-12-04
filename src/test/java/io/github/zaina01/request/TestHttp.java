package io.github.zaina01.request;

import io.github.zaina01.request.annotation.*;

@Request("https://www.baidu.com")
public interface TestHttp {
    static HttpHeaders header() {
        return HttpHeaders.newBuilder()
                .setHeader("ts","ddd");
    }
    @Get
    String get(@Param("query") String query, HttpHeaders headers);


    @Post(value = "/{query}",timeout = 5)
    String testPathVariable(@PathVariable("query") String query);
}

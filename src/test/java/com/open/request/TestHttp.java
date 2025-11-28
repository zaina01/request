package com.open.request;

import com.open.request.annotation.Get;
import com.open.request.annotation.Headers;
import com.open.request.annotation.Param;
import com.open.request.annotation.Request;

import java.util.List;

@Request("https://www.baidu.com")
public interface TestHttp {
    default HttpHeaders header() {
        return HttpHeaders.newBuilder().setHeader("ts","bbb")
                .setHeader("ts","ccc")
                .setHeader("ts","ddd");
    }
    @Get
    String get( String a, String b, @Headers HttpHeaders headers);
}

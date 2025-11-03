package com.open.request;

import com.open.request.annotation.Get;
import com.open.request.annotation.Request;

@Request("https://www.baidu.com")
public interface TestHttp {
    @Get
    String get();
}

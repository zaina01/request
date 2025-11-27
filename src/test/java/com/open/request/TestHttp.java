package com.open.request;

import com.open.request.annotation.Get;
import com.open.request.annotation.Param;
import com.open.request.annotation.Request;

import java.util.List;

@Request("https://www.baidu.com")
public interface TestHttp {
    @Get
    List<String> get(@Param(name = "yy") String a,@Param(name = "pp") String b);
}

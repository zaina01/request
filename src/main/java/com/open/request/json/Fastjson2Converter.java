package com.open.request.json;

import com.alibaba.fastjson2.JSON;

import java.util.List;

public class Fastjson2Converter implements JsonConverter{
    @Override
    public <T> T parseObject(String text, Class<T> clazz) {

        return JSON.parseObject(text,clazz);
    }

    @Override
    public <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSON.parseArray(text,clazz);
    }

    @Override
    public String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }
}

package io.github.zaina01.request.json;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;
import java.util.List;

public class Fastjson2Converter implements JsonConverter{
    @Override
    public <T> T parseObject(String text, Class<T> clazz) {

        return JSON.parseObject(text,clazz);
    }

    @Override
    public <T> T parseObject(String text, Type type) {
        return JSON.parseObject(text,type);
    }

    @Override
    public <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSON.parseArray(text,clazz);
    }

    @Override
    public String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    @Override
    public void clearCaches() {

    }
}

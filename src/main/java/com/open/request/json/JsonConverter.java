package com.open.request.json;

import java.lang.reflect.Type;
import java.util.List;

public interface JsonConverter {
    <T> T parseObject(String text, Class<T> clazz);
    <T> T parseObject(String text, Type type);

    <T> List<T> parseArray(String text, Class<T> clazz);

    String toJSONString(Object object);
    void clearCaches();
}

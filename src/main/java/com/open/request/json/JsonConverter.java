package com.open.request.json;

import java.util.List;

public interface JsonConverter {
    <T> T parseObject(String text, Class<T> clazz);

    <T> List<T> parseArray(String text, Class<T> clazz);

    String toJSONString(Object object);
    void clearCaches();
}

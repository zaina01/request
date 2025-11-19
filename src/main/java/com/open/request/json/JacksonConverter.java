package com.open.request.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class JacksonConverter implements JsonConverter{
    private final ObjectMapper objectMapper=new ObjectMapper();
    {
        try {
            Class.forName("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");
            objectMapper.registerModule(new JavaTimeModule());
        }catch (ClassNotFoundException _) {
        }
    }
    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text,clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> parseArray(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text,objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJSONString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCaches() {
        objectMapper.clearCaches();
    }
}

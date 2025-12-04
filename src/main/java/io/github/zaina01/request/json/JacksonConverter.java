package io.github.zaina01.request.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Type;
import java.util.List;

public class JacksonConverter implements JsonConverter{
    private final ObjectMapper objectMapper=new ObjectMapper();
    {
        try {
            Class.forName("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule");
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }catch (ClassNotFoundException _) {
        }
    }
    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text,clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(text+e.getMessage(),e);
        }
    }

    @Override
    public <T> T parseObject(String text, Type type) {
        try {
            return objectMapper.readValue(text,objectMapper.getTypeFactory().constructType(type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(text+e.getMessage(),e);
        }
    }

    @Override
    public <T> List<T> parseArray(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text,objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(text+e.getMessage(),e);
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

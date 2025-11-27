package com.open.request.json;

import java.lang.reflect.Type;
import java.util.List;

public class Json {
    private static final JsonConverter converter;

    private static boolean checkFastjson2Class(){
        try {
            Class.forName("com.alibaba.fastjson2.JSON");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean checkJacksonClass(){
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    static {
        converter=checkFastjson2Class() ? new Fastjson2Converter() : checkJacksonClass() ? new JacksonConverter() : null;
    }

    public static  <T> T parseObject(String text, Class<T> clazz){
        return converter.parseObject(text, clazz);
    }

    public static  <T> T parseObject(String text, Type type){
        return converter.parseObject(text, type);
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz){
        return converter.parseArray(text, clazz);
    }

    public static  String toJSONString(Object object){
        return converter.toJSONString(object);
    }
    public static  void clearCaches(){
        converter.clearCaches();
    }
}

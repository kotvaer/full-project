package dev.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public record Rest<T>(int code, T data, String message) {
    public static <T> Rest<T> success(T data) {
        return new Rest<T>(200,data,"请求成功");
    }
    public static <T> Rest<T> success() {
        return new Rest<T>(200,null,"请求成功");
    }

    public static <T> Rest<T> unauthorized(String message) {
        return new Rest<T>(401,null,message);
    }
    public static <T> Rest<T> forbidden(String message) {
        return new Rest<T>(403,null,message);
    }

    public static <T> Rest<T> failure(int code, String message) {
        return new Rest<T>(code,null,message);
    }
    public String asJsonString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }
}

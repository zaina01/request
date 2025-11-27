package com.open.request.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

public interface ResultHandler<T> {
    Object handle(HttpResponse<T> httpResponse);
    
    HttpResponse.BodyHandler<T> bodyHandler();

    default Object executeHandle(HttpResponse<?> httpResponse) {
        return handle((HttpResponse<T>) httpResponse);
    }

    default byte[] gzipDecompression(HttpResponse<byte[]> httpResponse) {
        String contentEncoding = httpResponse.headers()
                .firstValue("Content-Encoding")
                .orElse("");
        byte[] body = httpResponse.body();
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            try(GZIPInputStream gzipIs =new GZIPInputStream(new ByteArrayInputStream(body)); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                // 读取解压后的数据
                while ((len = gzipIs.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Gzip解压失败", e);
            }

        }
        return body;
    }
}

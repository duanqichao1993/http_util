package com.example.http_util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Objects;

public class HttpUtil<T> {
    private final HttpUrl url;

    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.get("application/json; charset=utf-8");

    public HttpUtil(HttpUrl url) {
        this.url = url;
    }

    public Response doPost(Object body) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(JSON.toJSONString(body), MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        return call.execute();
    }

    public Response doPostWithHeader(Object body, Headers headers) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(JSON.toJSONString(body), MEDIA_TYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .headers(headers)
                .build();
        Call call = okHttpClient.newCall(request);
        return call.execute();
    }

    public T doPostEntity(Object body , Class<T> returnEntity) throws IOException {
        Response response = this.doPost(body);
        if (response == null) {
            throw new InvalidParameterException("response is null ");
        }
        if (response.body() == null) {
            throw new InvalidParameterException("response body  is null ");
        }
        String result = Objects.requireNonNull(response.body()).string();
        return JSONObject.parseObject(result, returnEntity);
    }
}

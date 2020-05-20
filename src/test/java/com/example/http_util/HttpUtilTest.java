package com.example.http_util;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpUtilTest {

    public static final String BODY = "{\n" +
            "  \"code\":200,\n" +
            "   \"success\":true,\n" +
            "   \"data\":{\n" +
            "     \"aaa\":\"111\"\n" +
            "}\n" +
            "\n" +
            "}";
    MockWebServer mockWebServer;

    @BeforeEach
    public void init() throws IOException {
        mockWebServer = new MockWebServer();
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request == null) {
                    throw  new RuntimeException("request is Null");
                }
                switch (Objects.requireNonNull(request.getPath())) {
                    case "/v1/test":
                        return new MockResponse().setResponseCode(200);
                    case "v1/check/version/":
                        return new MockResponse().setResponseCode(200).setBody("version=9");
                    case "/v1/profile/info":
                        return new MockResponse().setResponseCode(200).setBody(BODY);
                    case "/v2/header/test":
                        Headers headers = request.getHeaders();
                        String token = headers.get("token");
                        if (StringUtils.isBlank(token)) {
                            throw new RuntimeException("not find Token, Please set token value in header");
                        }
                        return  new MockResponse().setResponseCode(200).setBody(BODY) ;
                }
                return new MockResponse().setResponseCode(404);
            }
        };
        mockWebServer.setDispatcher(dispatcher);

        mockWebServer.start();
    }


    @Test
    public void should_return_code_is_200_and_body_is_not_null_when_post() throws InterruptedException, IOException {
        HttpUrl url = mockWebServer.url("/v1/profile/info");
        HttpUtil<String> httpUtil = new HttpUtil<>(url);
        Response response = httpUtil.doPost("1111");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/profile/info");
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.request().method()).isEqualTo("POST");
        assertThat(response.body().string()).isNotNull();
    }

    @Test
    public void should_return_code_is_200_and_body_is_not_null_when_post_with_header()throws InterruptedException, IOException {
        HttpUrl url = mockWebServer.url("/v2/header/test");
        HttpUtil<String> httpUtil = new HttpUtil<>(url);
        // header 准备
        Headers headers = Headers.of("token", "this is token");
        // 发起http post 带header 调用
        Response response = httpUtil.doPostWithHeader("1111",headers);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        // 断言
        assertThat(recordedRequest.getPath()).isEqualTo("/v2/header/test");
        assertThat(recordedRequest.getHeader("token")).isEqualTo("this is token");
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.request().method()).isEqualTo("POST");
        assertThat(response.body().string()).isNotNull();
    }

    @Test
    public void should_return_object_when_post() throws InterruptedException, IOException {
        HttpUrl url = mockWebServer.url("/v1/profile/info");
        HttpUtil<String> httpUtil = new HttpUtil<>(url);
        String result  = httpUtil.doPostEntity("1111", String.class);
        System.out.println(result);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/profile/info");
        assertThat(result).isInstanceOf(String.class);
    }


    @AfterEach
    public void after() throws IOException {
        mockWebServer.close();
    }
}
package org.example.gamelist.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${ai.api.base-url}")
    private String baseUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.http.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${ai.api.http.read-timeout:60000}")
    private int readTimeout;

    @Bean
    public WebClient aiWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                // TCP 建连超时
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                // 等响应的间隔超时。非流式请求下，它就是"整个请求最多等多久"
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));

        return builder
                .baseUrl(baseUrl)
                // ⭐ 必须显式设置连接器，否则上面配的超时会被静默忽略（见下方说明）
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                // 默认上限只有 256KB，web_search 的结果可能撑爆，放到 2MB
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
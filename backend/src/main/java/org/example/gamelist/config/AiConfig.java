package org.example.gamelist.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix= "ai.api")
public class AiConfig {
    private String url;
    private String key;
    private String model;
    private Integer maxTokens;
    private Double temperature;
}

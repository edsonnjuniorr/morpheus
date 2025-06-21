package com.morpheus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "morpheus.jwt")
public class JwtProperties {
    private String secret;
    private int expiration;
    private String prefix;
}

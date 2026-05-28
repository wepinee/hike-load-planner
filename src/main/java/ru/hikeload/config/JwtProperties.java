package ru.hikeload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private int expirationHours = 24;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpirationHours() {
        return expirationHours;
    }

    public void setExpirationHours(int expirationHours) {
        this.expirationHours = expirationHours;
    }
}

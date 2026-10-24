package com.carpool.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrsConfig {
    @Value("${ors.apiKey}")
    private String apiKey;
    public String getApiKey() {
        return apiKey;
    }
    @Value(("${ors.baseUrl}"))
    private String baseUrl;
    public String getBaseUrl() {
        return baseUrl;
    }
}

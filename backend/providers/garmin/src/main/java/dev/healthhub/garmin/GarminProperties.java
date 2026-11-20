package dev.healthhub.garmin;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "healthhub.garmin")
public class GarminProperties {

    private boolean enabled = false;
    private String personalKey = "";
    private String keyHeader = "";
    private String baseUrl = "";
    private String userId = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPersonalKey() {
        return personalKey;
    }

    public void setPersonalKey(String personalKey) {
        this.personalKey = personalKey;
    }

    public String getKeyHeader() {
        return keyHeader;
    }

    public void setKeyHeader(String keyHeader) {
        this.keyHeader = keyHeader;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

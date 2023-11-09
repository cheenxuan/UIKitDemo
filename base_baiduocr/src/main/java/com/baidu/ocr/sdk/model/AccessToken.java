package com.baidu.ocr.sdk.model;

import java.io.Serializable;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class AccessToken implements Serializable {
    
    private final int preExpiredTime = 10000;
    private String accessToken;
    private String tokenJson;
    private String lic;
    private int expiresIn;
    private volatile long expiresTime = -1L;

    public AccessToken() {
    }

    public AccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getLic() {
        return this.lic;
    }

    public void setLic(String lic) {
        this.lic = lic;
    }

    public long getExpiresTime() {
        return this.expiresTime;
    }

    public synchronized boolean hasExpired() {
        long now = System.currentTimeMillis();
        return this.expiresTime != -1L && now - (this.expiresTime - 10000L) > 0L;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return this.expiresIn;
    }

    public void setTokenJson(String json) {
        this.tokenJson = json;
    }

    public String getTokenJson() {
        return this.tokenJson;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        this.expiresTime = System.currentTimeMillis() + (long)expiresIn * 1000L;
    }

    public void setExpireTime(long expiresTime) {
        this.expiresTime = expiresTime;
    }
}

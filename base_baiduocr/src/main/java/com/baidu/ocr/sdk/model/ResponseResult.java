package com.baidu.ocr.sdk.model;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class ResponseResult {
    public static final int DIRECTION_UNSPECIFIED = -1;
    private long logId;
    private String jsonRes;

    public ResponseResult() {
    }

    public long getLogId() {
        return this.logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getJsonRes() {
        return this.jsonRes;
    }

    public void setJsonRes(String jsonRes) {
        this.jsonRes = jsonRes;
    }
}

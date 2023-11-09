package com.baidu.ocr.sdk.utils;


import static com.baidu.ocr.sdk.exception.SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR;

import com.baidu.ocr.sdk.exception.SDKError;
import com.baidu.ocr.sdk.model.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class AccessTokenParser implements Parser<AccessToken> {
    public AccessTokenParser() {
    }

    public AccessToken parse(String json) throws SDKError {
        SDKError error;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.isNull("status")) {
                int status = jsonObject.optInt("status");
                if (status == 0) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    if (data != null) {
                        AccessToken accessToken = new AccessToken();
                        accessToken.setTokenJson(json);
                        accessToken.setAccessToken(data.getString("access_token"));
                        if (data.has("lic")) {
                            accessToken.setLic(data.getString("lic"));
                        }

                        accessToken.setExpiresIn(data.optInt("expires_in"));
                        return accessToken;
                    } else {
                        return null;
                    }
                } else {
                    String message = jsonObject.optString("message");
                    Long logId = jsonObject.optLong("log_id");
                    error = new SDKError(status, message + " logId: " + logId);
                    throw error;
                }
            } else {
                throw new SDKError(ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json);
            }
        } catch (JSONException e) {
            throw new SDKError(ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json, e);
        }
    }
}
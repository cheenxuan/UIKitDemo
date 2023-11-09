package com.baidu.ocr.sdk.utils;

import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.BankCardResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class BankCardResultParser implements Parser<BankCardResult> {
    public BankCardResultParser() {
    }

    public BankCardResult parse(String json) throws OCRError {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code")) {
                OCRError error = new OCRError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                error.setLogId(jsonObject.optLong("log_id"));
                throw error;
            } else {
                BankCardResult result = new BankCardResult();
                result.setLogId(jsonObject.optLong("log_id"));
                result.setJsonRes(json);
                JSONObject resultObject = jsonObject.optJSONObject("result");
                if (resultObject != null) {
                    result.setBankCardNumber(resultObject.optString("bank_card_number"));
                    result.setBankCardType(resultObject.optInt("bank_card_type"));
                    result.setBankName(resultObject.optString("bank_name"));
                }

                return result;
            }
        } catch (JSONException e) {
            throw new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, "Server illegal response " + json, e);
        }
    }
}


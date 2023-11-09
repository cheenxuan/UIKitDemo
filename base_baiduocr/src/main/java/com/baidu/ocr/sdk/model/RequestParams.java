package com.baidu.ocr.sdk.model;

import java.io.File;
import java.util.Map;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public interface RequestParams {
    Map<String, File> getFileParams();

    Map<String, String> getStringParams();

    Map<String, String> getParams(IDCardParams idCardParams);

    Map<String, String> getParams(BankCardParams bankCardParams);
}

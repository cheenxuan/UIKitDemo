package com.baidu.ocr.sdk.utils;


import com.baidu.ocr.sdk.exception.OCRError;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public interface Parser<T> {
    T parse(String response) throws OCRError;
}
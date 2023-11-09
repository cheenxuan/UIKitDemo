package com.baidu.ocr.sdk;

import com.baidu.ocr.sdk.exception.OCRError;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public interface OnResultListener<T> {

    void onResult(T result);

    void onError(OCRError ocrError);
}

package com.baidu.ocr.sdk.utils;

import static com.baidu.ocr.sdk.exception.SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR;
import static com.baidu.ocr.sdk.exception.SDKError.ErrorCode.NETWORK_REQUEST_ERROR;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.exception.SDKError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.RequestParams;

import java.util.Map;


/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class HttpUtil {
    private Handler handler;
    private static volatile HttpUtil instance;
    private static Options options = new Options();

    private HttpUtil() {
    }

    public static void setOptions(Options options) {
        HttpUtil.options = options;
    }

    public static Options getOptions() {
        return options;
    }

    public static HttpUtil getInstance() {
        if (instance == null) {
            synchronized(HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }

        return instance;
    }

    public void init() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    public <T> void post(String path, RequestParams params, final Parser<T> parser, final OnResultListener<T> listener) {
        HttpsClient cl = new HttpsClient();
        HttpsClient.RequestBody body = new HttpsClient.RequestBody();
        body.setStrParams(params.getStringParams());
        body.setFileParams(params.getFileParams());
        HttpsClient.RequestInfo reqInfo = new HttpsClient.RequestInfo(path, body);
        reqInfo.build();
        cl.newCall(reqInfo).enqueue(new HttpsClient.Callback() {
            public void onFailure(final Throwable e) {
                HttpUtil.this.handler.post(new Runnable() {
                    public void run() {
                        HttpUtil.throwSDKError(listener, NETWORK_REQUEST_ERROR, "Network error", e);
                    }
                });
            }

            public void onResponse(String resultStr) {
                String responseString = resultStr;

                try {
                    final T result = parser.parse(responseString);
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final OCRError error) {
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onError(error);
                        }
                    });
                }

            }
        });
    }

    public <T> void postidcard(String path, IDCardParams params, final Parser<T> parser, final OnResultListener<T> listener) {
        HttpsClient cl = new HttpsClient();
        HttpsClient.RequestBody body = new HttpsClient.RequestBody();
        body.setStrParams(params.getParams(params));
        body.setFileParams(params.getFileParams());
        HttpsClient.RequestInfo reqInfo = new HttpsClient.RequestInfo(path, body);
        reqInfo.build();
        cl.newCall(reqInfo).enqueue(new HttpsClient.Callback() {
            public void onFailure(final Throwable e) {
                HttpUtil.this.handler.post(new Runnable() {
                    public void run() {
                        HttpUtil.throwSDKError(listener, NETWORK_REQUEST_ERROR, "Network error", e);
                    }
                });
            }

            public void onResponse(String resultStr) {
                String responseString = resultStr;

                try {
                    final T result = parser.parse(responseString);
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final OCRError error) {
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onError(error);
                        }
                    });
                }

            }
        });
    }

    public <T> void postcard(String path, BankCardParams params, final Parser<T> parser, final OnResultListener<T> listener) {
        HttpsClient cl = new HttpsClient();
        HttpsClient.RequestBody body = new HttpsClient.RequestBody();
        body.setStrParams(params.getParams(params));
        body.setFileParams(params.getFileParams());
        HttpsClient.RequestInfo reqInfo = new HttpsClient.RequestInfo(path, body);
        reqInfo.build();
        cl.newCall(reqInfo).enqueue(new HttpsClient.Callback() {
            public void onFailure(final Throwable e) {
                HttpUtil.this.handler.post(new Runnable() {
                    public void run() {
                        HttpUtil.throwSDKError(listener, NETWORK_REQUEST_ERROR, "Network error", e);
                    }
                });
            }

            public void onResponse(String resultStr) {
                String responseString = resultStr;

                try {
                    final T result = parser.parse(responseString);
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final OCRError error) {
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onError(error);
                        }
                    });
                }

            }
        });
    }

    public void getAccessToken(final OnResultListener<AccessToken> listener, String url, String param) {
        final Parser<AccessToken> accessTokenParser = new AccessTokenParser();
        HttpsClient cl = new HttpsClient();
        HttpsClient.RequestBody body = new HttpsClient.RequestBody();
        body.setBody(param);
        HttpsClient.RequestInfo reqInfo = new HttpsClient.RequestInfo(url, body);
        reqInfo.setHeader("Content-Type", "text/html");
        reqInfo.build();
        cl.newCall(reqInfo).enqueue(new HttpsClient.Callback() {
            public void onFailure(Throwable e) {
                HttpUtil.throwSDKError(listener, NETWORK_REQUEST_ERROR, "Network error", e);
            }

            public void onResponse(String resultStr) {
                if (resultStr != null && !TextUtils.isEmpty(resultStr)) {
                    try {
                        AccessToken accessToken = (AccessToken)accessTokenParser.parse(resultStr);
                        if (accessToken != null) {
                            OCR.getInstance((Context)null).setAccessToken(accessToken);
                            OCR.getInstance((Context)null).setLicense(accessToken.getLic());
                            listener.onResult(accessToken);
                        } else {
                            HttpUtil.throwSDKError(listener, ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + resultStr);
                        }
                    } catch (SDKError error) {
                        listener.onError(error);
                    } catch (Exception e) {
                        HttpUtil.throwSDKError(listener, ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + resultStr, e);
                    }

                } else {
                    HttpUtil.throwSDKError(listener, ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + resultStr);
                }
            }
        });
    }

    private static void throwSDKError(OnResultListener listener, int errorCode, String msg) {
        SDKError error = new SDKError(errorCode, msg);
        listener.onError(error);
    }

    private static void throwSDKError(OnResultListener listener, int errorCode, String msg, Throwable cause) {
        SDKError error = new SDKError(errorCode, msg, cause);
        listener.onError(error);
    }

    public void release() {
        this.handler = null;
    }

    public static class Options {
        private int connectionTimeoutInMillis = 10000;
        private int socketTimeoutInMillis = 10000;

        public Options() {
        }

        public int getConnectionTimeoutInMillis() {
            return this.connectionTimeoutInMillis;
        }

        public void setConnectionTimeoutInMillis(int connectionTimeoutInMillis) {
            this.connectionTimeoutInMillis = connectionTimeoutInMillis;
        }

        public int getSocketTimeoutInMillis() {
            return this.socketTimeoutInMillis;
        }

        public void setSocketTimeoutInMillis(int socketTimeoutInMillis) {
            this.socketTimeoutInMillis = socketTimeoutInMillis;
        }
    }
}

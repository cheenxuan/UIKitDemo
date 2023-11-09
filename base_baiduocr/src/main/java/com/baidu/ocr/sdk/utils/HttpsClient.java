package com.baidu.ocr.sdk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class HttpsClient {
    public HttpsClient() {
    }

    public Call newCall(RequestInfo requestInfo) {
        Call call = new Call(requestInfo);
        return call;
    }

    public static class Call implements Runnable {
        private RequestInfo requestInfo;
        private Thread thread;
        private Callback callback;

        public Call(RequestInfo requestInfo) {
            this.requestInfo = requestInfo;
        }

        public Call enqueue(Callback callback) {
            this.callback = callback;
            this.thread = new Thread(this);
            this.thread.start();
            return this;
        }

        private void setHeaders(HttpURLConnection con, Map<String, String> headers) {
            Iterator param = headers.entrySet().iterator();

            while(param.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)param.next();
                con.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
            }

        }

        public void run() {
            RequestInfo requestInfo = this.requestInfo;
            HttpURLConnection con = null;
            Exception buildException;
            if ((buildException = requestInfo.getBuildException()) != null) {
                this.callback.onFailure(buildException);
            } else {
                try {
                    URL url = requestInfo.getURL();
                    byte[] body = requestInfo.getBody();
                    con = (HttpURLConnection)url.openConnection();
                    this.setHeaders(con, requestInfo.getHeaders());
                    con.setRequestMethod("POST");
                    con.setConnectTimeout(requestInfo.getConTimeout());
                    con.setReadTimeout(requestInfo.getReadTimeout());
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    OutputStream out = con.getOutputStream();
                    out.write(body);
                    this.writeResp(con);
                } catch (Throwable e) {
                    e.printStackTrace();
                    this.callback.onFailure(e);
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }

                }

            }
        }

        public void writeResp(HttpURLConnection con) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer sb = new StringBuffer();
                char[] cs = new char[512];
                
                int readedNumber;
                while((readedNumber = br.read(cs)) != -1) {
                    sb.append(new String(cs, 0, readedNumber));
                }

                this.callback.onResponse(sb.toString());
                br.close();
            } catch (IOException e) {
                this.callback.onFailure(e);
            }

        }
    }

    public static class RequestInfo {
        private String urlStr;
        private URL url;
        private Map<String, String> headers;
        private RequestBody body;
        private Exception ex;
        private int conTimeout;
        private int readTimeout;

        public Exception getBuildException() {
            return this.ex;
        }

        public int getConTimeout() {
            return this.conTimeout;
        }

        public int getReadTimeout() {
            return this.readTimeout;
        }

        public RequestInfo(String urlStr, RequestBody body) {
            this.urlStr = urlStr;
            this.body = body;
            this.headers = new HashMap();
            this.ex = null;
            this.conTimeout = HttpUtil.getOptions().getConnectionTimeoutInMillis();
            this.readTimeout = HttpUtil.getOptions().getSocketTimeoutInMillis();
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public void setHeader(String key, String value) {
            this.headers.put(key, value);
        }

        public void build() {
            try {
                this.url = new URL(this.urlStr);
            } catch (Exception e) {
                this.ex = e;
            }

        }

        public URL getURL() {
            return this.url;
        }

        public byte[] getBody() {
            return this.body.getBytes();
        }
    }

    public static class RequestBody {
        private StringBuffer stringBuffer = new StringBuffer();
        private int paramNumber = 0;
        private static String UTF8 = "UTF-8";
        private static FileBase64Encoder encoder = new FileBase64Encoder();

        public RequestBody() {
        }

        public void setBody(String body) {
            this.stringBuffer.append(body);
        }

        public void setStrParams(Map<String, String> params) {
            if (params != null) {
                Iterator param = params.entrySet().iterator();

                while(param.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)param.next();
                    if (this.paramNumber > 0) {
                        this.stringBuffer.append("&");
                    }

                    String key = (String)entry.getKey();
                    String value = (String)entry.getValue();

                    try {
                        key = URLEncoder.encode(key, UTF8);
                        value = URLEncoder.encode(value, UTF8);
                        this.stringBuffer.append(key + "=" + value);
                        ++this.paramNumber;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        public void setFileParams(Map<String, File> params) {
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                Iterator param = params.entrySet().iterator();

                while(param.hasNext()) {
                    Map.Entry<String, File> entry = (Map.Entry)param.next();
                    if (this.paramNumber > 0) {
                        this.stringBuffer.append("&");
                    }

                    String key = (String)entry.getKey();
                    File file = (File)entry.getValue();

                    try {
                        key = URLEncoder.encode(key, UTF8);
                        encoder.setInputFile(file);
                        this.stringBuffer.append(key + "=");

                        byte[] encoded;
                        while((encoded = encoder.encode()) != null) {
                            sb.append(new String(encoded));
                            this.stringBuffer.append(URLEncoder.encode(new String(encoded), UTF8));
                        }

                        ++this.paramNumber;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        public byte[] getBytes() {
            byte[] bytes = new byte[0];

            try {
                bytes = String.valueOf(this.stringBuffer).getBytes("UTF-8");
                return bytes;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return bytes;
            }
        }
    }

    public interface Callback {
        void onFailure(Throwable e);

        void onResponse(String response);
    }
}

package com.baidu.ocr.sdk;

import static com.baidu.ocr.sdk.exception.SDKError.ErrorCode.LOAD_JNI_LIBRARY_ERROR;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Base64;

import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.exception.SDKError;
import com.baidu.ocr.sdk.jni.JniInterface;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.sdk.tool.AESUtil;
import com.baidu.ocr.sdk.tool.Base64Util;
import com.baidu.ocr.sdk.tool.ContextProvider;
import com.baidu.ocr.sdk.tool.FileUtil;
import com.baidu.ocr.sdk.tool.RSAUtil;
import com.baidu.ocr.sdk.tool.StringUtil;
import com.baidu.ocr.sdk.utils.AccessTokenParser;
import com.baidu.ocr.sdk.utils.BankCardResultParser;
import com.baidu.ocr.sdk.utils.CrashReporterHandler;
import com.baidu.ocr.sdk.utils.DeviceUtil;
import com.baidu.ocr.sdk.utils.HttpUtil;
import com.baidu.ocr.sdk.utils.IDCardResultParser;
import com.baidu.ocr.sdk.utils.Parser;
import com.baidu.ocr.sdk.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


/**
 * @auther: QinjianXuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class OCR {

    private static final String QUERY_TOKEN = "https://verify.baidubce.com/verify/1.0/token/sk?sdkVersion=2_0_1";
    private static final String BANK_CARD_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/bankcard?";
    private static final String ID_CARD_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard?";

    private Context context;
    private CrashReporterHandler crInst;
    private int authStatus = 0;
    private boolean isAutoCacheToken = false;
    private String license = null;
    private String ak = null;
    private String sk = null;
    private AccessToken accessToken = null;
    private static volatile OCR instance;

    String data;
    String key;
    Handler mHandler = new Handler();

    private OCR(Context ctx) {
        if (ctx != null) {
            this.context = ctx;
        }

    }

    public static OCR getInstance(Context ctx) {
        if (instance == null) {
            synchronized (OCR.class) {
                if (instance == null) {
                    instance = new OCR(ctx);
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.crInst = CrashReporterHandler.init(context).addSourceClass(OCR.class);

        try {
            Class uiClass = Class.forName("com.baidu.ocr.ui.camera.CameraActivity");
            this.crInst.addSourceClass(uiClass);
        } catch (Throwable e) {
        }

        HttpUtil.getInstance().init();
    }

    public synchronized void setAccessToken(AccessToken accessToken) {
        if (accessToken.getTokenJson() != null) {
            SharedPreferences mSharedPreferences = this.context.getSharedPreferences("com.baidu.ocr.sdk", 0);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("token_json", accessToken.getTokenJson());
            editor.putLong("token_expire_time", accessToken.getExpiresTime());
            editor.putInt("token_auth_type", this.authStatus);
            editor.apply();
        }

        this.accessToken = accessToken;
    }

    public synchronized AccessToken getAccessToken() {
        return this.accessToken;
    }

    public void setLicense(String license) {
        this.license = license;
    }


    private AccessToken getByCache() {
        if (!this.isAutoCacheToken) {
            return null;
        } else {
            SharedPreferences mSharedPreferences = this.context.getSharedPreferences("com.baidu.ocr.sdk", 0);
            String json = mSharedPreferences.getString("token_json", "");
            int type = mSharedPreferences.getInt("token_auth_type", 0);
            if (type != this.authStatus) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.commit();
                return null;
            } else {
                AccessTokenParser parser = new AccessTokenParser();
                try {
                    AccessToken token = parser.parse(json);
                    long expireTime = mSharedPreferences.getLong("token_expire_time", 0L);
                    token.setExpireTime(expireTime);
                    this.authStatus = type;
                    return token;
                } catch (SDKError error) {
                    return null;
                }
            }
        }
    }

    public void initAccessTokenWithAkSk(Context context, String ak, String sk, OnResultListener<AccessToken> listener) {
//        Log.e("ocr-tag", "initAccessTokenWithAkSk方法仅供测试使用，上线请使用initAccessToken方法");
        this.authStatus = 2;
        this.ak = ak;
        this.sk = sk;
        this.init(context);
        AccessToken tokenFromCache = this.getByCache();
        if (tokenFromCache != null) {
            this.accessToken = tokenFromCache;
            listener.onResult(tokenFromCache);
            this.setLicense(tokenFromCache.getLic());
        } else {
            Throwable loadLibError = JniInterface.getLoadLibraryError();
            if (loadLibError != null) {
                SDKError e = new SDKError(LOAD_JNI_LIBRARY_ERROR, "Load jni so library error", loadLibError);
                listener.onError(e);
            } else {
                JniInterface jniInterface = new JniInterface();
                String hashSk = Util.md5(sk);
                byte[] buf = jniInterface.init(context, DeviceUtil.getDeviceInfo(context));
                String param = ak + ";" + hashSk + Base64.encodeToString(buf, 2);
                HttpUtil.getInstance().getAccessToken(listener, QUERY_TOKEN, param);
            }
        }
    }

    private synchronized boolean isTokenInvalid() {
        return null == this.accessToken || this.accessToken.hasExpired();
    }

    private void getToken(final OnResultListener listener) {
        if (this.isTokenInvalid()) {
            if (this.authStatus == 2) {
                this.initAccessTokenWithAkSk(this.context, this.ak, this.sk, new OnResultListener<AccessToken>() {
                    public void onResult(AccessToken result) {
                        OCR.this.setAccessToken(result);
                        listener.onResult(result);
                    }

                    public void onError(OCRError error) {
                        listener.onError(error);
                    }
                });
            }
        } else {
            listener.onResult(this.accessToken);
        }

    }

    public void recognizeIDCard(final IDCardParams param, final OnResultListener<IDCardResult> listener) {
        File imageFile = param.getImageFile();
        final Parser<IDCardResult> idCardResultParser = new IDCardResultParser(param.getIdCardSide());
        byte[] raw = FileUtil.reaFileFromSDcard(imageFile.getAbsolutePath());
        final String img = Base64Util.byte2String(raw);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                OCR.this.check(img);
                param.setKey(OCR.this.key);
                param.setData(OCR.this.data);
                param.setRsaaesencry(true);
                OCR.this.getToken(new OnResultListener<AccessToken>() {
                    public void onResult(AccessToken result) {
                        HttpUtil.getInstance().postidcard(OCR.this.urlAppendCommonParams(ID_CARD_URL), param, idCardResultParser, new OnResultListener<IDCardResult>() {
                            public void onResult(IDCardResult result) {
                                if (listener != null) {
                                    listener.onResult(result);
                                }

                            }

                            public void onError(OCRError error) {
                                if (listener != null) {
                                    listener.onError(error);
                                }

                            }
                        });
                    }

                    public void onError(OCRError error) {
                        listener.onError(error);
                    }
                });
            }
        });
    }

    public void recognizeBankCard(final BankCardParams params, final OnResultListener<BankCardResult> listener) {
        File imageFile = params.getImageFile();
        final Parser<BankCardResult> bankCardResultParser = new BankCardResultParser();
        byte[] raw = FileUtil.reaFileFromSDcard(imageFile.getAbsolutePath());
        final String img = Base64Util.byte2String(raw);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                OCR.this.check(img);
                params.setKey(OCR.this.key);
                params.setData(OCR.this.data);
                params.setRsaaesencry(true);

                OCR.this.getToken(new OnResultListener<AccessToken>() {
                    public void onResult(AccessToken result) {
                        HttpUtil.getInstance().postcard(OCR.this.urlAppendCommonParams(BANK_CARD_URL), params, bankCardResultParser, new OnResultListener<BankCardResult>() {
                            public void onResult(BankCardResult result) {
                                if (listener != null) {
                                    listener.onResult(result);
                                }

                            }

                            public void onError(OCRError error) {
                                if (listener != null) {
                                    listener.onError(error);
                                }

                            }
                        });
                    }

                    public void onError(OCRError error) {
                        listener.onError(error);
                    }
                });

            }
        });
    }

    private void check(String imgBase64) {
        try {
            String jsonContent = this.getContent(imgBase64);
            String randomAesSession = StringUtil.getRandomString(16);
            byte[] realSession = randomAesSession.getBytes();
            byte[] aesByte = AESUtil.generateEnKey(realSession);
            byte[] dataBytes = AESUtil.encrypt(jsonContent.getBytes(), aesByte);
            this.data = Base64Util.byte2String(dataBytes);
            AssetManager assetManager = ContextProvider.get().getContext().getAssets();
            String base64Key = FileUtil.readAssetFileUtf8String(assetManager, "publickey");
            byte[] keyBytes = RSAUtil.rsaEncrypt(aesByte, base64Key);
            this.key = Base64Util.byte2String(keyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getContent(String img64) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("image", img64);
            jsonObject.put("platform", "android");
            String packageName = ContextProvider.get().getApplication().getPackageName();
            jsonObject.put("package_name", packageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("access_token=").append(this.getAccessToken().getAccessToken());
        sb.append("&aipSdk=Android");
        sb.append("&aipSdkVersion=").append("2_0_1");
        sb.append("&aipDevid=").append(DeviceUtil.getDeviceId(this.context));
        return sb.toString();
    }
}

package com.baidu.ocr.sdk.tool;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class ContextProvider {
    @SuppressLint({"StaticFieldLeak"})
    private static volatile ContextProvider instance;
    private Context mContext;

    private ContextProvider(Context context) {
        this.mContext = context;
    }

    public static ContextProvider get() {
        if (instance == null) {
            Class var0 = ContextProvider.class;
            synchronized(ContextProvider.class) {
                if (instance == null) {
                    Context context = ApplicationContextProvider.context;
                    if (context == null) {
                        throw new IllegalStateException("context == null");
                    }

                    instance = new ContextProvider(context);
                }
            }
        }

        return instance;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Application getApplication() {
        return (Application)this.mContext.getApplicationContext();
    }
}

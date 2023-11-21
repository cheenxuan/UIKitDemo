package com.tech.android.base.log;

import android.util.Log;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: xuan
 * Created on 2021/3/18 15:01.
 * <p>
 * Describe:
 */
public class LogType {
    
    @IntDef({V,D,I,W,E,A})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TYPE{
        
    }
    
    public final static int V = Log.VERBOSE;
    public final static int D = Log.DEBUG;
    public final static int I = Log.INFO;
    public final static int W = Log.WARN;
    public final static int E = Log.ERROR;
    public final static int A = Log.ASSERT;
}

package com.tech.android.base.log;

import androidx.annotation.NonNull;

/**
 * Author: xuan
 * Created on 2021/3/18 15:56.
 * <p>
 * Describe:
 */
public interface LogPrinter {
    void print(@NonNull LogConfig config, int level, String tag, String printString);
}

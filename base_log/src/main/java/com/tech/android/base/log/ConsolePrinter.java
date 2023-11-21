package com.tech.android.base.log;

import android.util.Log;

import androidx.annotation.NonNull;




/**
 * Author: xuan
 * Created on 2021/3/18 16:13.
 * <p>
 * Describe:
 */
public class ConsolePrinter implements LogPrinter {
    @Override
    public void print(@NonNull LogConfig config, int level, String tag, String printString) {
        int len = printString.length();
        int countOfSub = len / LogConfig.MAX_LEN;
        int index = 0;
        if(countOfSub > 0){
            for (int i = 0; i < countOfSub; i++) {
                Log.println(level, tag, printString.substring(index, index + LogConfig.MAX_LEN));
                index += LogConfig.MAX_LEN;
            }
        }

        if (index != len) {
            Log.println(level, tag, printString.substring(index, len));
        }
    }
}

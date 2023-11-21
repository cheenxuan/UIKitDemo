package com.tech.android.base.log;

/**
 * Author: xuan
 * Created on 2021/3/18 15:59.
 * <p>
 * Describe:
 */
public class ThreadFormatter implements LogFormatter<Thread> {
    
    @Override
    public String format(Thread thread) {
        return "Thread : " + thread.getName();
    }
}

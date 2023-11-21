package com.tech.android.base.log;

/**
 * Author: xuan
 * Created on 2021/3/18 15:57.
 * <p>
 * Describe:
 */
public interface LogFormatter<T> {
    String format(T data);
}

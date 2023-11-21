package com.tech.android.base.log;



/**
 * Author: xuan
 * Created on 2021/3/18 15:10.
 * <p>
 * Describe:
 */
public abstract class LogConfig {
    static int MAX_LEN = 1024;
    static StackTraceFormatter STACK_TRACE_FORMATTER = new StackTraceFormatter();
    static ThreadFormatter THREAD_FORMATTER = new ThreadFormatter();

    public JsonParser injectJsonParser() {
        return null;
    }

    public String getGlobalTag() {
        return "VLog";
    }

    public boolean enable() {
        return true;
    }

    public boolean includeThread() {
        return false;
    }

    public int stackTraceDepth() {
        return 5;
    }

    public LogPrinter[] printers() {
        return null;
    }

    public interface JsonParser {
        String toJson(Object src);
    }
}

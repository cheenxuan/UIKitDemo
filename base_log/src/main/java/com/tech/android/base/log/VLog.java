package com.tech.android.base.log;


import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * Author: xuan
 * Created on 2021/3/18 14:59.
 * <p>
 * Describe:
 * <p>
 * 1.打印堆栈信息
 * 2.File输出
 * 3.模拟控制台
 */
public class VLog {
    private static final String V_LOG_PACKAGE;

    static {
        String className = VLog.class.getName();
        V_LOG_PACKAGE = className.substring(0, className.lastIndexOf(".") + 1);
    }

    public static void v(Object... contents) {
        log(LogType.V, contents);
    }

    public static void vt(String tag, Object... contents) {
        log(LogType.V, tag, contents);
    }

    public static void d(Object... contents) {
        log(LogType.D, contents);
    }

    public static void dt(String tag, Object... contents) {
        log(LogType.D, tag, contents);
    }

    public static void i(Object... contents) {
        log(LogType.I, contents);
    }

    public static void it(String tag, Object... contents) {
        log(LogType.I, tag, contents);
    }

    public static void w(Object... contents) {
        log(LogType.W, contents);
    }

    public static void wt(String tag, Object... contents) {
        log(LogType.W, tag, contents);
    }

    public static void e(Object... contents) {
        log(LogType.E, contents);
    }

    public static void et(String tag, Object... contents) {
        log(LogType.E, tag, contents);
    }

    public static void a(Object... contents) {
        log(LogType.A, contents);
    }

    public static void at(String tag, Object... contents) {
        log(LogType.A, tag, contents);
    }

    public static void log(@LogType.TYPE int type, Object... contents) {
        log(type, LogManager.getInstance().getConfig().getGlobalTag(), contents);
    }

    public static void log(@LogType.TYPE int type, @NonNull String tag, Object... contents) {
        log(LogManager.getInstance().getConfig(), type, tag, contents);
    }

    public static void log(@NonNull LogConfig config, @LogType.TYPE int type, @NonNull String tag, Object... contents) {
        if (!config.enable()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (config.includeThread()) {
            String threadInfo = LogConfig.THREAD_FORMATTER.format(Thread.currentThread());
            sb.append(threadInfo).append("\n");
        }

        if (config.stackTraceDepth() > 0) {
            String stackTrace = LogConfig.STACK_TRACE_FORMATTER.format(
//                    new Throwable().getStackTrace()
                    StackTraceUtil.getCropedRealStackTrace(new Throwable().getStackTrace(), V_LOG_PACKAGE, config.stackTraceDepth())
            );
            sb.append(stackTrace).append("\n");
        }

        String body = parseBody(contents, config);
        sb.append(body);

        List<LogPrinter> printers = config.printers() != null ? Arrays.asList(config.printers()) : LogManager.getInstance().getPrinters();
        if (printers == null)
            return;

        //打印LOG
        for (LogPrinter printer : printers) {
            printer.print(config, type, tag, sb.toString());
        }
    }

    private static String parseBody(@NonNull Object[] contents, @NonNull LogConfig config) {

        if (config.injectJsonParser() != null) {
            return config.injectJsonParser().toJson(contents);
        }

        StringBuilder sb = new StringBuilder();
        for (Object o : contents) {
            sb.append(o.toString()).append(";");
        }
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}

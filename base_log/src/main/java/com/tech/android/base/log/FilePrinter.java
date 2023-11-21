package com.tech.android.base.log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: xuan
 * Created on 2021/3/19 16:52.
 * <p>
 * Describe:
 * 1、BlockingQueue的使用，防止频繁的创建线程
 * 2、线程同步
 * 3、文件操作，BufferWriter的应用
 */
public class FilePrinter implements LogPrinter {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private final String logPath;
    private final long retentionTime;
    private volatile PrintWorker worker;
    private LogWriter writer;
    private static FilePrinter instance;

    public static synchronized FilePrinter getInstance(String logPath, long retentionTime) {
        if (instance == null) {
            instance = new FilePrinter(logPath, retentionTime);
        }
        return instance;
    }

    private FilePrinter(String logPath, long retentionTime) {
        this.logPath = logPath;
        this.retentionTime = retentionTime;
        this.writer = new LogWriter();
        this.worker = new PrintWorker();
        cleanExpiredLog();
    }

    @Override
    public void print(@NonNull LogConfig config, int level, String tag, String printString) {
        long timeMillis = System.currentTimeMillis();
        if (!worker.isRunning()) {
            worker.start();
        }
        worker.put(new LogMo(timeMillis,level,tag,printString));
    }

    private void doPrint(LogMo log) {
        String lastFileName = writer.getPreFileName();
        if (lastFileName == null) {
            String newFileName = geneFileName();

            if (writer.isReady()) {
                writer.close();
            }

            if (!writer.ready(newFileName)) {
                return;
            }
        }
        writer.append(log.flattenedlog());
    }
    
    private void cleanExpiredLog() {
        if (retentionTime <= 0) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        File logDir = new File(logPath);
        File[] files = logDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (currentTimeMillis - file.lastModified() > retentionTime) {
                file.delete();
            }
        }
    }
    
    private String geneFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    private class PrintWorker implements Runnable {
        private BlockingQueue<LogMo> logs = new LinkedBlockingDeque<>();
        private volatile boolean running;

        /**
         * 将log放入打印队列
         *
         * @param log 要被打印的log
         */
        void put(LogMo log) {
            try {
                logs.put(log);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean isRunning() {
            synchronized (this) {
                return running;
            }
        }

        void start() {
            synchronized (this) {
                EXECUTOR.execute(this);
                running = true;
            }
        }

        @Override
        public void run() {
            LogMo log;
            try {
                while (true) {
                    log = logs.take();
                    doPrint(log);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                synchronized (this) {
                    running = false;
                }
            }
        }
    }

    private class LogWriter {
        private String preFileName;
        private File logFile;
        private BufferedWriter bufferedWriter;
        
        boolean isReady() {
            return bufferedWriter != null;
        }
        
        String getPreFileName() {
            return preFileName;
        }

        /**
         * log写入前的准备操作
         * @param newFileName 要保存的log文件名
         * @return true 表示准备就绪
         */
        boolean ready(String newFileName) {
            preFileName = newFileName;
            logFile = new File(logPath, newFileName);

            if (!logFile.exists()) {
                try {
                    File parent = logFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    preFileName = null;
                    logFile = null;
                    return false;
                }
            }

            try {
                bufferedWriter = new BufferedWriter(new FileWriter(logFile,true));
            } catch (IOException e) {
                e.printStackTrace();
                preFileName = null;
                logFile = null;
                return false;
            }
            return true;
        }
        
        boolean close() {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    bufferedWriter = null;
                    preFileName = null;
                    logFile = null;
                }
            }
            return true;
        }

        void append(String flattendLog) {
            try {
                bufferedWriter.write(flattendLog);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}

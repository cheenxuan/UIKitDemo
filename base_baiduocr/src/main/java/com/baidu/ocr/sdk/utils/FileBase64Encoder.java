package com.baidu.ocr.sdk.utils;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class FileBase64Encoder {
    private InputStream inputStream;
    private byte[] buffer = new byte[24576];

    public FileBase64Encoder() {
    }

    public void setInputFile(File file) throws FileNotFoundException {
        this.inputStream = new FileInputStream(file);
    }

    public byte[] encode() {
        int readNumber;
        try {
            readNumber = this.inputStream.read(this.buffer);
            if (readNumber == -1) {
                this.closeInputStream();
                return null;
            }
        } catch (IOException e) {
            this.closeInputStream();
            e.printStackTrace();
            return null;
        }

        return Base64.encode(this.buffer, 0, readNumber, Base64.NO_WRAP);
    }

    private void closeInputStream() {
        try {
            this.inputStream.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        } finally {
            this.inputStream = null;
        }

    }
}

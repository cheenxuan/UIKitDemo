package com.baidu.ocr.sdk.tool;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class FileUtil {
    public FileUtil() {
    }

    public static String readAssetFileUtf8String(AssetManager assetManager, String filename) {
        byte[] bytes = new byte[0];

        try {
            bytes = readAssetFileContent(assetManager, filename);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return new String(bytes, Charset.forName("UTF-8"));
    }

    public static String[] listFile(AssetManager assetManager, String fileDir) {
        String[] result = new String[0];

        try {
            result = assetManager.list(fileDir);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return result;
    }

    public static byte[] readAssetFileContent(AssetManager assetManager, String filename) throws IOException {
        Log.i("FileUtil", " try to read asset file :" + filename);
        InputStream is = assetManager.open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        int realSize = is.read(buffer);
        if (realSize != size) {
            throw new IOException("realSize is not equal to size: " + realSize + " : " + size);
        } else {
            is.close();
            return buffer;
        }
    }

    public static void writeTxtToFile(String path, String str) {
        String strContent = str;
        File file = null;

        try {
            file = new File(path);
            File parentfile = file.getParentFile();
            if (!parentfile.exists()) {
                parentfile.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            } else {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
            }

            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public static String readFromFile(String path) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();

        try {
            in = new FileInputStream(new File(path));
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException var13) {
            var13.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException var12) {
                    var12.printStackTrace();
                }
            }

        }

        return content.toString();
    }

    public static byte[] readLargeFile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/baidu/12M.jpg";
        FileInputStream in = null;
        BufferedInputStream bufferedInputStream = null;
        byte[] result = null;

        try {
            in = new FileInputStream(new File(path));
            bufferedInputStream = new BufferedInputStream(in);
            result = new byte[in.available()];
            int byteSize = bufferedInputStream.read(result);
            Log.d("TAG", "readLargeFile: byteSize" + byteSize);
        } catch (IOException var17) {
            var17.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException var16) {
                    var16.printStackTrace();
                }
            }

            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException var15) {
                    var15.printStackTrace();
                }
            }

        }

        return result;
    }

    public static byte[] reaFileFromSDcard(String path) {
        FileInputStream in = null;
        BufferedInputStream bufferedInputStream = null;
        byte[] result = null;

        try {
            in = new FileInputStream(new File(path));
            bufferedInputStream = new BufferedInputStream(in);
            result = new byte[in.available()];
            int byteSize = bufferedInputStream.read(result);
            Log.d("TAG", "reaFileFromSDcard: byteSize" + byteSize);
        } catch (IOException var17) {
            var17.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return result;
    }
}

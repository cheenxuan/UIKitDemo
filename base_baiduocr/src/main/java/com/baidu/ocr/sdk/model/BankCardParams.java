package com.baidu.ocr.sdk.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class BankCardParams implements RequestParams {
    public static final String ID_CARD_SIDE_FRONT = "front";
    public static final String ID_CARD_SIDE_BACK = "back";
    private String key;
    private String data;
    private boolean rsaaesencry;
    private File imageFile;
    private boolean detectDirection;
    private String idCardSide;
    private boolean detectRisk;

    public BankCardParams() {
    }

    public String getkey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean getRsaaesencry() {
        return this.rsaaesencry;
    }

    public void setRsaaesencry(boolean rsaaesencry) {
        this.rsaaesencry = rsaaesencry;
    }

    public File getImageFile() {
        return this.imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public String getIdCardSide() {
        return this.idCardSide;
    }

    public void setIdCardSide(String idCardSide) {
        this.idCardSide = idCardSide;
    }

    public Map<String, File> getFileParams() {
        Map<String, File> fileMap = new HashMap();
        fileMap.put("image", this.imageFile);
        return fileMap;
    }

    public Map<String, String> getStringParams() {
        Map<String, String> stringMap = new HashMap();
        return stringMap;
    }

    public Map<String, String> getParams(IDCardParams params) {
        Map<String, String> stringMap = new HashMap();
        stringMap.put("data", params.getData());
        stringMap.put("key", params.getkey());
        stringMap.put("RSAAESEncry", String.valueOf(true));
        stringMap.put("id_card_side", this.idCardSide);
        return stringMap;
    }

    public Map<String, String> getParams(BankCardParams params) {
        Map<String, String> stringMap = new HashMap();
        stringMap.put("data", params.getData());
        stringMap.put("key", params.getkey());
        stringMap.put("RSAAESEncry", String.valueOf(true));
        return stringMap;
    }
}
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
public class IDCardParams implements RequestParams {
    public static final String ID_CARD_SIDE_FRONT = "front";
    public static final String ID_CARD_SIDE_BACK = "back";
    private boolean detectDirection;
    private boolean detectRisk;
    private boolean detectQuality;
    private boolean detectCard;
    private String idCardSide;
    private File imageFile;
    private int imageQuality = 20;
    private String key;
    private String data;
    private boolean rsaaesencry;

    public IDCardParams() {
    }

    public boolean isDetectDirection() {
        return this.detectDirection;
    }

    public void setDetectDirection(boolean detectDirection) {
        this.detectDirection = detectDirection;
    }

    public String getIdCardSide() {
        return this.idCardSide;
    }

    public void setIdCardSide(String idCardSide) {
        this.idCardSide = idCardSide;
    }

    public File getImageFile() {
        return this.imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public void setDetectRisk(boolean detectRisk) {
        this.detectRisk = detectRisk;
    }
    
    public boolean getDetectRisk() {
        return this.detectRisk;
    }

    public void setDetectQuality(boolean detectQuality) {
        this.detectQuality = detectQuality;
    }

    public boolean getDetectQuality() {
        return this.detectQuality;
    }

    public void setDetectCard(boolean detectCard) {
        this.detectCard = detectCard;
    }

    public boolean getDetectCard() {
        return this.detectCard;
    }

    public int getImageQuality() {
        return this.imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
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

    public Map<String, File> getFileParams() {
        Map<String, File> fileMap = new HashMap();
        fileMap.put("image", this.imageFile);
        return fileMap;
    }

    public Map<String, String> getStringParams() {
        Map<String, String> stringMap = new HashMap();
        stringMap.put("id_card_side", this.idCardSide);
        if (this.detectDirection) {
            stringMap.put("detect_direction", "true");
        } else {
            stringMap.put("detect_direction", "false");
        }

        if (this.detectRisk) {
            stringMap.put("detect_risk", "true");
        } else {
            stringMap.put("detect_risk", "false");
        }

        return stringMap;
    }

    public Map<String, String> getParams(IDCardParams params) {
        Map<String, String> stringMap = new HashMap();
        stringMap.put("data", params.getData());
        stringMap.put("key", params.getkey());
        stringMap.put("RSAAESEncry", String.valueOf(true));
        stringMap.put("id_card_side", this.idCardSide);
        if (this.detectDirection) {
            stringMap.put("detect_direction", "true");
        } else {
            stringMap.put("detect_direction", "false");
        }

        if (this.detectRisk) {
            stringMap.put("detect_risk", "true");
        } else {
            stringMap.put("detect_risk", "false");
        }

        if (this.detectQuality) {
            stringMap.put("detect_quality", "true");
        } else {
            stringMap.put("detect_quality", "false");
        }

        if (this.detectCard) {
            stringMap.put("detect_card", "true");
        } else {
            stringMap.put("detect_card", "false");
        }

        return stringMap;
    }

    public Map<String, String> getParams(BankCardParams params) {
        Map<String, String> stringMap = new HashMap();
        stringMap.put("data", params.getData());
        stringMap.put("key", params.getkey());
        stringMap.put("RSAAESEncry", String.valueOf(true));
        stringMap.put("id_card_side", this.idCardSide);
        return stringMap;
    }
}
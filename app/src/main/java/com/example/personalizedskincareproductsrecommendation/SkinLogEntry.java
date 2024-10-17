package com.example.personalizedskincareproductsrecommendation;

import java.io.Serializable;
import java.util.Map;

public class SkinLogEntry {
    private String logId;
    private String timestamp;
    private String userId;
    private String leftSelfieUrl;
    private String rightSelfieUrl;
    private String frontSelfieUrl;
    private String neckSelfieUrl;

    public SkinLogEntry(String logId, String timestamp, String userId, String leftSelfieUrl, String rightSelfieUrl, String frontSelfieUrl, String neckSelfieUrl) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.leftSelfieUrl = leftSelfieUrl;
        this.rightSelfieUrl = rightSelfieUrl;
        this.frontSelfieUrl = frontSelfieUrl;
        this.neckSelfieUrl = neckSelfieUrl;
    }

    public String getLogId() {
        return logId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getLeftSelfieUrl() {
        return leftSelfieUrl;
    }

    public String getRightSelfieUrl() {
        return rightSelfieUrl;
    }

    public String getFrontSelfieUrl() {
        return frontSelfieUrl;
    }

    public String getNeckSelfieUrl() {
        return neckSelfieUrl;
    }


}

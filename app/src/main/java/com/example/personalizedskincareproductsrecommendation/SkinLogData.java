package com.example.personalizedskincareproductsrecommendation;

import java.util.List;

public class SkinLogData {
    private String logId;
    private String userId;
    private String timestamp;
    private Selfies selfies;

    public SkinLogData() {
        // Default constructor required for calls to DataSnapshot.getValue(SkinLogData.class)
    }

    public SkinLogData(String logId, String userId, String timestamp, Selfies selfies) {
        this.logId = logId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.selfies = selfies;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Selfies getSelfies() {
        return selfies;
    }

    public void setSelfies(Selfies selfies) {
        this.selfies = selfies;
    }

    public static class Selfies {
        private String left;
        private String right;
        private String front;
        private String neck;

        public Selfies() {
            // Default constructor
        }

        public Selfies(String left, String right, String front, String neck) {
            this.left = left;
            this.right = right;
            this.front = front;
            this.neck = neck;
        }

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

        public String getFront() {
            return front;
        }

        public void setFront(String front) {
            this.front = front;
        }

        public String getNeck() {
            return neck;
        }

        public void setNeck(String neck) {
            this.neck = neck;
        }
    }
}

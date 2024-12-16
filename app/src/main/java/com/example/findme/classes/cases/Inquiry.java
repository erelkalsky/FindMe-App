package com.example.findme.classes.cases;

import java.io.Serializable;

public class Inquiry implements Serializable {
    private String investigatedName;
    private String userId;
    private String inquirySummary;
    private long uploadTime;

    public Inquiry() {

    }

    public Inquiry(String userId, String investigatedName, String inquirySummary, long uploadTime) {
        this.userId = userId;
        this.investigatedName = investigatedName;
        this.inquirySummary = inquirySummary;
        this.uploadTime = uploadTime;
    }

    public String getInvestigatedName() {
        return investigatedName;
    }

    public String getUserId() {
        return userId;
    }

    public String getInquirySummary() {
        return inquirySummary;
    }

    public long getUploadTime() {
        return uploadTime;
    }
}

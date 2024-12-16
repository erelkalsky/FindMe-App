package com.example.findme.classes.cases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Case implements Serializable {
    private String caseId;
    private boolean active;
    private String transcript;
    private MissingPerson missingPerson;
    private List<Inquiry> inquiries;
    private List<String> usersInCase;

    public Case() {

    }

    public Case(String caseId, String transcript, MissingPerson missingPerson) {
        this.caseId = caseId;
        this.transcript = transcript;
        this.active = true;
        this.missingPerson = missingPerson;
        this.inquiries = new ArrayList<>();
        this.usersInCase = new ArrayList<>();
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public boolean isActive() {
        return active;
    }

    public String getTranscript() {
        return transcript;
    }

    public MissingPerson getMissingPerson() {
        return missingPerson;
    }

    public List<Inquiry> getInquiries() {
        return inquiries;
    }

    public void setInquiries(List<Inquiry> inquiries) {
        this.inquiries = inquiries;
    }

    public List<String> getUsersInCase() {
        return usersInCase;
    }

    public void removeUserFromCase(String userId) {
        if(isUserInCase(userId)) {
            usersInCase.remove(userId);
        }
    }

    public boolean isUserInCase(String userId) {
        return this.usersInCase != null && this.usersInCase.contains(userId);
    }
}

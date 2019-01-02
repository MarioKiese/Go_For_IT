package de.goforittechnologies.go_for_it.storage;

public class Request {

    private String id;
    private int stepTarget;
    private String sourceUserID;
    private String targetUserID;
    private String challengeID;
    private String status;

    public Request() {
    }

    public Request(String id, int stepTarget, String sourceUserID, String targetUserID, String challengeID, String status) {
        this.id = id; //TODO: Check if necessary and clear all traces
        this.stepTarget = stepTarget;
        this.sourceUserID = sourceUserID;
        this.targetUserID = targetUserID;
        this.challengeID = challengeID;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStepTarget() {
        return stepTarget;
    }

    public void setStepTarget(int stepTarget) {
        this.stepTarget = stepTarget;
    }

    public String getSourceUserID() {
        return sourceUserID;
    }

    public void setSourceUserID(String sourceUserID) {
        this.sourceUserID = sourceUserID;
    }

    public String getTargetUserID() {
        return targetUserID;
    }

    public void setTargetUserID(String targetUserID) {
        this.targetUserID = targetUserID;
    }

    public String getChallengeID() {
        return challengeID;
    }

    public void setChallengeID(String challengeID) {
        this.challengeID = challengeID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

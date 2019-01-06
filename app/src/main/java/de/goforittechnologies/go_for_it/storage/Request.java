package de.goforittechnologies.go_for_it.storage;

/**
 * @author  Mario Kiese and Tom Hammerbacher
 * @version 0.8.
 *
 * class to represent and temporary store a challenge-request
 * contain getter ad setter methods
 */

public class Request {

    private String id;
    private int stepTarget;
    private String sourceUserID;
    private String targetUserID;
    private String sourceUserName;
    private String targetUserName;
    private String sourceUserImage;
    private String targetUserImage;
    private String challengeID;
    private String status;

    public Request() {
    }

    public Request(String id, int stepTarget, String sourceUserID, String targetUserID, String sourceUserName, String targetUserName, String sourceUserImage, String targetUserImage, String challengeID, String status) {
        this.id = id; //TODO: Check if necessary and clear all traces
        this.stepTarget = stepTarget;
        this.sourceUserID = sourceUserID;
        this.targetUserID = targetUserID;
        this.sourceUserName = sourceUserName;
        this.targetUserName = targetUserName;
        this.sourceUserImage = sourceUserImage;
        this.targetUserImage = targetUserImage;
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

    public String getSourceUserName() {
        return sourceUserName;
    }

    public void setSourceUserName(String sourceUserName) {
        this.sourceUserName = sourceUserName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getSourceUserImage() {
        return sourceUserImage;
    }

    public void setSourceUserImage(String sourceUserImage) {
        this.sourceUserImage = sourceUserImage;
    }

    public String getTargetUserImage() {
        return targetUserImage;
    }

    public void setTargetUserImage(String targetUserImage) {
        this.targetUserImage = targetUserImage;
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

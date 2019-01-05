package de.goforittechnologies.go_for_it.storage;

public class Challenge {

    private String id;
    private String requestID;
    private int stepTarget;
    private int stepsUser1;
    private int stepsUser2;
    private User user1;
    private User user2;
    private User winner;
    private String status;

    public Challenge(){}

    public Challenge(String id, String requestID, int stepTarget, User user1, User user2, String status) {
        this.id = id;
        this.requestID = requestID;
        this.stepTarget = stepTarget;
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
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

    public int getStepsUser1() {
        return stepsUser1;
    }

    public void setStepsUser1(int stepsUser1) {
        this.stepsUser1 = stepsUser1;
    }

    public int getStepsUser2() {
        return stepsUser2;
    }

    public void setStepsUser2(int stepsUser2) {
        this.stepsUser2 = stepsUser2;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

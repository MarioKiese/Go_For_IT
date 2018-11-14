package de.goforittechnologies.go_for_it;

import java.net.URI;
import java.util.ArrayList;

public class User {

    private String firstName;
    private String lastName;
    private String userName;
    private URI profileImage;
    private ArrayList<User> friendList;
    private ArrayList<String> challengeList;
    private float daySteps;
    private float maxSteps;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public URI getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(URI profileImage) {
        this.profileImage = profileImage;
    }

    public ArrayList<User> getFriendList() {
        return friendList;
    }

    public void addToFriendList(User friend) {
        this.friendList.add(friend);
    }

    public void deleteToFriendList(User friend) {
        this.friendList.remove(friend);
    }

    public ArrayList<String> getChallengeList() {
        return challengeList;
    }

    public void addChallenge(String challenge) {
        this.challengeList.add(challenge);
    }

    public void abortChallenge(String challenge) {
        this.challengeList.remove(challenge);
    }

    public float getDaySteps() {
        return daySteps;
    }

    public void setDaySteps(float daySteps) {
        this.daySteps = daySteps;
    }

    public float getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(float maxSteps) {
        this.maxSteps = maxSteps;
    }

}

package com.example.fyp_dummy_group;

public class UserProfile {
    public String userName, userEmail, userAge, userGender, userHeight, userWeight, userBirthday;

    public UserProfile(){

    }

    public UserProfile(String userName, String userEmail, String userAge, String userGender, String userHeight, String userWeight, String userBirthday)
    {
        this.userName=userName;
        this.userEmail=userEmail;
        this.userAge=userAge;
        this.userGender=userGender;
        this.userHeight=userHeight;
        this.userWeight=userWeight;
        this.userBirthday=userBirthday;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserHeight() {
        return userHeight;
    }

    public void setUserHeight(String userHeight) {
        this.userHeight = userHeight;
    }

    public String getUserWeight() {
        return userWeight;
    }

    public void setUserWeight(String userWeight) {
        this.userWeight = userWeight;
    }

    public String getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(String userBirthday) {
        this.userBirthday = userBirthday;
    }
}

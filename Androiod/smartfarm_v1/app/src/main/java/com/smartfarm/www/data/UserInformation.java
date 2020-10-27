package com.smartfarm.www.data;

public class UserInformation {

    private static UserInformation userInfo=null;

    private String userName;
    private String userNickName;
    private String userEmail;
    private String userID;
    private String userPwd;
    private String userLocation;
    private int userNo;
    private int userLoginCheck;

    private UserInformation(){
        userName = null ;
        userNickName = null ;
        userEmail = null ;
        userID = null ;
        userPwd = null ;
        userLocation = null ;
        userNo = 0;
        userLoginCheck = 0;
    }

    public static UserInformation getUserInformation(){
        if(userInfo==null){
            userInfo = new UserInformation();
        }
        return userInfo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public int getUserNo() {
        return userNo;
    }

    public void setUserNo(int userNo) { this.userNo = userNo; }

    public int getUserLoginCheck() { return userLoginCheck; }

    public void setUserLoginCheck(int userLoginCheck) { this.userLoginCheck = userLoginCheck; }
}

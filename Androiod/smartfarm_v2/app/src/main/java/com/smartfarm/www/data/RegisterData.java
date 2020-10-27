package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class RegisterData {
    @SerializedName("userName")
    private String userName;

    @SerializedName("userNickName")
    private String userNickName;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("userID")
    private String userID;

    @SerializedName("userPwd")
    private String userPwd;

    @SerializedName("userLocation")
    private String userLocation;

    //PRIMARY KEY
    @SerializedName("userNo")
    private int userNo;

    public RegisterData(String userName, String userNickName, String userEmail, String userID, String userPwd, String userLocation) {
        this.userName = userName;
        this.userNickName = userNickName;
        this.userEmail = userEmail;
        this.userID = userID;
        this.userPwd = userPwd;
        this.userLocation = userLocation;
    }

    //CHANGEMYINFORMATION
    public RegisterData(String userName, String userNickName, String userEmail, String userID, String userPwd, String userLocation, int userNo) {
        this.userName = userName;
        this.userNickName = userNickName;
        this.userEmail = userEmail;
        this.userID = userID;
        this.userPwd = userPwd;
        this.userLocation = userLocation;
        this.userNo = userNo;
    }
}

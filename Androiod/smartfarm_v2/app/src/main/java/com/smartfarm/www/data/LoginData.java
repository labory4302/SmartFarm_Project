package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class LoginData {
    @SerializedName("userName")
    String userName;

    @SerializedName("userNickName")
    String userNickName;

    @SerializedName("userEmail")
    String userEmail;

    @SerializedName("userID")
    String userID;

    @SerializedName("userPwd")
    String userPwd;

    @SerializedName("userLocation")
    String userLocation;

    public LoginData(String userID, String userPwd) {
        this.userID = userID;
        this.userPwd = userPwd;
    }
}

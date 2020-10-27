package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class MypageData {
    @SerializedName("userNickName")
    private String userNickName;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("userLocation")
    private String userLocation;

    public MypageData(String userNickName, String userEmail, String userLocation) {
        this.userNickName = userNickName;
        this.userEmail = userEmail;
        this.userLocation = userLocation;
    }
}

package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class AccessData {

    @SerializedName("userLoginCheck")
    int userLoginCheck;

    @SerializedName("userNo")
    int userNo;

    public AccessData(int userLoginCheck, int userNo) {
        this.userLoginCheck = userLoginCheck;
        this.userNo = userNo;
    }
}

package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class AccessResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("userLoginCheck")
    private int userLoginCheck;

    @SerializedName("userNo")
    private int userNo;

    public int getCode() { return code; }

    public void setCode(int code) { this.code = code; }

    public int getUserLoginCheck() { return userLoginCheck; }

    public void setUserLoginCheck(int userLoginCheck) { this.userLoginCheck = userLoginCheck; }

    public int getUserNo() { return userNo; }

    public void setUserNo(int userNo) { this.userNo = userNo; }


}

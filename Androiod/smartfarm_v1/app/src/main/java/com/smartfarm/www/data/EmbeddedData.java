package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

import okhttp3.internal.Version;

public class EmbeddedData {
    @SerializedName("code")
    int code;

    @SerializedName("userNo")
    int userNo;

    //현재 계측한 센서값들
    @SerializedName("recentHumi")
    int recentHumi;

    @SerializedName("Temp")
    int Temp;

    //사전에 세팅한 값들
    @SerializedName("Humi")
    int Humi;

    //현재 아두이노의 상태
    @SerializedName("automode")
    int automode;

    @SerializedName("pump")
    int pump;

    @SerializedName("fan")
    int fan;

    @SerializedName("led")
    int led;

    //세팅해둔 객체감지, 화재감지 상태
    @SerializedName("fireDetection")
    int fireDetection;

    @SerializedName("objectDetection")
    int objectDetection;

    public EmbeddedData(int userNo, int fireDetection, int objectDetection) {
        this.userNo = userNo;
        this.fireDetection = fireDetection;
        this.objectDetection = objectDetection;
    }

    public EmbeddedData(int userNo) {
        this.userNo = userNo;
    }
}

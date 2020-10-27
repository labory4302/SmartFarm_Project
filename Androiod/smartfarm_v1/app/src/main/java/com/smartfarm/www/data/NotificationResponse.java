package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class NotificationResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("notificationTitle")
    private String notificationTitle;

    @SerializedName("notificationContents")
    private String notificationContents;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationContents() {
        return notificationContents;
    }

    public void setNotificationContents(String notificationContents) {
        this.notificationContents = notificationContents;
    }
}

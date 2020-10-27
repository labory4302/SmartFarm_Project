package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class NotificationData {
    @SerializedName("notificationTitle")
    String notificationTitle;

    @SerializedName("notificationContents")
    String notificationContents;

    public NotificationData(String notificationTitle, String notificationContents) {
        this.notificationTitle = notificationTitle;
        this.notificationContents = notificationContents;
    }
}

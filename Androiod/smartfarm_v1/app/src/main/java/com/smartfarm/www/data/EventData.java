package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class EventData {
    @SerializedName("eventTitle")
    String eventTitle;

    @SerializedName("eventContents")
    String eventContents;

    public EventData(String eventTitle, String eventContents) {
        this.eventTitle = eventTitle;
        this.eventContents = eventContents;
    }
}

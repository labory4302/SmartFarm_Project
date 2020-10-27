package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class EventResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("eventTitle")
    private String eventTitle;

    @SerializedName("eventContents")
    private String eventContents;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventContents() {
        return eventContents;
    }

    public void setEventContents(String eventContents) {
        this.eventContents = eventContents;
    }
}

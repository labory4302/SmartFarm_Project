package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

public class VersionResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("version")
    private int version;

    @SerializedName("versionInformation")
    private String versionInformation;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getVersion() {
        return version;
    }

    public String getVersionInformation() {
        return versionInformation;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setVersionInformation(String versionInformation) {
        this.versionInformation = versionInformation;
    }
}

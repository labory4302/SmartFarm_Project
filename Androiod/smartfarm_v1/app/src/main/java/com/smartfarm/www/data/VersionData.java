package com.smartfarm.www.data;

import com.google.gson.annotations.SerializedName;

import okhttp3.internal.Version;

public class VersionData {
    @SerializedName("version")
    int version;

    @SerializedName("versionInformation")
    String versionInformation;

    public VersionData(int version, String versionInformation) {
        this.version = version;
        this.versionInformation = versionInformation;
    }
}

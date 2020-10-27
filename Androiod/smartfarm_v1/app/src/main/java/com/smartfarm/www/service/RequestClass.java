package com.smartfarm.www.service;



public class RequestClass {
    String userID; // S3에서 찾아갈 폴더명
    String imgName; // S3에서 사용해야 하는 이미지 이름

    public RequestClass(String userID, String imgName) {
        this.userID = userID; this.imgName = imgName;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public RequestClass() {
    }
}
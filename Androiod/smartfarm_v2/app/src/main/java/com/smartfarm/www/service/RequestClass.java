package com.smartfarm.www.service;



public class RequestClass {
    String imgName; // S3에서 사용해야 하는 이미지 이름

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public RequestClass(String imgName) {
        this.imgName = imgName;
    }

    public RequestClass() {
    }
}
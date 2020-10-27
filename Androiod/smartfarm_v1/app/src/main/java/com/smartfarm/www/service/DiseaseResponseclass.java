package com.smartfarm.www.service;

public class DiseaseResponseclass {
    public DiseaseResponseclass(){

    }

    private String statusCode;  // 전송 코드 (원활히 통신 됬는지 확인용)
    private int re_vegetable, re_disease;   // 결과 값

    public int getRe_vegetable() {
        return re_vegetable;
    }

    public void setRe_vegetable(int re_vegetable) {
        this.re_vegetable = re_vegetable;
    }

    public int getRe_disease() {
        return re_disease;
    }

    public void setRe_disease(int re_disease) {
        this.re_disease = re_disease;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }


}

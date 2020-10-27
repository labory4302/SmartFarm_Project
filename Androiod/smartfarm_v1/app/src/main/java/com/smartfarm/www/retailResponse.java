package com.smartfarm.www;

public class retailResponse {
    private String statusCode;  // 전송 코드 (원활히 통신 됬는지 확인용)
    private String cabbage_result;   // 배추 소매 가격
    private String rice_result;   // 쌀 소매 가격
    private String bean_result;   // 콩 소매 가격
    private String redPepper_result;   // 빨간 고추 소매 가격
    private String strawberry_result;   // 딸기 소매 가격

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCabbage_result() {
        return cabbage_result;
    }

    public void setCabbage_result(String cabbage_result) {
        this.cabbage_result = cabbage_result;
    }

    public String getRice_result() {
        return rice_result;
    }

    public void setRice_result(String rice_result) {
        this.rice_result = rice_result;
    }

    public String getBean_result() {
        return bean_result;
    }

    public void setBean_result(String bean_result) {
        this.bean_result = bean_result;
    }

    public String getRedPepper_result() {
        return redPepper_result;
    }

    public void setRedPepper_result(String redPepper_result) {
        this.redPepper_result = redPepper_result;
    }

    public String getStrawberry_result() {
        return strawberry_result;
    }

    public void setStrawberry_result(String strawberry_result) {
        this.strawberry_result = strawberry_result;
    }
}

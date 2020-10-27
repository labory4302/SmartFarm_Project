package com.smartfarm.www.service;

// AWS 람다 함수는 입력값과 출력값이 JSON 구조이기 때문에
// JSON을 자바로 구현하려면 getter setter로 구현하여 주어야만 한다.

public class ResponseClass {
    private String statusCode;  // 전송 코드 (원활히 통신 됬는지 확인용)
    private String body;   // 결과 값

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public ResponseClass() {
    }
}
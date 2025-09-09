package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ResponseModel {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String message;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    // Constructor
    public ResponseModel() {}
    public ResponseModel(String message, String status) {
        this.message = message;
        this.status = status;
    }
    public ResponseModel(String message, String status, String token) {
        this.message = message;
        this.status = status;
        this.data = token;
    }
    public ResponseModel(String message, String status, Object data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    // Getters
    public String getMessage() {
        return this.message;
    }
    public String getStatus() {
        return this.status;
    }
    public Object getData() {
        return this.data;
    }
    public String getToken() {
        return this.token;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public void setToken(String token) {
        this.token = token;
    }
}

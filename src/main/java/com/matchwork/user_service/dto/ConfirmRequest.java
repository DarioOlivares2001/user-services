package com.matchwork.user_service.dto;

public class ConfirmRequest {
    private String email;
    private String code;

    // getters & setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

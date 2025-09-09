package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class UserModel {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String userCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String username;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String password;
    private  String firstname;
    private  String lastname;
    private  String avatarUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String confirmPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String newPassword;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String deletedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String token;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String timezone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String dateFormat;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String productName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String brandImageUrl;

    public UserModel() {}
    public UserModel(String userCode, String username, String email, String password, String firstName, String lastName, String avatarUrl, String confirmPassword, String deletedAt, String createdAt, String updatedAt, String newPassword, String token,
        String timezone, String dateFormat, String productName, String brandImageUrl
    ) {
        this.userCode = userCode;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstname = firstName;
        this.lastname = lastName;
        this.avatarUrl = avatarUrl;
        this.confirmPassword = confirmPassword;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.newPassword = newPassword;
        this.token = token;
        this.timezone = timezone;
        this.dateFormat = dateFormat;
        this.productName = productName;
        this.brandImageUrl = brandImageUrl;
    }

    // Getters
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getFirstname() {
        return firstname;
    }
    public String getLastname() {
        return lastname;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public String getConfirmPassword() {
        return  confirmPassword;
    }
    public String getUserCode() {
        return userCode;
    }
    public String getDeletedAt() {
        return deletedAt;
    }
    public String getToken() { return token; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getNewPassword() { return newPassword; }
    public String getTimezone() { return timezone; }
    public String getDateFormat() { return dateFormat; }
    public String getProductName() { return productName; }
    public String getBrandImageUrl() { return brandImageUrl; }

    //Setters
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }
    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }
    public void setToken(String token) { this.token = token; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setNewPassword(String newPassword) {  this.newPassword = newPassword; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setBrandImageUrl(String brandImageUrl) { this.brandImageUrl = brandImageUrl; }

    //Additional Method
    public String createUserCode(){
        return UUID.randomUUID().toString();
    }
}

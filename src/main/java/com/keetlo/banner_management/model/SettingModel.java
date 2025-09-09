package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class SettingModel {
    private String systemSettingId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String userCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String systemGeneralSettingId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String productName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String timezone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String dateFormat;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String systemBrandSettingId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String brandImageUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String systemTrackingAndAnalyticSettingId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  Integer trackViews;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  Integer trackClicks;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  Integer trackActionLogs;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String systemApiSettingId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String publicKey;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String secretKey;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String updatedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String confirmText = "";

    public SettingModel() {}
    public SettingModel(String systemSettingId, String userCode,
                        String systemGeneralSettingId, String productName, String timezone, String dateFormat,
                        String systemBrandSettingId, String brandImageUrl,
                        String systemTrackingAndAnalyticSettingId, String trackViews, String trackClicks, String trackActionLogs,
                        String systemApiSettingId, String publicKey, String secretKey,
                        String createdAt, String updatedAt, String confirmText
    ) {
        this.systemSettingId = systemSettingId;
        this.userCode = userCode;

        this.systemGeneralSettingId = systemGeneralSettingId;
        this.productName = productName;
        this.timezone = timezone;
        this.dateFormat = dateFormat;

        this.systemBrandSettingId = systemBrandSettingId;
        this.brandImageUrl = brandImageUrl;

        this.systemTrackingAndAnalyticSettingId = systemTrackingAndAnalyticSettingId;
        this.trackViews = Integer.parseInt(trackViews);
        this.trackClicks = Integer.parseInt(trackClicks);
        this.trackActionLogs = Integer.parseInt(trackActionLogs);

        this.systemApiSettingId = systemApiSettingId;
        this.publicKey = publicKey;
        this.secretKey = secretKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        this.confirmText = confirmText;
    }

    // Getters
    public String getSystemSettingId() { return systemSettingId; }
    public String getProductName() { return productName; }

    public String getSystemGeneralSettingId() { return systemGeneralSettingId; }
    public String getTimezone() { return timezone; }
    public String getDateFormat() { return dateFormat; }

    public String getSystemBrandSettingId() { return systemBrandSettingId; }
    public String getBrandImageUrl() { return brandImageUrl; }

    public String getSystemTrackingAndAnalyticSettingId() { return systemTrackingAndAnalyticSettingId; }
    public Integer getTrackViews() { return trackViews; }
    public Integer getTrackClicks() { return trackClicks; }
    public Integer getTrackActionLogs() { return trackActionLogs; }

    public String getSystemApiSettingId() { return systemApiSettingId; }
    public String getPublicKey() { return publicKey; }
    public String getSecretKey() { return secretKey; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public String getConfirmText() { return confirmText; }

    //Setters
   public void setSystemSettingId(String systemSettingId) { this.systemSettingId = systemSettingId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setSystemGeneralSettingId(String systemGeneralSettingId) { this.systemGeneralSettingId = systemGeneralSettingId; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public void setSystemBrandSettingId(String systemBrandSettingId) { this.systemBrandSettingId = systemBrandSettingId; }
    public void setBrandImageUrl(String brandImageUrl) { this.brandImageUrl = brandImageUrl; }
    public void setSystemTrackingAndAnalyticSettingId(String systemTrackingAndAnalyticSettingId) {
        this.systemTrackingAndAnalyticSettingId = systemTrackingAndAnalyticSettingId;
    }
    public void setTrackViews(Integer trackViews) { this.trackViews = trackViews; }
    public void setTrackClicks(Integer trackClicks) { this.trackClicks = trackClicks; }
    public  void setTrackActionLogs(Integer trackActionLogs) { this.trackActionLogs = trackActionLogs; }
    public void setSystemApiSettingId(String systemApiSettingId) { this.systemApiSettingId = systemApiSettingId; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setConfirmText(String confirmText) { this.confirmText = confirmText; }

    //Additional Method
    public String createSettingCode(){
        return UUID.randomUUID().toString();
    }
    public String createPublicKey(){
        return UUID.randomUUID().toString();
    }
    public String createSecretKey(){
        return UUID.randomUUID().toString();
    }
}

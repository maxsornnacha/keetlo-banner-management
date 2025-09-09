package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class CampaignModel {
    private String campaignId;
    private String projectId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalBanners;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectName;
    private String campaignName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    private String campaignImageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String startDate;
    private String endDate;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updatedAt;

    public CampaignModel(){}
    public CampaignModel(String projectId, Integer totalBanners, String campaignId, String projectName, String campaignName, String description, String status,
                         String startDate, String endDate, String campaignImageUrl, String updatedAt) {
        this.projectId = projectId;
        this.campaignId = campaignId;
        this.totalBanners = totalBanners;
        this.projectName = projectName;
        this.campaignName = campaignName;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate  = endDate;
        this.campaignImageUrl = campaignImageUrl;
        this.updatedAt = updatedAt;
    }

    //Getters
    public String getCampaignId() {
        return campaignId;
    }
    public String getProjectId() {
        return projectId;
    }
    public Integer getTotalBanners() { return totalBanners; }
    public String getProjectName() { return projectName; }
    public String getCampaignName() {
        return campaignName;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }
    public String getStartDate() { return  startDate; }
    public String getEndDate() { return  endDate; }
    public String getCampaignImageUrl() { return campaignImageUrl; }
    public String getUpdatedAt() { return  updatedAt; }

    //Setters
    public void setCampaignId(String campaignId) {this.campaignId = campaignId;}
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public void setTotalBanners(Integer totalBanners) { this.totalBanners = totalBanners; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public void setCampaignImageUrl(String campaignImageUrl) { this.campaignImageUrl = campaignImageUrl; }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    //Additional Method
    public String createCampaignId(){
        return UUID.randomUUID().toString();
    }
}

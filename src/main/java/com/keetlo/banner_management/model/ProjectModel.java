package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class ProjectModel {
    private String projectId;
    private String projectImageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalCampaigns;
    private String projectName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updatedAt;

    public ProjectModel(){}
    public ProjectModel(String projectId, String projectName, String description, String status,
                        String projectImageUrl, String updatedAt,
                        Integer totalCampaigns) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.description = description;
        this.status = status;
        this.projectImageUrl = projectImageUrl;
        this.updatedAt = updatedAt;
        this.totalCampaigns = totalCampaigns;
    }

    //Getters
    public String getProjectId() {
        return projectId;
    }
    public String getProjectName() {
        return projectName;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }
    public String getProjectImageUrl() { return  projectImageUrl; }
    public String getUpdatedAt() { return  updatedAt; }
    public Integer getTotalCampaigns() { return totalCampaigns; }

    //Setters
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setProjectImageUrl(String projectImageUrl) { this.projectImageUrl = projectImageUrl; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setTotalCampaigns(Integer totalCampaigns) { this.totalCampaigns = totalCampaigns; }

    //Additional Method
    public String createProjectId(){
        return UUID.randomUUID().toString();
    }
}

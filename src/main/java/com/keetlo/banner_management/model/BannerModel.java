package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class BannerModel {
    private String bannerId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String campaignId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectId;
    private String bannerName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String campaignName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String campaignDescription;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectDescription;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String campaignImageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectImageUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    private String imageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer views;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer clicks;
    private String link;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String updatedAt;

    public BannerModel() {}
    public BannerModel(String bannerId, String campaignId, String projectId,
                       String imageUrl, Integer imageWidth,
                       Integer imageHeight, Integer views, Integer clicks, String link, String createdAt, String updatedAt,
                        String bannerName, String campaignName, String projectName, String description,
                       String campaignDescription, String projectDescription,
                       String campaignImageUrl, String projectImageUrl
    ) {
         this.bannerId = bannerId;
         this.campaignId = campaignId;
         this.projectId = projectId;
         this.imageUrl = imageUrl;
         this.imageWidth = imageWidth;
         this.imageHeight = imageHeight;
         this.views = views;
         this.clicks = clicks;
         this.link = link;
         this.createdAt = createdAt;
         this.updatedAt = updatedAt;
         this.bannerName = bannerName;
         this.campaignName =  campaignName;
         this.projectName = projectName;
         this.campaignDescription = campaignDescription;
         this.projectDescription = projectDescription;
         this.campaignImageUrl = campaignImageUrl;
         this.projectImageUrl = projectImageUrl;
         this.campaignName = campaignName;
         this.projectName = projectName;
         this.description = description;
    }

    // Getters
    public String getBannerId() { return bannerId; }
    public String getCampaignId() { return campaignId; }
    public String getProjectId() { return projectId; }
    public String getBannerName() { return bannerName; }
    public String getCampaignName() { return campaignName; }
    public String getProjectName() { return projectName; }
    public String getCampaignDescription() { return campaignDescription; }
    public String getProjectDescription() { return projectDescription; }
    public String getCampaignImageUrl() { return campaignImageUrl; }
    public String getProjectImageUrl() { return projectImageUrl; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public Integer getImageWidth() { return imageWidth; }
    public Integer getImageHeight() { return imageHeight; }
    public Integer getViews() { return views; }
    public Integer getClicks() { return clicks; }
    public String getLink() { return link; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //Setters
    public void setBannerId(String bannerId) { this.bannerId = bannerId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public void setBannerName(String bannerName) { this.bannerName = bannerName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public void setCampaignDescription(String campaignDescription) { this.campaignDescription = campaignDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }
    public void setCampaignImageUrl(String campaignImageUrl) {  this.campaignImageUrl = campaignImageUrl; }
    public void setProjectImageUrl(String projectImageUrl) { this.projectImageUrl = projectImageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }
    public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }
    public void setViews(Integer views) { this.views = views; }
    public void setClicks(Integer clicks) { this.clicks = clicks; }
    public void setLink(String link) { this.link = link; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    //Additional Method
    public String createBannerId(){
        return UUID.randomUUID().toString();
    }
}

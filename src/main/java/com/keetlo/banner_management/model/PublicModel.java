package com.keetlo.banner_management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PublicModel {
    private  String publicKey;
    private String secretKey;
    private  String link;
    private  Integer trackViews;
    private  Integer trackClicks;
    private  String projectStatus;
    private String campaignStatus;
    private  String startDate;
    private  String endDate;


    public PublicModel() {}
    public PublicModel(String publicKey, String link, String secretKey, Integer trackViews, Integer trackClicks,
                       String projectStatus, String campaignStatus, String startDate, String endDate
    ) {
        this.publicKey = publicKey;
        this.link = link;
        this.secretKey = secretKey;
        this.trackViews = trackViews;
        this.trackClicks = trackClicks;
        this.projectStatus = projectStatus;
        this.campaignStatus = campaignStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public String getPublicKey() { return publicKey; }
    public String getSecretKey() { return secretKey; }
    public String getLink() { return link; }
    public Integer getTrackViews() { return trackViews; }
    public Integer getTrackClicks() { return trackClicks; }
    public String getProjectStatus() { return projectStatus; }
    public String getCampaignStatus() { return campaignStatus; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }

    //Setters
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public void setLink(String link) { this.link = link; }
    public void setTrackViews(Integer trackViews) { this.trackViews = trackViews; }
    public void setTrackClicks(Integer trackClicks) { this.trackClicks = trackClicks; }
    public void setProjectStatus(String projectStatus) { this.projectStatus = projectStatus; }
    public void setCampaignStatus(String campaignStatus) { this.campaignStatus = campaignStatus; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    //Help function
    public boolean isDateInRange(String startDate, String endDate) {
        // Parse the start and end dates into LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        // Get the current date
        LocalDate now = LocalDate.now();
        return !now.isBefore(start) && !now.isAfter(end);
    }
}

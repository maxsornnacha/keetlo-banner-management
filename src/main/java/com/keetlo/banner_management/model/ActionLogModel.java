package com.keetlo.banner_management.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

public class ActionLogModel {
    private  String actionLogId;
    private  String type;
    private  String topic;
    private  String message;
    private  String createdAt;


    public ActionLogModel() {}
    public ActionLogModel(String actionLogId, String type, String topic, String message) {
        this.actionLogId = actionLogId;
        this.type = type;
        this.topic = topic;
        this.message = message;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters
    public String getActionLogId() { return actionLogId; }
    public String getType() { return type; }
    public String getTopic() { return topic; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }

    //Setters
    public void setActionLogId(String actionLogId) { this.actionLogId = actionLogId; }
    public void setType(String type) { this.type = type; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

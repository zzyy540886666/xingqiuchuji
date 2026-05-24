package com.xingqiu.server.repair.dto;

public class CreateFeedbackRequest {

    private String content;
    private String images;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
}

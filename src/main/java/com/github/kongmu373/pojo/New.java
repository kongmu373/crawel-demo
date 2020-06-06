package com.github.kongmu373.pojo;

import java.time.Instant;

public class New {
    private String title;
    private String url;
    private String content;
    private Instant createdAt;
    private Instant modifiedAt;

    public New() {
    }

    public New(String title, String url, String content) {
        this.title = title;
        this.url = url;
        this.content = content;
    }

    public New(New old) {
        this.title = old.title;
        this.url = old.url;
        this.content = old.content;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}

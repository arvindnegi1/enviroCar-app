package org.envirocar.core.entity;

import com.google.gson.annotations.SerializedName;

public class Link {

    @SerializedName("href")
    private String href;
    @SerializedName("type")
    private String type;
    @SerializedName("title")
    private String title;
    @SerializedName("rel")
    private String rel;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }
}

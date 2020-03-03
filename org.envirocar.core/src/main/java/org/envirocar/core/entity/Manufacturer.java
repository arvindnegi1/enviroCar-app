package org.envirocar.core.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Manufacturer {

    @SerializedName("links")
    private List<Link> links = null;
    @SerializedName("hsn")
    private String hsn;
    @SerializedName("name")
    private String name;

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getHsn() {
        return hsn;
    }

    public void setHsn(String hsn) {
        this.hsn = hsn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

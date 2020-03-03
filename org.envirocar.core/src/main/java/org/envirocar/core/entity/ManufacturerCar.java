package org.envirocar.core.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ManufacturerCar {
    @SerializedName("links")
    private List<Link> links = null;
    @SerializedName("tsn")
    private String tsn;
    @SerializedName("commercialName")
    private String commercialName;
    @SerializedName("allotmentDate")
    private String allotmentDate;

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getTsn() {
        return tsn;
    }

    public void setTsn(String tsn) {
        this.tsn = tsn;
    }

    public String getCommercialName() {
        return commercialName;
    }

    public void setCommercialName(String commercialName) {
        this.commercialName = commercialName;
    }

    public String getAllotmentDate() {
        return allotmentDate;
    }

    public void setAllotmentDate(String allotmentDate) {
        this.allotmentDate = allotmentDate;
    }
}

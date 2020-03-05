package org.envirocar.core.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CarNew {
    @SerializedName("links")
    private List<Link> links = null;
    @SerializedName("tsn")
    private String tsn;
    @SerializedName("commercialName")
    private String commercialName;
    @SerializedName("allotmentDate")
    private String allotmentDate;
    @SerializedName("category")
    private String category;
    @SerializedName("bodywork")
    private String bodywork;
    @SerializedName("power")
    private Integer power;
    @SerializedName("engineCapacity")
    private Integer engineCapacity;
    @SerializedName("axles")
    private Integer axles;
    @SerializedName("poweredAxles")
    private Integer poweredAxles;
    @SerializedName("seats")
    private Integer seats;
    @SerializedName("maximumMass")
    private Integer maximumMass;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBodywork() {
        return bodywork;
    }

    public void setBodywork(String bodywork) {
        this.bodywork = bodywork;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    public Integer getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(Integer engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public Integer getAxles() {
        return axles;
    }

    public void setAxles(Integer axles) {
        this.axles = axles;
    }

    public Integer getPoweredAxles() {
        return poweredAxles;
    }

    public void setPoweredAxles(Integer poweredAxles) {
        this.poweredAxles = poweredAxles;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Integer getMaximumMass() {
        return maximumMass;
    }

    public void setMaximumMass(Integer maximumMass) {
        this.maximumMass = maximumMass;
    }
}

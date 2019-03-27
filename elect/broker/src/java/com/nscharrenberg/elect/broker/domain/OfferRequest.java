package com.nscharrenberg.elect.broker.domain;

import java.io.Serializable;

public class OfferRequest implements Serializable {
    private String firstName;
    private String lastName;
    private String sector;
    private String region;
    private String skills;

    public OfferRequest() {
        super();
    }

    public OfferRequest(String firstName, String lastName, String sector, String region, String skills) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.sector = sector;
        this.region = region;
        this.skills = skills;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }
}

package com.nscharrenberg.elect.google.domain;

import java.io.Serializable;

public class ResumeRequest implements Serializable {
    private String email;
    private String firstName;
    private String lastName;
    private String sector;
    private String region;
    private String skills;

    public ResumeRequest() {
        super();
    }

    public ResumeRequest(String email, String firstName, String lastName, String sector, String region, String skills) {
        super();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sector = sector;
        this.region = region;
        this.skills = skills;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return String.format("email=%s firstname=%s lastname=%s sector=%s region=%s skills=%s", email, firstName, lastName, sector, region, skills);
    }
}

package com.nscharrenberg.elect.microsoft.domain;

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

    @Override
    public String toString() {
        return String.format("firstname=%s lastname=%s sector=%s region=%s skills=%s", firstName, lastName, sector, region, skills);
    }

    @Override
    public boolean equals(Object obj) {
        if(
                obj instanceof OfferRequest &&
                        this.getFirstName().equals(((OfferRequest) obj).getFirstName()) &&
                        this.getLastName().equals(((OfferRequest) obj).getLastName()) &&
                        this.getSector().equals(((OfferRequest) obj).getSector()) &&
                        this.getRegion().equals(((OfferRequest) obj).getRegion()) &&
                        this.getSkills().equals(((OfferRequest) obj).getSkills())
                ) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + sector.hashCode();
        result = 31 * result + region.hashCode();
        result = 31 * result + skills.hashCode();
        return result;
    }
}

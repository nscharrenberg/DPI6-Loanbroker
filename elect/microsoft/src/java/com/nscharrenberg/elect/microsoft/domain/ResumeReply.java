package com.nscharrenberg.elect.microsoft.domain;

import java.io.Serializable;

public class ResumeReply implements Serializable {
    private String companyId;
    private String functionTitle;
    private double salary;
    private String duration;
    private String contactEmail;
    private String contactPersonName;
    private String functionDescription;

    public ResumeReply() {
        super();
    }

    public ResumeReply(String companyId, String functionTitle, double salary, String duration, String contactEmail, String contactPersonName, String functionDescription) {
        super();
        this.companyId = companyId;
        this.functionTitle = functionTitle;
        this.salary = salary;
        this.duration = duration;
        this.contactEmail = contactEmail;
        this.contactPersonName = contactPersonName;
        this.functionDescription = functionDescription;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getFunctionTitle() {
        return functionTitle;
    }

    public void setFunctionTitle(String functionTitle) {
        this.functionTitle = functionTitle;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getFunctionDescription() {
        return functionDescription;
    }

    public void setFunctionDescription(String functionDescription) {
        this.functionDescription = functionDescription;
    }

    @Override
    public String toString() {
        return String.format("companyId=%s function=%s salary%s duration=%s contactemail=%s contactperson=%s functiondescription=%s", companyId, functionTitle, salary, duration, contactEmail, contactPersonName, functionDescription);
    }
}

package com.nscharrenberg.elect.broker.data;

import java.util.stream.Stream;

public enum CompanyList {
    GOOGLE("google", "STREQ(\"IT\")"),
    MICROSOFT("microsoft", "STREQ(\"IT\")"),
    MCDONALDS("mcdonalds", "STREQ(\"HORECA\")"),
    CLEANING("cleaning", "STREQ(\"CLEANING\")"),
    AMAZON("amazon", "STREQ(\"LOGISTIEK\")"),
    AH("albertheijn", "STREQ(\"LOGISTIEK\")");

    private String name;
    private String criteria;

    CompanyList(String name, String criteria) {
        this.name = name;
        this.criteria = criteria;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public static Stream<CompanyList> stream() {
        return Stream.of(CompanyList.values());
    }
}

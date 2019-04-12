package com.nscharrenberg.elect.broker.data;

import java.util.stream.Stream;

public enum CompanyList {
    //TODO: All Companies known to the broker with their sector. Currently only the sector.
    GOOGLE("google", "SECTOR(\"IT\")"),
    MICROSOFT("microsoft", "SECTOR(\"IT\")"),
    MCDONALDS("mcdonalds", "SECTOR(\"HORECA\")"),
    CLEANING("cleaning", "SECTOR(\"CLEANING\")"),
    AMAZON("amazon", "SECTOR(\"LOGISTIEK\")"),
    AH("albertheijn", "SECTOR(\"LOGISTIEK\")"),
    SODEXO("sodexo", "SECTOR(\"HORECA\")");

    private String name;
    private String sector;

    CompanyList(String name, String sector) {
        this.name = name;
        this.sector = sector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    /**
     * The Enum values have been converted to a stream so it's possible to iterate through them.
     * @return a stream of CompanyList
     */
    public static Stream<CompanyList> stream() {
        return Stream.of(CompanyList.values());
    }
}

package org.jboss.da.communcation.pnc.model;

import java.util.List;

public class Product {
    private int id;
    private String name;
    private String description;
    private String abbreviation;
    private String productCode;
    private String pgmSystemName;
    private List<Integer> productVersionIds;

    public List<Integer> getProductVersionIds() {
        return productVersionIds;
    }

    public void setProductVersionIds(List<Integer> productVersionIds) {
        this.productVersionIds = productVersionIds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getPgmSystemName() {
        return pgmSystemName;
    }

    public void setPgmSystemName(String pgmSystemName) {
        this.pgmSystemName = pgmSystemName;
    }
}

package org.apache.ibatis.submitted.selectkey;

public class Name {
    private int nameId;
    private String name;
    private String generatedName;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getNameId() {
        return nameId;
    }
    public void setNameId(int nameId) {
        this.nameId = nameId;
    }
    public String getGeneratedName() {
        return generatedName;
    }
    public void setGeneratedName(String generatedName) {
        this.generatedName = generatedName;
    }
}

package com.hq.activiti.test.entity;

public class Resource {

    private String name;

    private String desc;

    private String owner;

    public Resource() {
        super();
    }

    public Resource(String name, String desc, String owner) {
        this.name = name;
        this.desc = desc;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

package com.library.model;

public class Category {
    private long id; private String name,description;
    public Category(){ } public Category(long id,String name,String description){this.id=id;this.name=name;this.description=description;}
    public long getId(){return id;} public void setId(long v){id=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
}

package com.example.mani.beatpolice.TagsRelated;

import java.io.Serializable;

public class Tag implements Serializable {

    private int id;
    private String coord;
    private int tagType;
    private String name;
    private String des;

    private String phone;
    private String gender;
    private String n_name;
    private String n_phone;
    private String imageName;


    public Tag(int id, String coord, int tagType, String name, String des, String phone,
               String gender, String n_name, String n_phone, String imageName) {
        this.id = id;
        this.coord = coord;
        this.tagType = tagType;
        this.name = name;
        this.des = des;
        this.phone = phone;
        this.gender = gender;
        this.n_name = n_name;
        this.n_phone = n_phone;
        this.imageName = imageName;

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoord() {
        return coord;
    }

    public void setCoord(String coord) {
        this.coord = coord;
    }

    public int getTagType() {
        return tagType;
    }

    public void setTagType(int tagType) {
        this.tagType = tagType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getN_name() {
        return n_name;
    }

    public void setN_name(String n_name) {
        this.n_name = n_name;
    }

    public String getN_phone() {
        return n_phone;
    }

    public void setN_phone(String n_phone) {
        this.n_phone = n_phone;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}



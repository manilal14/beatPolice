package com.example.mani.beatpolice.TagsRelated;

import java.io.Serializable;

public class Tag implements Serializable {

    private int id;
    private String title;
    private String des;
    private String coord;
    private String imageName;
    private int status;

    public Tag(int id, String title, String des, String coord, String imageName, int status) {
        this.id = id;
        this.title = title;
        this.des = des;
        this.coord = coord;
        this.imageName = imageName;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getCoord() {
        return coord;
    }

    public void setCoord(String coord) {
        this.coord = coord;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

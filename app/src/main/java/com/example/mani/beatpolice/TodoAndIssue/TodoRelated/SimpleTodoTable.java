package com.example.mani.beatpolice.TodoAndIssue.TodoRelated;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class SimpleTodoTable implements Serializable {

    @PrimaryKey
    private int id;
    private String title;
    private String des;

    private String lat;
    private String lon;

    private boolean isChecked;

    public SimpleTodoTable(int id, String title, String des, String lat, String lon) {
        this.id = id;
        this.title = title;
        this.des = des;
        this.lat = lat;
        this.lon = lon;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDes() {
        return des;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}

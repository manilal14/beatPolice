package com.example.mani.beatpolice.TodoAndIssue.TodoRelated;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class TodoTable {

    @PrimaryKey
    private int todoId;

    private String pId;
    private String aId;

    private String title;

    private String tagType;
    private String from;
    private String to;
    private String reportedAt;
    private String imagePath;
    private String des;

    private String reportedAtLat;
    private String reportedAtLon;

    private int isSynced;

    public TodoTable(int todoId, String pId, String aId,String title,String tagType, String from,
                     String to, String reportedAt, String imagePath, String des, String reportedAtLat, String reportedAtLon) {
        this.todoId = todoId;
        this.pId = pId;
        this.aId = aId;
        this.title = title;
        this.tagType = tagType;
        this.from = from;
        this.to = to;
        this.reportedAt = reportedAt;
        this.imagePath = imagePath;
        this.des = des;

        this.reportedAtLat = reportedAtLat;
        this.reportedAtLon = reportedAtLon;


        this.isSynced = 0;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }

    public int getTodoId() {
        return todoId;
    }

    public String getPId() {
        return pId;
    }

    public String getAId() {
        return aId;
    }

    public String getTitle() {
        return title;
    }

    public String getTagType() {
        return tagType;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getReportedAt() {
        return reportedAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDes() {
        return des;
    }

    public String getReportedAtLat() {
        return reportedAtLat;
    }

    public void setReportedAtLat(String reportedAtLat) {
        this.reportedAtLat = reportedAtLat;
    }

    public String getReportedAtLon() {
        return reportedAtLon;
    }

    public void setReportedAtLon(String reportedAtLon) {
        this.reportedAtLon = reportedAtLon;
    }
}

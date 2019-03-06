package com.example.mani.beatpolice.TodoAndIssue.IssueRelated;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class IssueTable {

    @PrimaryKey(autoGenerate = true)
    private int issueId = 0;

    private String pId;
    private String aId;

    private String issueType;
    private String from;
    private String to;
    private String reportedAtTime;
    private String reportedAtLocation;
    private String imagePath;
    private String des;


    //Accident occurs at
    private String locationLatitude;
    private String locationLongitude;

    private String address;

    private int isSynced;

    public IssueTable(String pId, String aId, String issueType, String from, String to, String reportedAtTime,
                      String reportedAtLocation, String imagePath,
                      String des, String locationLatitude, String locationLongitude, String address) {
        this.pId = pId;
        this.aId = aId;
        this.issueType = issueType;
        this.from = from;
        this.to = to;
        this.reportedAtTime = reportedAtTime;
        this.reportedAtLocation = reportedAtLocation;
        this.imagePath = imagePath;
        this.des = des;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.address = address;
    }

    public int getIssueId() {
        return issueId;
    }

    public String getPId() {
        return pId;
    }

    public String getAId() {
        return aId;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getReportedAtTime() {
        return reportedAtTime;
    }
    public String getReportedAtLocation() {
        return reportedAtLocation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDes() {
        return des;
    }


    public String getLocationLatitude() {
        return locationLatitude;
    }

    public String getLocationLongitude() {
        return locationLongitude;
    }

    public String getAddress() {
        return address;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }
}

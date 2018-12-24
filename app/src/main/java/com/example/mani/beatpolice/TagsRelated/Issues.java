package com.example.mani.beatpolice.TagsRelated;

public class Issues {

    private int issueId;
    private String des;
    private String unixTime;

    public Issues(int issueId, String des, String unixTime) {
        this.issueId = issueId;
        this.des = des;
        this.unixTime = unixTime;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(String unixTime) {
        this.unixTime = unixTime;
    }
}

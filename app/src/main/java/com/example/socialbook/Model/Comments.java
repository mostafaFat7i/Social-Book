package com.example.socialbook.Model;

public class Comments {
    private String date,time,uid,username,comment;

    public Comments() {
    }

    public Comments(String date, String time, String uid, String username, String comment) {
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.username = username;
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}


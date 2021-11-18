package com.example.socialbook.Model;

public class FindFriends {

    private String profileimages,fullname,status;

    public FindFriends() {
    }

    public FindFriends(String profileimages, String fullname, String status) {
        this.profileimages = profileimages;
        this.fullname = fullname;
        this.status = status;
    }

    public String getProfileimages() {
        return profileimages;
    }

    public void setProfileimages(String profileimages) {
        this.profileimages = profileimages;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

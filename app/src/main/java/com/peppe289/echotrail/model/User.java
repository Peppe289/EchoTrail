package com.peppe289.echotrail.model;

import java.util.List;

public class User {
    private String username;
    private Integer imageIndex;
    private Integer colorIndex;
    private List<String> links;
    private List<String> notes;
    private List<String> readedNotes;
    private List<String> friends;
    private boolean anonymousByDefault;

    public User() {

    }

    public User(String username, List<String> links, List<String> notes, List<String> readedNotes, List<String> friends, boolean anonymousByDefault) {
        this.username = username;
        this.links = links;
        this.notes = notes;
        this.readedNotes = readedNotes;
        this.friends = friends;
        this.anonymousByDefault = anonymousByDefault;
    }

    public Integer getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(Integer imageIndex) {
        this.imageIndex = imageIndex;
    }

    public Integer getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(Integer colorIndex) {
        this.colorIndex = colorIndex;
    }

    public boolean isAnonymousByDefault() {
        return anonymousByDefault;
    }

    public void setAnonymousByDefault(boolean anonymousByDefault) {
        this.anonymousByDefault = anonymousByDefault;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getReadedNotes() {
        return readedNotes;
    }

    public void setReadedNotes(List<String> readedNotes) {
        this.readedNotes = readedNotes;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

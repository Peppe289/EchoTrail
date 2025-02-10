package com.peppe289.echotrail.model;

import com.google.firebase.Timestamp;

public class Session {
    public String device;
    public String position;
    public Timestamp time;
    public String version;
    public String id;

    /**
     * This empty constructor is used by firestore
     */
    public Session() {

    }

    public Session(String device, String position, Timestamp time, String version) {
        this.device = device;
        this.position = position;
        this.time = time;
        this.version = version;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

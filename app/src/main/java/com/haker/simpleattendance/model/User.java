package com.haker.simpleattendance.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    int id = 0;
    private String name = "";
    private String registeredAt = "";

    private String selfieImageUrl = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getSelfieImageUrl() {
        return selfieImageUrl;
    }

    public void setSelfieImageUrl(String selfieImageUrl) {
        this.selfieImageUrl = selfieImageUrl;
    }
}

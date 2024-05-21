package com.haker.simpleattendance.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "checkin_results")
public class CheckInResult {
    @PrimaryKey(autoGenerate = true)
    int id = 0;

    int userId = 0;

    int isSuccess = 0;

    String date = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(int isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

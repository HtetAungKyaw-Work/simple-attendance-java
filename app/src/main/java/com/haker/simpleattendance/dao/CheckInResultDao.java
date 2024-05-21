package com.haker.simpleattendance.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.haker.simpleattendance.model.CheckInResult;
import com.haker.simpleattendance.model.User;

import java.util.List;

@Dao
public interface CheckInResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(CheckInResult checkInResult);

    @Query("SELECT * FROM checkin_results ORDER BY id ASC")
    LiveData<List<CheckInResult>> getCheckInResults();
}

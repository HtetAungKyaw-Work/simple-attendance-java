package com.haker.simpleattendance.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.haker.simpleattendance.dao.CheckInResultDao;
import com.haker.simpleattendance.dao.UserDao;
import com.haker.simpleattendance.database.AppDatabase;
import com.haker.simpleattendance.model.CheckInResult;
import com.haker.simpleattendance.model.User;

import java.util.List;

public class CheckInResultRepository {

    private CheckInResultDao checkInResultDao;
    private LiveData<List<CheckInResult>> checkInResults;

    public CheckInResultRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        checkInResultDao = db.checkInResultDao();
        checkInResults = checkInResultDao.getCheckInResults();
    }

    public LiveData<List<CheckInResult>> getCheckInResults() {
        return checkInResults;
    }

    public void insert(CheckInResult checkInResult) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            checkInResultDao.insert(checkInResult);
        });
    }
}

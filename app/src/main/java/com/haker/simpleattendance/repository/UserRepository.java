package com.haker.simpleattendance.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.haker.simpleattendance.dao.UserDao;
import com.haker.simpleattendance.database.AppDatabase;
import com.haker.simpleattendance.model.User;

import java.util.List;

public class UserRepository {

    private UserDao userDao;
    private LiveData<List<User>> users;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        users = userDao.getUsers();
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void insert(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(user);
        });
    }
}

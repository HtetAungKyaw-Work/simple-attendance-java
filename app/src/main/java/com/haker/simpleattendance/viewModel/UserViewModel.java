package com.haker.simpleattendance.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.haker.simpleattendance.database.AppDatabase;
import com.haker.simpleattendance.model.User;
import com.haker.simpleattendance.repository.UserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private UserRepository repository;
    private final LiveData<List<User>> users;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        users = repository.getUsers();
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void insert(User user) {
        repository.insert(user);
    }
}

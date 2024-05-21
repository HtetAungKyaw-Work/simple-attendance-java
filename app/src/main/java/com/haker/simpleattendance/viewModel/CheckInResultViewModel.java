package com.haker.simpleattendance.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.haker.simpleattendance.model.CheckInResult;
import com.haker.simpleattendance.model.User;
import com.haker.simpleattendance.repository.CheckInResultRepository;
import com.haker.simpleattendance.repository.UserRepository;

import java.util.List;

public class CheckInResultViewModel extends AndroidViewModel {

    private CheckInResultRepository repository;
    private final LiveData<List<CheckInResult>> checkInResults;

    public CheckInResultViewModel(@NonNull Application application) {
        super(application);
        repository = new CheckInResultRepository(application);
        checkInResults = repository.getCheckInResults();
    }

    public LiveData<List<CheckInResult>> getCheckInResults() {
        return checkInResults;
    }

    public void insert(CheckInResult checkInResult) {
        repository.insert(checkInResult);
    }
}

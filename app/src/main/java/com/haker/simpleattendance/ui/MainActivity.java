package com.haker.simpleattendance.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.haker.simpleattendance.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    ///external/images/media/1000000103

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });

        binding.btnCheckInOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCheckInOut();
            }
        });

        binding.btnVisual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChart();
            }
        });
    }

    private void goToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToCheckInOut() {
        Intent intent = new Intent(this, CheckInOutActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToChart() {
        Intent intent = new Intent(this, ChartActivity.class);
        startActivity(intent);
        finish();
    }
}
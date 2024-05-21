package com.haker.simpleattendance.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.haker.simpleattendance.databinding.ActivityCompareBinding;
import com.haker.simpleattendance.model.CheckInResult;
import com.haker.simpleattendance.model.User;
import com.haker.simpleattendance.viewModel.CheckInResultViewModel;
import com.haker.simpleattendance.viewModel.UserViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CompareActivity extends AppCompatActivity {

    private String TAG = "CompareActivity";

    private ActivityCompareBinding binding;

    private UserViewModel userViewModel;

    private Double percent = 0.0;
    Bitmap bitmap1 = null;
    Bitmap bitmap2 = null;
    private CheckInResultViewModel checkInResultViewModel;
    int userId = 0;
    int isSuccess = 0;
    String date = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCompareBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        checkInResultViewModel = new ViewModelProvider(this).get(CheckInResultViewModel.class);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getUsers().observe(this, users -> {
            Log.i(TAG, "users_size = " + users.size());

            if (users.size() != 0){
                User user = users.get(users.size() - 1);
                userId = user.getId();
                String imageUrl = user.getSelfieImageUrl();
                Log.i(TAG, "imageUrl = " + imageUrl);
                File image = new File(imageUrl);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap1 = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                bitmap1 = Bitmap.createScaledBitmap(bitmap1,bitmap1.getWidth(),bitmap1.getHeight(),true);
                binding.ivSelfie1.setImageBitmap(bitmap1);
            }
        });

        String data = getIntent().getStringExtra("imageUri");
        Uri imageUri = Uri.parse(data);
        //binding.ivSelfie2.setImageURI(imageUri)

        try {
            bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        binding.ivSelfie2.setImageBitmap(bitmap2);

        binding.btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                percent = compareImages(bitmap1, bitmap2);
                if (percent < 50) {
                    Toast.makeText(getApplicationContext(), "Two Selfies are the same!", Toast.LENGTH_SHORT).show();
                    isSuccess = 1;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Two Selfies are not the same!", Toast.LENGTH_SHORT).show();
                    isSuccess = 0;
                }
                Date currentDate = Calendar.getInstance().getTime();
                date = new SimpleDateFormat("dd/MM/yyyy").format(currentDate);
                Log.i(TAG, "date = " + date);

                CheckInResult checkInResult = new CheckInResult();
                checkInResult.setUserId(userId);
                checkInResult.setIsSuccess(isSuccess);
                checkInResult.setDate(date);
                checkInResultViewModel.insert(checkInResult);
            }
        });
    }

    private Double compareImages(Bitmap bitmap1, Bitmap bitmap2) {
        int width1 = bitmap1.getWidth();
        int height1 = bitmap1.getHeight();
        int width2 = bitmap2.getWidth();
        int height2 = bitmap2.getHeight();

        if ((width1 != width2) || (height1 != height2)) {
            percent = 0.0;
            Log.i("here1", "here1");
        }
        else {
            Log.i("here2", "here2");
            long difference = 0L;
            int y = 0;
            while (y < height1) {
                // original y++
                int x = 0;
                while (x < width1) {
                    // original x++
                    int rgbA = bitmap1.getPixel(x, y);
                    int rgbB = bitmap2.getPixel(x, y);
                    int redA = (rgbA >> 16) & 0xff;
                    int greenA = (rgbA >> 8) & 0xff;
                    int blueA = (rgbA)&0xff;
                    int redB = (rgbB >> 16) & 0xff;
                    int greenB = (rgbB >> 8) & 0xff;
                    int blueB = (rgbB)&0xff;

                    difference += Math.abs(redA - redB);
                    difference += Math.abs(greenA - greenB);
                    difference += Math.abs(blueA - blueB);
                    x = x + 2;
                }
                y = y + 2;
            }

            Log.i("difference", String.valueOf(difference));
            double total_pixels = width1 * height1 * 3;
            Log.i("total_pixels", String.valueOf(total_pixels));
            double avg_diff_pixels = difference / total_pixels;
            Log.i("avg_diff_pixels", String.valueOf(avg_diff_pixels));
            percent = (avg_diff_pixels * 100) / 255;
            Log.i("percent", String.valueOf(percent));
        }
        return percent;
    }
}

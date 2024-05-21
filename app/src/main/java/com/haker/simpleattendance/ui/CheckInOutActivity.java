package com.haker.simpleattendance.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.haker.simpleattendance.databinding.ActivityCheckInOutBinding;
import com.haker.simpleattendance.viewModel.CheckInResultViewModel;
import com.haker.simpleattendance.viewModel.UserViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CheckInOutActivity extends AppCompatActivity {

    private String TAG = "CheckInOutActivity";

    private ActivityCheckInOutBinding binding;

    private int PERMISSION_CAMERA_REQUEST = 1;

    private File filePhoto = null;
    private String FILE_NAME = "photo.jpg";

    private int IMAGE_CAPTURE_CODE = 1001;

    private Bitmap takenPhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCheckInOutBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQRScanner();
            }
        });

        binding.btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isCameraPermissionGranted()) {
                        openCamera();
                    }
                    else {
                        ActivityCompat.requestPermissions(
                                CheckInOutActivity.this,
                                new String[] {android.Manifest.permission.CAMERA},
                                PERMISSION_CAMERA_REQUEST);
                    }
                }
                else {
                    Toast.makeText(CheckInOutActivity.this, "Sorry you're version android is not support, Min Android 6.0 (Marsmallow)", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setBeepEnabled(false);
        integrator.setTorchEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                showAlertDialog(result.getContents());
            }
        }
        else if(requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
            takenPhoto = BitmapFactory.decodeFile(filePhoto.getAbsolutePath());
            Uri imageUri = getImageUri(this, takenPhoto);
            Log.i("imageUri", imageUri.toString());

            goToCompare(imageUri.toString());
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showAlertDialog(String data) {
        String[] dataArr = data.split("&");
        String name = dataArr[0];
        String date = dataArr[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage("Name : " + name + "\nDate : " + date);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private Boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                openCamera();
            } else {
                Log.e(TAG, "no camera permission");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);

        filePhoto = getPhotoFile(FILE_NAME);
        Uri image_uri = FileProvider.getUriForFile(this, CheckInOutActivity.this.getPackageName() + ".provider", filePhoto);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    private File getPhotoFile(String fileName) {
        File directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", directoryStorage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path =
                MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void goToCompare(String data) {
        Intent intent = new Intent(this, CompareActivity.class);
        intent.putExtra("imageUri", data);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

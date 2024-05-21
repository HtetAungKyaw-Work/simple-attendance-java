package com.haker.simpleattendance.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.haker.simpleattendance.databinding.ActivityRegisterBinding;
import com.haker.simpleattendance.model.User;
import com.haker.simpleattendance.viewModel.UserViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "RegisterActivity";

    private ActivityRegisterBinding binding;
    private int PERMISSION_CAMERA_REQUEST = 1;

    private File filePhoto = null;
    private String FILE_NAME = "photo.jpg";

    private int IMAGE_CAPTURE_CODE = 1001;

    private Bitmap takenPhoto = null;

    private ActivityResultLauncher<Intent> galleryLauncher = null;

    private Uri galleryUri = null;

    private String name = "";
    private String inputData = "";

    long currentTimeInMil = 0;

    private UserViewModel userViewModel;

    private String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getUsers().observe(this, users -> {
            Log.i(TAG, "users_size = " + users.size());
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data != null){
                            galleryUri = data.getData();
                            if (galleryUri != null){
                                try {
                                    binding.ivProfile.setImageURI(galleryUri);
                                }catch (Exception e){
                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

        binding.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForTakePhotoOrChooseGallery();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidate()) {
                    binding.progressBar.setVisibility(View.VISIBLE);

                    name = binding.etName.getText().toString();

                    String currentDateAndTime = Calendar.getInstance().getTime().toString();
                    Log.i("currentDateAndTime", currentDateAndTime);

                    currentTimeInMil = Calendar.getInstance().getTimeInMillis();

                    inputData = name + "&" + currentDateAndTime;

                    User user = new User();
                    user.setName(name);
                    user.setRegisteredAt(currentDateAndTime);

                    if (takenPhoto != null) {
                        saveToGallery(RegisterActivity.this, takenPhoto, "MySelfie");
                    }
                    else {
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryUri);
                            saveToGallery(RegisterActivity.this, bitmap, "MySelfie");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Log.i(TAG, "imageUrl = " + imageUrl);

                    user.setSelfieImageUrl(imageUrl);
                    userViewModel.insert(user);

                    goToPrintQR(inputData);
                }
            }
        });
    }

    private void showAlertForTakePhotoOrChooseGallery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose...");
        builder.setPositiveButton("Take Photo", new DialogInterface.OnClickListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isCameraPermissionGranted()) {
                        openCamera();
                    }
                    else {
                        ActivityCompat.requestPermissions(
                                RegisterActivity.this,
                                new String[] {android.Manifest.permission.CAMERA},
                                PERMISSION_CAMERA_REQUEST);
                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this, "Sorry you're version android is not support, Min Android 6.0 (Marsmallow)", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
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
        Uri image_uri = FileProvider.getUriForFile(this, RegisterActivity.this.getPackageName() + ".provider", filePhoto);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
            takenPhoto = BitmapFactory.decodeFile(filePhoto.getAbsolutePath());
            binding.ivProfile.setImageBitmap(takenPhoto);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private File getPhotoFile(String fileName) {
        File directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", directoryStorage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean isValidate() {
        if (binding.etName.length() == 0) {
            binding.etName.setError("Please enter Name");
            binding.etName.requestFocus();
            return false;
        }
        if (takenPhoto == null && galleryUri == null) {
            Toast.makeText(this, "You need to set the profile selfie picture!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveToGallery(Context context, Bitmap bitmap, String albumName) {
        String filename = name + "_" + currentTimeInMil + ".png";
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        File myDir = new File(root + "/" + albumName);
        myDir.mkdirs();

        imageUrl = myDir + "/" + filename;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + albumName);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            File file = new File(myDir, filename);
            if (file.exists()) file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void goToPrintQR(String inputData) {
        Intent intent = new Intent(this, PrintQRActivity.class);
        intent.putExtra("inputData", inputData);
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

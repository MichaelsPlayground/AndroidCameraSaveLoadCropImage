package de.androidcrypto.androidcamerasaveloadcropimage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
// https://www.youtube.com/watch?v=nA4XWsG9IPM
// https://github.com/foxandroid/ScopedStorageJavaYT

    // speichern eines kamerabildes, laden eines Bildes aus der Gallery und Cropping eines Bildes aus der Gallery funktionieren

/*
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28"/>
 */

    Button takeImage, saveImage, loadImage, cropImage;

    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    ActivityResultLauncher<Intent> mGetImage;
    Uri imageUri;
    private static final int CAMERA = 100;

    Intent takeImageIntent, saveImageIntent, loadImageIntent, cropImageIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takeImage = findViewById(R.id.btnTakeImage);
        //takeImageIntent = new Intent(MainActivity.this, TakeImage.class);
        saveImage = findViewById(R.id.btnSaveImage);
        //saveImageIntent = new Intent(MainActivity.this, SaveImage.class);
        loadImage = findViewById(R.id.btnLoadImage);
        //loadImageIntent = new Intent(MainActivity.this, ReadImage.class);
        cropImage = findViewById(R.id.btnCropImage);
        cropImageIntent = new Intent(MainActivity.this, CropActivity.class);

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                    isReadPermissionGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                if (result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null) {
                    isWritePermissionGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        mGetImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data"); // this is the thumbnail
                    if (isWritePermissionGranted) {
                        if (saveImageToExternalStorage(UUID.randomUUID().toString(), bitmap)) {
                            Toast.makeText(MainActivity.this, "saved Image successfully", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetImage.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            }
        });

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(loadImageIntent);
            }
        });

        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(cropImageIntent);
            }
        });

        // check and request read and write permissions
        // don't place this above
        // mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>()
        requestPermission();
    }

    private void requestPermission() {
        boolean minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
        isWritePermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
        isWritePermissionGranted = isWritePermissionGranted || minSDK;
        List<String> permissionRequest = new ArrayList<String>();
        if (!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!isWritePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionRequest.isEmpty()) {
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    private boolean saveImageToExternalStorage(String imgName, Bitmap bmp) {
        // https://www.youtube.com/watch?v=nA4XWsG9IPM
        Uri imageCollection = null;
        ContentResolver resolver = getContentResolver();
        // > SDK 28
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imgName + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri imageUri = resolver.insert(imageCollection, contentValues);
        try {
            OutputStream outputStream = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Image not saved: \n" + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return false;
    }
}
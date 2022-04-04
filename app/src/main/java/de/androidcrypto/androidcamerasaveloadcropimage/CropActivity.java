package de.androidcrypto.androidcamerasaveloadcropimage;

/*
 * Copyright (c) 2015 Naver Corp.
 * @Author Ohkyun Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.naver.android.helloyako.imagecrop.model.ViewState;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class CropActivity extends AppCompatActivity {
    public static final String TAG = "CropActivity";

    private ImageCropView imageCropView;
    private ViewState viewState;

    private ActivityResultLauncher<String> mCropPhoto;
    Uri imageUri;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        imageCropView = findViewById(R.id.image);


        Intent i = getIntent();
        Uri uri = i.getData();
        context = getApplicationContext();
        init();

//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int imageWidth = (int) ( (float) metrics.widthPixels / 1.5 );
//        int imageHeight = (int) ( (float) metrics.heightPixels / 1.5 );
//
//        bitmap = BitmapLoadUtils.decode(uri.toString(), imageWidth, imageHeight);
//
//        imageCropView.setImageBitmap(bitmap);

//        imageCropView.setImageFilePath(uri.toString());
        //imageCropView.setImageURI();

        // note: das bild wird geladen in ActivityResultLauncher mCropPhoto

        imageCropView.setAspectRatio(1, 1);

        findViewById(R.id.ratio11btn).setOnClickListener(v -> {
            Log.d(TAG, "click 1 : 1");
            if (isPossibleCrop(1, 1)) {
                imageCropView.setAspectRatio(1, 1);
            } else {
                Toast.makeText(CropActivity.this, R.string.can_not_crop, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.ratio34btn).setOnClickListener(v -> {
            Log.d(TAG, "click 3 : 4");
            if (isPossibleCrop(3, 4)) {
                imageCropView.setAspectRatio(3, 4);
            } else {
                Toast.makeText(CropActivity.this, R.string.can_not_crop, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.ratio43btn).setOnClickListener(v -> {
            Log.d(TAG, "click 4 : 3");
            if (isPossibleCrop(4, 3)) {
                imageCropView.setAspectRatio(4, 3);
            } else {
                Toast.makeText(CropActivity.this, R.string.can_not_crop, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.ratio169btn).setOnClickListener(v -> {
            Log.d(TAG, "click 16 : 9");
            if (isPossibleCrop(16, 9)) {
                imageCropView.setAspectRatio(16, 9);
            } else {
                Toast.makeText(CropActivity.this, R.string.can_not_crop, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.ratio916btn).setOnClickListener(v -> {
            Log.d(TAG, "click 9 : 16");
            if (isPossibleCrop(9, 16)) {
                imageCropView.setAspectRatio(9, 16);
            } else {
                Toast.makeText(CropActivity.this, R.string.can_not_crop, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.crop_btn).setOnClickListener(v -> {
            if (!imageCropView.isChangingScale()) {
                Bitmap b = imageCropView.getCroppedImage();
                if (b != null) {
                    bitmapConvertToFile(b);
                } else {
                    Toast.makeText(CropActivity.this, R.string.fail_to_crop, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isPossibleCrop(int widthRatio, int heightRatio) {
        Bitmap bitmap = imageCropView.getViewBitmap();
        if (bitmap == null) {
            return false;
        }
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        if (bitmapWidth < widthRatio && bitmapHeight < heightRatio) {
            return false;
        } else {
            return true;
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

    //public File bitmapConvertToFile(Bitmap bitmap) {
    public void bitmapConvertToFile(Bitmap bitmap) {
        String filename = "IMG_" + (new SimpleDateFormat("yyyyMMddHHmmss")).format(Calendar.getInstance().getTime());
        boolean saveResult;
        saveResult = saveImageToExternalStorage(filename, bitmap);
        if (saveResult == false) {
            System.out.println("*** ERROR image saving not successfull");
            //return null;
            return;
        }
        Toast.makeText(CropActivity.this, "file saved", Toast.LENGTH_LONG).show();
        /*
        FileOutputStream fileOutputStream = null;
        File bitmapFile = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory("image_crop_sample"), "");
            if (!file.exists()) {
                file.mkdir();
            }

            bitmapFile = new File(file, "IMG_" + (new SimpleDateFormat("yyyyMMddHHmmss")).format(Calendar.getInstance().getTime()) + ".jpg");
            fileOutputStream = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            MediaScannerConnection.scanFile(this, new String[]{bitmapFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    runOnUiThread(() -> Toast.makeText(CropActivity.this, "file saved", Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                }
            }
        }*/
        /*
        MediaScannerConnection.scanFile(this, new String[]{bitmapFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                runOnUiThread(() -> Toast.makeText(CropActivity.this, "file saved", Toast.LENGTH_LONG).show());
            }
        });*/
        //return bitmapFile;
        return;
    }

    public void onClickSaveButton(View v) {
        viewState = imageCropView.saveState();
        View restoreButton = findViewById(R.id.restore_btn);
        if (!restoreButton.isEnabled()) {
            restoreButton.setEnabled(true);
        }
    }

    public void onClickRestoreButton(View v) {
        if (viewState == null) {
            return;
        }
        imageCropView.restoreState(viewState);
    }

    private void init(){

        mCropPhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result == null) {
                            System.out.println("result is NULL");
                            return;
                        }

                        //ImageView imageView;
                        ImageView imageView = findViewById(R.id.ivInvisible);
                        imageView.setImageURI(result);
                        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();
                        imageCropView.setImageBitmap(bitmap);
                        /*
                        Bitmap bitmap = null;
                        try {
                            bitmap = ImageUtils.handleSamplingAndRotationBitmap(context, result);
                            imageCropView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }*/
                        // clearing
                        drawable = null;
                        bitmap = null;
                        //imageCropView.setImageURI(result); // does not work
                    }
                }
        );

        if(ContextCompat.checkSelfPermission(
                CropActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ){
            mCropPhoto.launch("image/*");
        } else {
            Toast.makeText(CropActivity.this,"Permission not Granted",Toast.LENGTH_SHORT).show();
        }

        /*
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(
                        CropActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ){
                    mTakePhoto.launch("image/*");
                } else {
                    Toast.makeText(CropActivity.this,"Permission not Granted",Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }
}
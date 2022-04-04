package de.androidcrypto.androidcamerasaveloadcropimage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.ConfigurationCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
// https://www.youtube.com/watch?v=nA4XWsG9IPM
// https://github.com/foxandroid/ScopedStorageJavaYT

    //

/*
<uses-feature android:name="android.hardware.camera"
        android:required="true" />

<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28"/>
 */

    Button takeImage, saveImage, loadImage, cropImage;
    ImageButton rotateRight, rotateLeft;
    ImageView imageView;
    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    ActivityResultLauncher<String[]> myPermissionResultLauncher;
    ActivityResultLauncher<Intent> myTakeImage;
    Context context;
    Uri imageUri;
    //private static final int CAMERA = 100;
    String currentImagePath = "";

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
        imageView = findViewById(R.id.ivImage);
        rotateRight = findViewById(R.id.btnRotateRight);
        rotateLeft = findViewById(R.id.btnRotateLeft);

        myPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
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

        /*
        myTakeImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
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
        });*/

        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context = v.getContext();
                takeImageFullResolutionStoreInGallery();
            }
        });

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context = v.getContext();


            }
        });

        loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(loadImageIntent);
                context = v.getContext();
                // open the gallery and pick a file
                // create an instance of the
                // intent of the type image
                Intent i = new Intent();
                // this is for images only
                i.setType("image/*");
                // this is for videos only
                //i.setType("video/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                selectFileFromGalleryActivityResultLauncher.launch(i);
            }
        });

        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(cropImageIntent);
            }
        });

        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(ImageUtils.rotateImageviewRight(imageView));
            }
        });

        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(ImageUtils.rotateImageviewLeft(imageView));
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
            myPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    // section take an image and store it in full resolution in the gallery

    private void takeImageFullResolutionStoreInGallery() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFileFullGallery();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "de.androidcrypto.androidcamerasaveloadcropimage.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureFullGalleryActivityResultLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFileFullGallery() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentImagePath = image.getAbsolutePath();
        return image;
    }

    ActivityResultLauncher<Intent> takePictureFullGalleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        //Intent resultData = result.getData();
                        // and no resultData is given
                        File f = new File(currentImagePath);

                        // todo remove, just for checking
                        String filePath = f.getPath();
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        System.out.println("*** original bitmap data width: " + bitmap.getWidth() +
                                " height: " + bitmap.getHeight());

                        Uri uriFromFile = Uri.fromFile(f);
                        Bitmap bitmapImage;
                        // after rotation the image is in lower resolution (1.024 * 1.024 px)
                        // don't use the imageView for storage
                        //bitmapImage = handleSamplingAndRotationBitmap(getApplicationContext(), uriFromFile);
                        //imageView.setImageBitmap(bitmapImage);

                        // todo remove
                        //System.out.println("*** image bitmap data width: " + bitmapImage.getWidth() +
                        //        " height: " + bitmapImage.getHeight());


                        int rotationInDegrees = 0;
                        Bitmap bitmapRot = ImageUtils.rotateImage(bitmap, 90);
                        imageView.setImageBitmap(bitmapRot);
                        System.out.println("*** rotate bitmap data width: " + bitmapRot.getWidth() +
                                " height: " + bitmapRot.getHeight());


                        //imageView.setImageURI(Uri.fromFile(f));
                        //Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(f));
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        context.sendBroadcast(mediaScanIntent);
                        Toast.makeText(MainActivity.this, "Image was save to gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            });




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

    // section select file from gallery start

    ActivityResultLauncher<Intent> selectFileFromGalleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        Uri selectedImageUri = resultData.getData();
                        if (null != selectedImageUri) {
                            // update the preview image in the layout
                            imageView.setImageURI(selectedImageUri);
                            //String info = "file name: " + selectedImageUri.toString();
                            //tvG02.setText(info);
                        }
                    }
                }
            });

    // section select file from gallery end
}
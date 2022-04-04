
add in AndroidManifest.xml:
```plaintext
<uses-feature android:name="android.hardware.camera"
        android:required="true" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission
android:name="android.permission.WRITE_EXTERNAL_STORAGE"
android:maxSdkVersion="28" />


<provider
   android:name="androidx.core.content.FileProvider"
   android:authorities="de.androidcrypto.androidcamerasaveloadcropimage.fileprovider"
   android:exported="false"
   android:grantUriPermissions="true">
   <meta-data
      android:name="android.support.FILE_PROVIDER_PATHS"
      android:resource="@xml/file_paths" />
</provider>
```


for cropping add in build.gradle (Module):
```plaintext
implementation 'com.naver.android.helloyako:imagecropview:1.2.3'
implementation 'androidx.exifinterface:exifinterface:1.3.3'
```

check latest version number here:

https://github.com/naver/android-imagecropview

exifinterface is used to get orientation and rotate image

content of file_paths.xml
```plaintext
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
        name="external"
        path="." />
    <external-files-path
        name="external_files"
        path="." />
    <cache-path
        name="cache"
        path="." />
    <external-cache-path
        name="external_cache"
        path="." />
    <files-path
        name="files"
        path="." />
</paths>
```

The image is taken by the author and shows the Grand Prismatic Spring in Yellowstone National Park,
Wyoming, USA.


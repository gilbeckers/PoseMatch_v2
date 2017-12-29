/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*  This file has been modified by Nataniel Ruiz affiliated with Wall Lab
 *  at the Georgia Institute of Technology School of Interactive Computing
 */

package org.tensorflow.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.IOException;

public class CameraActivity extends Activity {
    private static final int PERMISSIONS_REQUEST = 1;
    public static final String TAG = "PoseMatch";
    public static final String EXTRA_IMAGE = "INUPUT_IMAGE" ;
    public static final String EXTRA_MODEL_POSE = "MODEL_POSE" ;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int REQUEST_CODE_MODEL_POSE = 1;
    private static final Logger LOGGER = new Logger();

    //public static boolean UPLOADING = false;


    private Button takePictureButton;



    public static File resultingFile;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        if (hasPermission()) {
            if (null == savedInstanceState) {
                setFragment();
            }
        } else {
            requestPermission();
        }

        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment();
                } else {
                    requestPermission();
                }
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) || shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                Toast.makeText(CameraActivity.this, "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

    private void setFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, CameraConnectionFragment.newInstance())
                .commit();
    }

    public void chooseModelPose(View view) {
        LOGGER.i("-----CHOOOOOOSE MODEL");
        // start new activity Choose Model Pose   (to server)
        Intent intent = new Intent(this, ChooseModelPose.class);
        startActivity(intent);
        //startActivityForResult(intent, REQUEST_CODE_MODEL_POSE);
    }

    // Take picture with not-in-app camera view
    // Is handig om te behouden voor debugging
    public void takePicture(){
        LOGGER.e("---LOG In takePicture()");
        File folder = new File(Environment.getExternalStorageDirectory().toString()+"/ImagesFolder/");
        folder.mkdirs();

        LOGGER.e("Dir: " + folder.getAbsolutePath());

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        resultingFile = new File(folder.toString() + "/image.jpg");

        // https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
        //Uri uriSavedImage = Uri.fromFile(resultingFile);
        Uri uriSavedImage = FileProvider.getUriForFile(getApplicationContext(),
                getApplicationContext().getPackageName() + ".my.package.name.provider",
                resultingFile);


        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

        LOGGER.e("---LOG Start intent");
        startActivityForResult(cameraIntent, 1888);
        return;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGGER.e("---LOG requestcode: " +requestCode);

        // is voor stomme camera view (dieje not-in-app)
        // wordt dus niet meer gebruikt eigenlijk
        // Start ChooseModel Activity
        // Handler van not-in-app cameraview takePicture()
        if (requestCode == 1888 && resultCode == Activity.RESULT_OK) {

            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(photo);

            try {
                Bitmap  photo = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(resultingFile));

                // start new activity Choose Model Pose (to server)
                Intent intent = new Intent(this, ChooseModelPose.class);
                startActivityForResult(intent, REQUEST_CODE_MODEL_POSE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_CODE_MODEL_POSE) {


            LOGGER.i("#######welle hier");
            if(resultCode == RESULT_OK) {
                int modelId = data.getIntExtra("pose_model", 1);
                LOGGER.e("############-----GEKOZEN MODEL:  "+ modelId);

                Intent intent = new Intent();
                intent.putExtra(EXTRA_IMAGE, resultingFile);
                intent.putExtra(EXTRA_MODEL_POSE, modelId);
                setResult(RESULT_OK, intent);

                // Return back to MainActivity and close CameraActivity and the fragment
                finish();
                /*
                // start UploadImageView
                Intent intent = new Intent(this, UploadImageActivity.class);
                intent.putExtra(EXTRA_IMAGE, resultingFile);
                intent.putExtra(EXTRA_MODEL_POSE, modelId);
                startActivity(intent);
                */

            }
        }


    }


}

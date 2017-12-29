package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.tensorflow.demo.env.Logger;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_POSE_IMAGE = 2;
    private static final Logger LOGGER = new Logger();

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageViewPose);
    }

    public void startCameraView(View view) {
        Intent intent = new Intent(this, CameraActivity.class );
        this.startActivityForResult(intent, MainActivity.REQUEST_POSE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGGER.e("---LOG requestcode: " +requestCode);


        if (requestCode == REQUEST_POSE_IMAGE) {
            LOGGER.i("#######welle hier");
            if(resultCode == RESULT_OK) {
                LOGGER.e("Pose image ontvangennnn");
                File pictureFile = (File) data.getExtras().get(CameraActivity.EXTRA_IMAGE);
                int modelId = (int) data.getExtras().get(CameraActivity.EXTRA_MODEL_POSE);

                Bitmap image = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                imageView.setImageBitmap(image);


                LOGGER.e("#### IN mainactivity  model id: " + modelId);


                // start UploadImageView
                Intent intent = new Intent(this, UploadImageActivity.class);
                intent.putExtra(CameraActivity.EXTRA_IMAGE, pictureFile);
                intent.putExtra(CameraActivity.EXTRA_MODEL_POSE, modelId);
                startActivity(intent);


            }
        }


    }


    public void viewModels(View view) {
        LOGGER.i("-----BROWSE MODELSS");
        // start new activity Choose Model Pose   (to server)
        Intent intent = new Intent(this, ChooseModelPose.class);
        startActivity(intent);
    }
}

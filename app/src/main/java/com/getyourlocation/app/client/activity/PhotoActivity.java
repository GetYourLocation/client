package com.getyourlocation.app.client.activity;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.getyourlocation.app.client.R;
import com.getyourlocation.app.client.util.SensorUtil;
import com.getyourlocation.app.client.widget.CameraPreview;
import com.getyourlocation.app.client.widget.Index;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xusy on 2017/6/27.
 */

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = "PhotoActivity";


    private File framesDir;
    private SensorUtil sensorUtil;
    private List<String> sensorData;

    private Camera camera;
    private CameraPreview cameraPreview;
    private Button captureBtn;

    private File[] refPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Index.initIndex();
        initCamera();
        initSensor();
        initCaptureBtn();

    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorUtil.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorUtil.unregister();
        releaseCamera();
    }

    private void releaseCamera(){
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
    private void initSensor() {
        sensorData = new ArrayList<>();
        sensorUtil = SensorUtil.getInstance(this);
    }


    private void initCamera() {
        camera = Camera.open();
        cameraPreview = new CameraPreview(this, camera, new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });
        FrameLayout layout = (FrameLayout) findViewById(R.id.photo_preview_layout);
        layout.addView(cameraPreview);
    }
    private void initCaptureBtn() {
        captureBtn = (Button) findViewById(R.id.capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Index.Available()) {
                    camera.takePicture(null, null, pictureCallBack);
                    camera.startPreview();
                }
            }
        });
    }
    private Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            sensorData.add(sensorUtil.getSensorDataString());
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: " );
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

//    private void saveFrameToFile(byte[] raw) {
//        Camera.Size size = cameraPreview.getPreviewSize();
//        YuvImage im = new YuvImage(raw, ImageFormat.NV21, size.width, size.height, null);
//        Rect r = new Rect(0, 0, size.width, size.height);
//        ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
//        im.compressToJpeg(r, CameraPreview.JPEG_QUALITY, jpegStream);
//        String filename = framesDir.getPath() + File.separator + frameCnt + ".jpg";
//        File frameFile = new File(filename);
//        try {
//            FileOutputStream fos = new FileOutputStream(frameFile);
//            fos.write(jpegStream.toByteArray());
//            fos.close();
//            jpegStream.close();
//            Log.d(TAG, "Frame " + frameCnt + " saved");
//            ++frameCnt;
//        } catch (Exception e) {
//            Log.e(TAG, "", e);
//        }
//    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES ),"CollectData");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
    //    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_"+ timeStamp + ".jpg");
            mediaFile = new File(mediaStorageDir.getPath()+File.separator+"ref"+
                    Index.getAvailableIndex()+".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}

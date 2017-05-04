package com.getyourlocation.app.gylclient.activity;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.getyourlocation.app.gylclient.util.CommonUtil;
import com.getyourlocation.app.gylclient.util.SensorUtil;
import com.getyourlocation.app.gylclient.widget.CameraPreview;
import com.getyourlocation.app.gylclient.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;




public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";
    private TextView sensorInfoTxt;
    private SensorUtil sensorUtil;
    private CameraPreview cameraPreview;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data);
        initSensor();
        initCamera();
        initPreview();
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
        releaseResources();
    }

    private void releaseResources() {
        releaseCamera();
        releaseMediaRecorder();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void initSensor() {
        sensorInfoTxt = (TextView) findViewById(R.id.data_sensorTxt);
        sensorUtil = SensorUtil.getInstance(this);
        sensorUtil.setOnSensorUpdatedListener(new SensorUtil.OnSensorUpdatedListener() {
            @Override
            public void onUpdated() {
                String txt = "Sensor: " + sensorUtil.getCompassRotate();
                sensorInfoTxt.setText(txt);
            }
        });
    }

    private void initCamera() {
        try {
            mCamera = Camera.open();
        } catch (Exception e){
            Log.e(TAG, "", e);
        }
    }

    private void initPreview() {
        cameraPreview = new CameraPreview(this, mCamera);
        FrameLayout layout = (FrameLayout) findViewById(R.id.data_preview_layout);
        layout.addView(cameraPreview);
    }

    private void initCaptureBtn() {
        Button captureBtn = (Button) findViewById(R.id.data_capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.showToast(CollectDataActivity.this, "Capture button clicked!");
                   // camera.takePicture(null, null, mPicture);
                if (isRecording) {
                    // stop recording and release camera
                    mMediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder
                    CommonUtil.showToast(CollectDataActivity.this, "end record");
                    // inform the user that recording has stopped
                    //   setCaptureButtonText("Capture");
                    isRecording = false;
                } else {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mMediaRecorder.start();

                        // inform the user that recording has started
                        //  setCaptureButtonText("Stop");
                        isRecording = true;
                        CommonUtil.showToast(CollectDataActivity.this, "start record");
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
                }
            }
        });
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
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

    private boolean prepareVideoRecorder(){
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        // mMediaRecorder.setCaptureRate(10);
        // mMediaRecorder.setMaxDuration(5000);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");

        } else {
            return null;
        }

        return mediaFile;
    }

}

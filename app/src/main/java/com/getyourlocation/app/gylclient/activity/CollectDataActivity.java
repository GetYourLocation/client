package com.getyourlocation.app.gylclient.activity;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";

    private TextView sensorInfoTxt;
    private Button captureBtn;
    private CameraPreview cameraPreview;

    private SensorUtil sensorUtil;

    private File mediaStorageDir;
    private Camera camera;
    private MediaRecorder mediaRecorder;

    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data);
        initSensor();
        initStorageDir();
        initCamera();
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
        releaseMediaRecorder();
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();
            camera = null;
        }
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
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

    private void initStorageDir() {
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES ), "CollectData");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Failed to create media storage directory");
            }
        }
    }

    private void initCamera() {
        try {
            camera = Camera.open();
            cameraPreview = new CameraPreview(this, camera);
            FrameLayout layout = (FrameLayout) findViewById(R.id.data_preview_layout);
            layout.addView(cameraPreview);
        } catch (Exception e){
            Log.e(TAG, "", e);
        }
    }

    private void initCaptureBtn() {
        captureBtn = (Button) findViewById(R.id.data_capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    mediaRecorder.stop();
                    releaseMediaRecorder();
                    camera.lock();
                    captureBtn.setText("Capture");
                    isRecording = false;
                } else {
                    if (prepareVideoRecorder()) {
                        mediaRecorder.start();
                        captureBtn.setText("Stop");
                        isRecording = true;
                    } else {
                        CommonUtil.showToast(CollectDataActivity.this, "Video recorder prepared failed");
                        releaseMediaRecorder();
                    }
                }
            }
        });
    }

    private boolean prepareVideoRecorder(){
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        mediaRecorder.setOutputFile(getOutputMediaFilePath());
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
        mediaRecorder.setCaptureRate(30);
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.e(TAG, "", e);
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.e(TAG, "", e);
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public String getOutputMediaFilePath() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String timeStamp = dateFormat.format(new Date());
        return mediaStorageDir.getPath() + File.separator + timeStamp + ".mp4";
    }
}

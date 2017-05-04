package com.getyourlocation.app.gylclient.activity;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
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


public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";

    private TextView sensorTxt;
    private TextView infoTxt;
    private Button recordBtn;
    private CameraPreview cameraPreview;

    private SensorUtil sensorUtil;

    private File mediaStorageDir;
    private Camera camera;
    private MediaRecorder mediaRecorder;

    private Handler handler;
    private Runnable timingRunnable;

    private int maxFPS = 60;
    private int seconds = 0;
    private boolean isRecording = false;
    private String curFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data);
        initTxt();
        initSensor();
        initStorageDir();
        initCamera();
        initTiming();
        initRecordBtn();
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

    private void initTxt() {
        sensorTxt = (TextView) findViewById(R.id.data_sensorTxt);
        infoTxt = (TextView) findViewById(R.id.data_infoTxt);
    }

    private void initSensor() {
        sensorUtil = SensorUtil.getInstance(this);
        sensorUtil.setOnSensorUpdatedListener(new SensorUtil.OnSensorUpdatedListener() {
            @Override
            public void onUpdated() {
                String txt = String.valueOf(sensorUtil.getCompassRotate());
                sensorTxt.setText(txt);
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
            // Set maximum fps
            int fpsRange[] = new int[2];
            camera.getParameters().getPreviewFpsRange(fpsRange);
            maxFPS = fpsRange[1] / 1000;
            Log.d(TAG, "FPS: " + maxFPS);
        } catch (Exception e){
            Log.e(TAG, "", e);
        }
    }

    private void initTiming() {
        handler = new Handler();
        timingRunnable = new Runnable() {
            @Override
            public void run() {
                recordBtn.setText(String.valueOf(seconds++));
                handler.postDelayed(timingRunnable, 1000);
            }
        };
    }

    private void initRecordBtn() {
        recordBtn = (Button) findViewById(R.id.data_recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    handler.removeCallbacks(timingRunnable);
                    mediaRecorder.stop();
                    releaseMediaRecorder();
                    camera.lock();
                    recordBtn.setText("Start");
                    isRecording = false;
                    infoTxt.setText("Video saved at " + curFilename);
                } else {
                    if (prepareVideoRecorder()) {
                        mediaRecorder.start();
                        seconds = 0;
                        handler.post(timingRunnable);
                        isRecording = true;
                    } else {
                        CommonUtil.showToast(CollectDataActivity.this, "Video recorder prepared failed");
                        releaseMediaRecorder();
                    }
                }
            }
        });
    }

    private boolean prepareVideoRecorder() {
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        mediaRecorder.setCaptureRate(maxFPS);
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
        curFilename = mediaStorageDir.getPath() + File.separator + CommonUtil.getTimestamp() + ".mp4";
        mediaRecorder.setOutputFile(curFilename);
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
}

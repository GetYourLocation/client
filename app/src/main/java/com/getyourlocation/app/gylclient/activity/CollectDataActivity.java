package com.getyourlocation.app.gylclient.activity;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.getyourlocation.app.gylclient.model.SensorData;
import com.getyourlocation.app.gylclient.util.CommonUtil;
import com.getyourlocation.app.gylclient.util.SensorUtil;
import com.getyourlocation.app.gylclient.widget.CameraPreview;
import com.getyourlocation.app.gylclient.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";
    private static final String SENSOR_FILENAME = "sensor.txt";

    private TextView sensorTxt;
    private TextView infoTxt;
    private Button recordBtn;

    private Camera camera;
    private CameraPreview cameraPreview;

    private File storageDir;
    private SensorUtil sensorUtil;

    private Handler handler;
    private Runnable timingRunnable;

    private boolean isRecording = false;
    private int seconds = 0;
    private int frameCnt = 1;

    private List<SensorData> sensorData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data);
        initTxt();
        initSensor();
        initTiming();
        initCamera();
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
    }

    private void releaseCamera(){
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    private void initTxt() {
        sensorTxt = (TextView) findViewById(R.id.data_sensorTxt);
        infoTxt = (TextView) findViewById(R.id.data_infoTxt);
    }

    private void initSensor() {
        sensorData = new ArrayList<>();
        sensorUtil = SensorUtil.getInstance(this);
        sensorUtil.setOnSensorUpdatedListener(new SensorUtil.OnSensorUpdatedListener() {
            @Override
            public void onUpdated() {
                String txt = String.valueOf(sensorUtil.getCompassRotate());
                sensorTxt.setText(txt);
            }
        });
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

    private void initCamera() {
        camera = Camera.open();
        cameraPreview = new CameraPreview(this, camera, new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (isRecording) {
                    saveFrameToFile(data);
                    addSensorData();
                }
            }
        });
        FrameLayout layout = (FrameLayout) findViewById(R.id.data_preview_layout);
        layout.addView(cameraPreview);
    }

    private void initRecordBtn() {
        recordBtn = (Button) findViewById(R.id.data_recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording && createStorageDir()) {
                    seconds = 0;
                    frameCnt = 1;
                    handler.post(timingRunnable);
                    isRecording = true;
                    if (!sensorData.isEmpty())
                        sensorData.clear();
                } else {
                    handler.removeCallbacks(timingRunnable);
                    recordBtn.setText("Start");
                    isRecording = false;
                    saveSensorDataToFile();
                    infoTxt.setText("Data saved to " + storageDir.getPath());
                }
            }
        });
    }

    private boolean createStorageDir() {
        storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "GYL-Data/" + CommonUtil.getTimestamp());
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            CommonUtil.showToast(CollectDataActivity.this, "Failed to create storage directory");
            return false;
        } else {
            return true;
        }
    }

    private void saveFrameToFile(byte[] raw) {
        Camera.Size size = cameraPreview.getPreviewSize();
        YuvImage im = new YuvImage(raw, ImageFormat.NV21, size.width, size.height, null);
        Rect r = new Rect(0, 0, size.width, size.height);
        ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
        im.compressToJpeg(r, CameraPreview.JPEG_QUALITY, jpegStream);
        String filename = storageDir.getPath() + File.separator + frameCnt + ".jpg";
        File frameFile = new File(filename);
        try {
            FileOutputStream fos = new FileOutputStream(frameFile);
            fos.write(jpegStream.toByteArray());
            fos.close();
            jpegStream.close();
            Log.d(TAG, "Frame " + frameCnt + " saved");
            ++frameCnt;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void addSensorData() {
        SensorData data = new SensorData();
        data.setCompass(sensorUtil.getCompassRotate());
        sensorData.add(data);
    }

    private void saveSensorDataToFile() {
        String filename = storageDir.getPath() + File.separator + SENSOR_FILENAME;
        File sensorFile = new File(filename);
        try {
            FileWriter fos = new FileWriter(sensorFile);
            for (SensorData data: sensorData) {
                fos.write(data.toString());
                fos.write('\n');
            }
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }
}

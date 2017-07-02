package com.getyourlocation.app.client.activity;

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

import com.getyourlocation.app.client.util.CommonUtil;
import com.getyourlocation.app.client.util.SensorUtil;
import com.getyourlocation.app.client.widget.CameraPreview;
import com.getyourlocation.app.client.R;
import com.getyourlocation.app.client.widget.MapDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";
    private static final String STORAGE_DIR = "GYL-Data";
    private static final String FRAMES_DIR = "JPEGImages";
    private static final String SENSOR_FILENAME = "sensor.csv";

    private TextView infoTxt;
    private Button recordBtn;
    private MapDialog mapDialog;

    private Camera camera;
    private CameraPreview cameraPreview;

    private File framesDir;
    private SensorUtil sensorUtil;
    private List<String> sensorData;

    private Handler handler;
    private Runnable timingRunnable;

    private boolean isRecording = false;
    private int seconds = 0;
    private int frameCnt = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data);
        initInfoTxt();
        initSensor();
        initTiming();
        initCamera();
        initMapDialog();
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

    private void initInfoTxt() {
        infoTxt = (TextView) findViewById(R.id.data_infoTxt);
    }

    private void initSensor() {
        sensorData = new ArrayList<>();
        sensorUtil = SensorUtil.getInstance(this);
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
                    sensorData.add(sensorUtil.getSensorDataString());
                    saveFrameToFile(data);
                }
            }
        });
        FrameLayout layout = (FrameLayout) findViewById(R.id.data_preview_layout);
        layout.addView(cameraPreview);
    }

    private void initMapDialog() {
        mapDialog = new MapDialog(this, null);
    }

    private void initRecordBtn() {
        recordBtn = (Button) findViewById(R.id.data_recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    if (createStorageDir()) {
                        startRecord();
                    }
                } else {
                    endRecord();
                    mapDialog.setFramesDir(framesDir);
                    mapDialog.show();
                }
            }
        });
    }

    private boolean createStorageDir() {
        framesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                STORAGE_DIR + File.separator + CommonUtil.getTimestamp() + File.separator + FRAMES_DIR);
        if (!framesDir.exists() && !framesDir.mkdirs()) {
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
        String filename = framesDir.getPath() + File.separator + frameCnt + ".jpg";
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

    private void startRecord() {
        seconds = 0;
        frameCnt = 1;
        handler.post(timingRunnable);
        sensorUtil.reset();
        sensorData.clear();
        isRecording = true;
    }

    private void endRecord() {
        handler.removeCallbacks(timingRunnable);
        recordBtn.setText("Start");
        isRecording = false;
        saveSensorDataToFile();
        infoTxt.setText("Frame count: " + (frameCnt - 1) + "\nData saved to " + framesDir.getParent());
    }

    private void saveSensorDataToFile() {
        String filename = framesDir.getParent() + File.separator + SENSOR_FILENAME;
        File sensorFile = new File(filename);
        try {
            FileWriter fos = new FileWriter(sensorFile);
            fos.write("frame," + sensorUtil.getDescription() + "\n");
            for (int i = 0; i < sensorData.size(); ++i) {
                fos.write(i + 1 + "," + sensorData.get(i));
                fos.write("\n");
            }
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }
}

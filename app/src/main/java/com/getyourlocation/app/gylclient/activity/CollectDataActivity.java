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

import com.getyourlocation.app.gylclient.util.CommonUtil;
import com.getyourlocation.app.gylclient.util.SensorUtil;
import com.getyourlocation.app.gylclient.widget.CameraPreview;
import com.getyourlocation.app.gylclient.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";

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
                    storeFrame(data);
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
                } else {
                    handler.removeCallbacks(timingRunnable);
                    recordBtn.setText("Start");
                    isRecording = false;
                    infoTxt.setText("Video saved at " + storageDir.getPath());
                }
            }
        });
    }

    private void storeFrame(byte[] raw) {
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

    private boolean createStorageDir() {
        //判断是否存在sd卡  
        boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
        if(!sdExist){//如果不存在,  
            storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "GYL-Data/" + CommonUtil.getTimestamp());
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                CommonUtil.showToast(CollectDataActivity.this, "Failed to create storage directory");
                return false;
            } else {
                return true;
            }
        }
        else {
            String dbDir = android.os.Environment.getExternalStorageDirectory().toString();
            String path = dbDir + "/Pictures/GYL-Data";
            //判断目录是否存在，不存在则创建该目录  
            storageDir = new File(path);
            if(!storageDir.exists())
                storageDir.mkdirs();

            boolean isFileCreateSuccess = false;
            //判断文件是否存在，不存在则创建该文件  
            //File dbFile = new File(storageDir);
            if(!storageDir.exists()){
                try {
                    isFileCreateSuccess = storageDir.createNewFile();//创建文件 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else
                isFileCreateSuccess = true;

            if(isFileCreateSuccess) {
                return  true;
            }

            else
                return false;

        }
    }

}

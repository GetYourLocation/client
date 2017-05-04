package com.getyourlocation.app.gylclient.activity;

import android.hardware.Camera;
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


public class CollectDataActivity extends AppCompatActivity {
    private static final String TAG = "CollectDataActivity";
    private TextView sensorInfoTxt;
    private SensorUtil sensorUtil;
    private Camera camera;

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
        if (camera != null) {
            camera.release();
            camera = null;
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
            camera = Camera.open();
        } catch (Exception e){
            Log.e(TAG, "", e);
        }
    }

    private void initPreview() {
        CameraPreview cameraPreview = new CameraPreview(this, camera);
        FrameLayout layout = (FrameLayout) findViewById(R.id.data_preview_layout);
        layout.addView(cameraPreview);
    }

    private void initCaptureBtn() {
        Button captureBtn = (Button) findViewById(R.id.data_capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.showToast(CollectDataActivity.this, "Capture button clicked!");
            }
        });
    }


}

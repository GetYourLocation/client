package com.getyourlocation.app.gyl_client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getyourlocation.app.gyl_client.util.SensorUtil;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private SensorUtil sensorUtil;
    private TextView infoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoTxt = (TextView) findViewById(R.id.main_infoTxt);

        Button resetBtn = (Button) findViewById(R.id.main_resetBtn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorUtil.reset();
            }
        });

        sensorUtil = SensorUtil.getInstance(this);
        sensorUtil.setOnSensorUpdatedListener(new SensorUtil.OnSensorUpdatedListener() {
            @Override
            public void onUpdated() {
                float[] acceleration = sensorUtil.getAcceleration();
                float[] gyroRotate = sensorUtil.getGyroRotate();
                float compassRotate = sensorUtil.getCompassRotate();
                infoTxt.setText(getString(R.string.sensor_data,
                        acceleration[0],
                        acceleration[1],
                        acceleration[2],
                        gyroRotate[0],
                        gyroRotate[1],
                        gyroRotate[2],
                        compassRotate));
            }
        });
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
    }
}
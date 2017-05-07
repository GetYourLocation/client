package com.getyourlocation.app.gylclient.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * Manage sensor data.
 */
public class SensorUtil {
    private static final String TAG = "SensorUtil";
    private static SensorUtil instance;

    private static final float NS2S = 1.0f / 1000000000.0f; // nanoseconds to seconds

    private SensorManager sensorManager;
    private SensorEventListener eventListener;
    private OnSensorUpdatedListener onSensorUpdatedListener;

    private float timestamp = 0;

    private float[] acceleration = new float[3];  // axis: x, y, z
    private float[] gyroRotate = new float[3];  // axis: x, y, z
    private float compassRotate;  // axis: z

    /**
     * Called when the sensor data is updated.
     */
    public interface OnSensorUpdatedListener {
        void onUpdated();
    }

    /**
     * Return the only instance.
     */
    public static SensorUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SensorUtil(context);
        }
        return instance;
    }

    /**
     * Register sensor listener.
     */
    public void register() {
        Sensor[] sensors = new Sensor[]{
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        };
        for (Sensor sensor : sensors) {
            sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Unregister sensor listener.
     */
    public void unregister() {
        sensorManager.unregisterListener(eventListener);
    }

    /**
     * Reset sensor data.
     */
    public void reset() {
        acceleration[0] = acceleration[1] = acceleration[2] = 0;
        gyroRotate[0] = gyroRotate[1] = gyroRotate[2] = 0;
        compassRotate = 0;
    }

    private SensorUtil(Context context) {
        this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        this.eventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        acceleration = event.values;
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        if (timestamp != 0) {
                            final float dT = (event.timestamp - timestamp) * NS2S;
                            for (int i = 0; i < 3; ++i) {
                                gyroRotate[i] += (float) Math.toDegrees(event.values[i] * dT);
                            }
                        }
                        timestamp = event.timestamp;
                        break;
                    case Sensor.TYPE_ORIENTATION:
                        compassRotate = event.values[0];
                        break;
                    default:
                        break;
                }
                onSensorUpdatedListener.onUpdated();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing
            }
        };
    }

    public float[] getAcceleration() {
        return acceleration;
    }

    public float[] getGyroRotate() {
        return gyroRotate;
    }

    public float getCompassRotate() {
        return compassRotate;
    }

    public void setOnSensorUpdatedListener(OnSensorUpdatedListener onSensorUpdatedListener) {
        this.onSensorUpdatedListener = onSensorUpdatedListener;
    }
}

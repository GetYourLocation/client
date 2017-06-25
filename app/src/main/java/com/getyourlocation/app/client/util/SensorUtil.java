package com.getyourlocation.app.client.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import sysu.mobile.limk.sensorutils.SensorUtils;


/**
 * Manage sensor data.
 */
public class SensorUtil {
    private static final String TAG = "SensorUtil";
    private static SensorUtil instance;

    private SensorUtils sensorUtils;

    private SensorManager sensorManager;
    private SensorEventListener eventListener;
    private OnSensorUpdatedListener onSensorUpdatedListener;

    private float[] rotateMatrix = new float[9];
    private volatile float[] lastAcceleration = new float[3];
    private volatile float[] lastMagnetism = new float[3];
    private volatile float[] lastOrientation = new float[3];

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
        sensorUtils.registerSensor();
        Sensor[] sensors = new Sensor[]{
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        };
        for (Sensor sensor : sensors) {
            sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /**
     * Unregister sensor listener.
     */
    public void unregister() {
        sensorUtils.unregisterSensor();
        sensorManager.unregisterListener(eventListener);
    }

    /**
     * Reset sensor data.
     */
    public void reset() {
        for (int i = 0; i < 3; ++i) {
            lastAcceleration[i] = 0;
            lastMagnetism[i] = 0;
            lastOrientation[i] = 0;
        }
        sensorUtils.reset();
    }

    public float[] getLastAcceleration() {
        return lastAcceleration;
    }

    public float[] getLastMagnetism() {
        return lastMagnetism;
    }

    public float[] getLastOrientation() {
        return lastOrientation;
    }

    public double getLastGyroRotate() {
        return sensorUtils.getAngle();
    }

    public String getSensorDataString() {
        return lastMagnetism[0] + "," + lastMagnetism[1] + "," + lastMagnetism[2] + ","
                + getPositiveCompassOrientation() + "," + getLastGyroRotate();
    }

    private float getPositiveCompassOrientation() {
        float res = lastOrientation[0];
        return res < 0 ? res + 360 : res;
    }

    public String getDescription() {
        return "mag_x,mag_y,mag_z,orien_x,gyro_rotate";
    }

    public interface OnSensorUpdatedListener {
        void onUpdated();
    }

    public void setOnSensorUpdatedListener(OnSensorUpdatedListener onSensorUpdatedListener) {
        this.onSensorUpdatedListener = onSensorUpdatedListener;
    }

    private SensorUtil(Context context) {
        sensorUtils = SensorUtils.getInstance(context);
        reset();
        this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        this.eventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        lastAcceleration = event.values;
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        lastMagnetism = event.values;
                        break;
                    default:
                        break;
                }
                if (lastAcceleration != null && lastMagnetism != null) {
                    // Compute orientation
                    SensorManager.getRotationMatrix(rotateMatrix, null, lastAcceleration, lastMagnetism);
                    SensorManager.getOrientation(rotateMatrix, lastOrientation);
                    for (int i = 0; i < lastOrientation.length; ++i) {
                        lastOrientation[i] = (float) Math.toDegrees((double) lastOrientation[i]);
                    }
                }
                if (onSensorUpdatedListener != null) {
                    onSensorUpdatedListener.onUpdated();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Do nothing
            }
        };
    }
}


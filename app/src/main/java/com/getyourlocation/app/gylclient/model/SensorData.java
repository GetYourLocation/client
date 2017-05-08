package com.getyourlocation.app.gylclient.model;


public class SensorData {
    private float[] acceleration = new float[3];  // axis: x, y, z
    private float[] gyroRotate = new float[3];  // axis: x, y, z
    private float compassRotate;  // axis: z

    @Override
    public String toString() {
        return acceleration[0]+","+acceleration[1]+","+acceleration[2]+","+gyroRotate[0]
                +","+gyroRotate[1]+","+gyroRotate[2]+","+compassRotate+"\n";
    }

    public float getCompassRotate() {
        return compassRotate;
    }

    public void setCompassRotate(float compassRotate) {
        this.compassRotate = compassRotate;
    }

    public float[] getGyroRotate() {
        return gyroRotate;
    }

    public void setGyroRotate(float[] gyroRotate) {
        this.gyroRotate = gyroRotate;
    }

    public float[] getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float[] acceleration) {
        this.acceleration = acceleration;
    }
}

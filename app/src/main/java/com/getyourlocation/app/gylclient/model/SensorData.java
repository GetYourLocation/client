package com.getyourlocation.app.gylclient.model;


public class SensorData {
    private float compass;

    @Override
    public String toString() {
        return String.valueOf(compass);
    }

    public float getCompass() {
        return compass;
    }

    public void setCompass(float compass) {
        this.compass = compass;
    }
}

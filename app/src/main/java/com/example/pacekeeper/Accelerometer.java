package com.example.pacekeeper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import org.apache.commons.lang3.time.StopWatch;
import java.util.ArrayList;


public class Accelerometer implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] accelerometerValues;
    private static final float ALPHA = 0.8f;
    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private StopWatch stopWatch;
    private double previousTimeStep = 0;

    public Accelerometer(SensorManager sensorManager) {
        stopWatch = new StopWatch();
        this.sensorManager = sensorManager;
        sensorThread = new HandlerThread("Accelerometer");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    public void startAccelerometer() {
        stopWatch.start();
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME, sensorHandler);
        }
    }

    public void stopAccelerometer() {
        if (accelerometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
            accelerometer = null;
        }
        if (sensorThread != null) {
            sensorThread.quitSafely();
            try {
                sensorThread.join();
                sensorThread = null;
                sensorHandler = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (accelerometerValues == null) {
            accelerometerValues = event.values.clone();
        } else {
            for (int i = 0; i < 3; i++) {
                accelerometerValues[i] = ALPHA * accelerometerValues[i] + (1 - ALPHA) * event.values[i];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setAccelerometerValues(float[] values){
        accelerometerValues = values;
    }

    public float[] getAccelerometerValues(){
        return accelerometerValues;
    }

    public double getTimeStep() {
        double timeStep = 0;
        if (previousTimeStep == 0) {
            return timeStep;
        } else {
            timeStep = stopWatch.getTime() - previousTimeStep;
        }
        previousTimeStep = stopWatch.getTime();
        return timeStep / 1000;
    }
}
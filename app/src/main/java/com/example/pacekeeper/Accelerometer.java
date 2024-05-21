package com.example.pacekeeper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Accelerometer implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] accelerometerValues;
    private static final float ALPHA = 0.8f;
    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private ArrayList<float[]> accHistory;
    private ArrayList<float[]> accHistoryFiltered;
    private ArrayList<Long> timeStamp;
    private StopWatch stopWatch;
    private double previousTimeStep = 0;

    public Accelerometer(SensorManager sensorManager) {
        timeStamp = new ArrayList<>();
        stopWatch = new StopWatch();
        accHistory = new ArrayList<>();
        accHistoryFiltered = new ArrayList<>();
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
            storeValues(new Data(accHistory, timeStamp), "raw");
            storeValues(new Data(accHistoryFiltered, timeStamp), "filtered");
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

    public static class Data implements Serializable {
        private ArrayList<float[]> acc;
        private ArrayList<Long> timeStamp;
        private AtomicInteger idCount = new AtomicInteger(0);
        private int id;
        public Data(ArrayList<float[]> acc, ArrayList<Long> timeStamp){
            this.id = idCount.getAndIncrement();
            this.acc = acc;
            this.timeStamp = timeStamp;
        }

        public int getId(){
            return id;
        }

        public ArrayList<float[]> getAcc() {
            return acc;
        }

        public ArrayList<Long> getTimeStamp() {
            return timeStamp;
        }
    }

    public void storeValues(Data d, String type){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                type + "_testDataFile_" + LocalDate.now() + "_#" + stopWatch.getTime() + ".txt");
        try{
            FileOutputStream oos = new FileOutputStream(file);
            ArrayList<float[]> acc = d.getAcc();
            ArrayList<Long> timeStamp = d.getTimeStamp();
            for(int i = 0; i < acc.size(); i++){
                String s = timeStamp.get(i) + " " + acc.get(i)[0] + " " + acc.get(i)[1] + " \n";
                oos.write(s.getBytes());
            }
            oos.flush();
            oos.close();
            Log.i("File write confirmation", "Successfully wrote file.");
        }catch (IOException e){
            Log.e("File write error", "Couldn't write file: " + e.getMessage());
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
        accHistory.add(accelerometerValues);
        timeStamp.add(stopWatch.getTime());
    }

    public void setFilteredAccelerometerValues(float[] values){
        accHistoryFiltered.add(values);
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
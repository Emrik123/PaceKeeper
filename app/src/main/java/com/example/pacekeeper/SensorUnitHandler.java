package com.example.pacekeeper;

import android.content.Context;
import android.hardware.SensorManager;

public class SensorUnitHandler {
    private SensorManager sensorManager;
    private Accelerometer accelerometer;
    private GPS gps;
    private OrientationHandler orientationHandler;
    private RunnerView runnerView;
    private Session session;

    public SensorUnitHandler(RunnerView runnerView) {
        this.runnerView = runnerView;
        sensorManager = (SensorManager) runnerView.requireContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = new Accelerometer(sensorManager);
        gps = new GPS(runnerView, this);
        orientationHandler = new OrientationHandler(sensorManager, this);
        session = runnerView.getCurrentSession();
    }

    public void startSensorThreads(){
        accelerometer.startAccelerometer();
        gps.startLocationUpdates();
        orientationHandler.startOrientationSensor();
    }

    public void stopSensorThreads(){
        accelerometer.stopAccelerometer();
        gps.stopLocationUpdates();
        orientationHandler.stopOrientationSensor();
    }

    public SensorManager getSensorManager(){
        return this.sensorManager;
    }

    public Session getSession(){
        if(session == null){
            return runnerView.getCurrentSession();
        }else{
            return session;
        }
    }

    public Accelerometer getAccelerometer(){
        return this.accelerometer;
    }

    public GPS getGPS(){
        return this.gps;
    }

    public OrientationHandler getOrientationHandler(){
        return this.orientationHandler;
    }
}

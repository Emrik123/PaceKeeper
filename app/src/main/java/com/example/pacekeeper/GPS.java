package com.example.pacekeeper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

public class GPS implements Runnable {

    private SensorUnitHandler sensorUnitHandler;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private final long UPDATE_INTERVAL_MS = 250;
    private final long LOW_ACCURACY = 1;
    private final long HIGH_ACCURACY = 2;
    private long gpsAccuracy = 0;
    private Thread thread;
    private Context context;

    public GPS(Context context, SensorUnitHandler sensorUnitHandler) {
        this.context = context;
        this.sensorUnitHandler = sensorUnitHandler;
        init();
    }

    private void init() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context);
        thread = new Thread(this);
    }

    public void setHighAccuracy(){
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gpsAccuracy = HIGH_ACCURACY;
        updateLocationRequest();
    }

    public void setLowAccuracy(){
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        gpsAccuracy = LOW_ACCURACY;
        updateLocationRequest();
    }

    public void updateLocationRequest() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            stopLocationUpdates();
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }
    
    public void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult.getLastLocation().getAccuracy() > 50) {
                    if(gpsAccuracy != LOW_ACCURACY) {
                        setLowAccuracy();
                    }
                }else{
                    if(gpsAccuracy != HIGH_ACCURACY) {
                        setHighAccuracy();
                    }
                }
                sensorUnitHandler.GPSNotification(locationResult);
            }
        };
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        thread.interrupt();
    }

    @Override
    public void run() {
        Looper.prepare();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            stopLocationUpdates();
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        gpsAccuracy = HIGH_ACCURACY;
        Looper.loop();
    }
}

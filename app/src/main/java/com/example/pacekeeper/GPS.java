package com.example.pacekeeper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import org.jetbrains.annotations.NotNull;

public class GPS implements Runnable {

    private SensorUnitHandler sensorUnitHandler;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private final long UPDATE_INTERVAL_MS = 250;
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

    public void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (!locationResult.getLocations().isEmpty()) {
                    sensorUnitHandler.GPSNotification(locationResult);
                }
            }
        };
        thread.start();
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
        Looper.loop();
    }
}

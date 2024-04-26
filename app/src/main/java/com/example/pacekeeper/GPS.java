package com.example.pacekeeper;

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
    private RunnerView runnerView;
    private Thread thread;

    public GPS(RunnerView runnerView, SensorUnitHandler sensorUnitHandler) {
        this.sensorUnitHandler = sensorUnitHandler;
        this.runnerView = runnerView;
        init();
    }

    private void init() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(runnerView.requireContext());
        thread = new Thread(this);
    }

    public void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                System.out.println(Thread.currentThread().getName() + " , gps update");
                super.onLocationResult(locationResult);
                if (!locationResult.getLocations().isEmpty() && sensorUnitHandler.getSession().getRunning()) {
                    sensorUnitHandler.getSession().updateLocation(locationResult.getLocations().get(locationResult.getLocations().size() - 1),
                            locationResult.getLocations().size(), sensorUnitHandler.getAccelerometer().getAccelerometerValues());
                    sensorUnitHandler.getSession().updateSessionData();
                }
            }
        };
        thread.start();
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        thread.interrupt();
    }

    public long getUpdateInterval() {
        return UPDATE_INTERVAL_MS;
    }

    @Override
    public void run() {
        Looper.prepare();
        if (ActivityCompat.checkSelfPermission(runnerView.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(runnerView.requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            stopLocationUpdates();
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        Looper.loop();
    }
}

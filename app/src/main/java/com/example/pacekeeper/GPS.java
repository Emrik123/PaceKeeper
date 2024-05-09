package com.example.pacekeeper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import org.jetbrains.annotations.NotNull;

/**
 * This class holds a GNSS sensor, currently only utilizing the GPS.
 * It gets hold of an Android Location instance using a FusedLocationProviderClient
 * through passing a LocationRequest.
 * The sensor interval is set to 4 Hz, but rarely achieves this rate of update.
 * It implements the Runnable interface and uses a private Thread to execute the Location updates.
 * @author Emrik, Johnny
 */
public class GPS implements Runnable {

    private SensorUnitHandler sensorUnitHandler;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private final long UPDATE_INTERVAL_MS = 250;
    private Thread thread;
    private Context context;

    /**
     * Class constructor.
     * Initializes the LocationRequest.
     * It instantiates a reference to the SensorUnitHandler in order to notify
     * the object of each new location received.
     * @param context the application context.
     * @param sensorUnitHandler the object handling all the sensors.
     * @author Emrik
     */
    public GPS(Context context, SensorUnitHandler sensorUnitHandler) {
        this.context = context;
        this.sensorUnitHandler = sensorUnitHandler;
        init();
    }

    /**
     * Used to initialize the LocationRequest.
     * @author Emrik, Johnny
     */
    private void init() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context);
        thread = new Thread(this);
    }

    /**
     * Used to define a Callback and starts the thread to start listening for changes in locational data.
     * @author Emrik
     */
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

    /**
     * Used to stop listening for updates in locational data.
     * @author Emrik
     */
    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        thread.interrupt();
    }

    /**
     * Generic run method from the Runnable interface.
     * Uses a default Looper to request location updates with the set interval of 4 Hz.
     * @author Emrik
     */
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

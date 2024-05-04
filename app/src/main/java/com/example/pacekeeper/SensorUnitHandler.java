package com.example.pacekeeper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.LocationResult;

public class SensorUnitHandler extends Service {
    private SensorManager sensorManager;
    private Accelerometer accelerometer;
    private GPS gps;
    private OrientationHandler orientationHandler;
    private Context context;

    public SensorUnitHandler() {
        super();
    }

    public void startSensorThreads() {
        if (accelerometer == null) {
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            accelerometer = new Accelerometer(sensorManager);
            gps = new GPS(context, this);
            orientationHandler = new OrientationHandler(sensorManager, this);
        }
        accelerometer.startAccelerometer();
        gps.startLocationUpdates();
        orientationHandler.startOrientationSensor();
    }

    public void stopSensorThreads() {
        if (accelerometer != null && gps != null && orientationHandler != null) {
            accelerometer.stopAccelerometer();
            gps.stopLocationUpdates();
            orientationHandler.stopOrientationSensor();
        }
    }

    public SensorManager getSensorManager() {
        return this.sensorManager;
    }

    public Accelerometer getAccelerometer() {
        return this.accelerometer;
    }

    public GPS getGPS() {
        return this.gps;
    }

    public OrientationHandler getOrientationHandler() {
        return this.orientationHandler;
    }

    public void GPSNotification(LocationResult result) {
        Intent locationIntent = new Intent("locationUpdate");
        Bundle bundle = new Bundle();
        bundle.putParcelable("loc", result);
        float[] a = accelerometer.getAccelerometerValues();
        bundle.putFloatArray("accel", a);
        locationIntent.putExtras(bundle);
        sendBroadcast(locationIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy called");
        stopService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("STOP")) {
            stopService();
        } else if (intent.getAction().equals("START")) {
            startService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startService() {
        context = getApplicationContext();
        if (context != null) {
            startSensorThreads();
        }
        startForeground(1, getNotification());
    }

    private void stopService() {
        stopSensorThreads();
        stopForeground(true);
        stopSelf();
    }

    private Notification getNotification() {
        String channelId = "sensor_service";
        NotificationChannel channel = new NotificationChannel(channelId, "Sensor Service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Sensor Service")
                .setContentText("Sensor Service active")
                .setSmallIcon(R.drawable.pacekeeperlogo)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

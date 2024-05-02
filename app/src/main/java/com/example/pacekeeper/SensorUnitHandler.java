package com.example.pacekeeper;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    public void startSensorThreads(){
        if(accelerometer == null){
            sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            accelerometer = new Accelerometer(sensorManager);
            gps = new GPS(context, this);
            orientationHandler = new OrientationHandler(sensorManager, this);
        }
        accelerometer.startAccelerometer();
        gps.startLocationUpdates();
        orientationHandler.startOrientationSensor();
    }

    public void stopSensorThreads(){
        if(accelerometer != null && gps != null && orientationHandler != null){
            accelerometer.stopAccelerometer();
            gps.stopLocationUpdates();
            orientationHandler.stopOrientationSensor();
        }
    }

    public SensorManager getSensorManager(){
        return this.sensorManager;
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

    public void GPSNotification(LocationResult result){
        Intent locationIntent = new Intent("locationUpdate");
        Bundle bundle = new Bundle();
        bundle.putParcelable("loc", result);
        float[] a = accelerometer.getAccelerometerValues();
        bundle.putFloatArray("accel", a);
        locationIntent.putExtras(bundle);
        sendBroadcast(locationIntent);
    }

//    public void initBroadcaster(Session session){
//        if(sessionBroadcastReceiver == null){
//            sessionBroadcastReceiver = new SessionBroadcastReceiver(session);
//        }else{
//            sessionBroadcastReceiver.setSession(session);
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
        if(context!= null){
            startSensorThreads();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSensorThreads();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, getNotification());
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("locationUpdate");
//        if(sessionBroadcastReceiver != null){
//            registerReceiver(sessionBroadcastReceiver, intentFilter);
//        }else{
//            sessionBroadcastReceiver = new SessionBroadcastReceiver(null);
//            registerReceiver(sessionBroadcastReceiver, intentFilter);
//        }
        return START_STICKY;
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

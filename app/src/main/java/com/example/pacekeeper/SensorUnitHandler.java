package com.example.pacekeeper;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.LocationResult;

/**
 * This class is used to hold all the sensors which the application uses.
 * It inherits from the Android Service class, and is run as a ForegroundService.
 * Each time the GPS values are changed, a notification is received and broadcast to any listening receivers.
 * @author Emrik, Johnny
 */
public class SensorUnitHandler extends Service {
    private SensorManager sensorManager;
    private Accelerometer accelerometer;
    private GPS gps;
    private OrientationHandler orientationHandler;
    private Context context;

    /**
     * Required empty constructor.
     * @author Emrik
     */
    public SensorUnitHandler() {
        super();
    }

    /**
     * Used to start the sensor threads.
     * @author Emrik
     */
    public void startSensorThreads() {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accelerometer = new Accelerometer(sensorManager);
        gps = new GPS(context, this);
        orientationHandler = new OrientationHandler(sensorManager, this);
        accelerometer.startAccelerometer();
        gps.startLocationUpdates();
        orientationHandler.startOrientationSensor();
    }

    /**
     * Used to stop the sensors threads.
     * @author Emrik
     */
    public void stopSensorThreads() {
        if (accelerometer != null && gps != null && orientationHandler != null) {
            accelerometer.stopAccelerometer();
            gps.stopLocationUpdates();
            orientationHandler.stopOrientationSensor();
        }
    }

    public Accelerometer getAccelerometer() {
        return this.accelerometer;
    }

    /**
     * Each time the GPS is updated with new locational data, this method is called.
     * The new values are then broadcast to any listening receivers.
     * @param result the updated locational data.
     * @author Emrik
     */
    public void GPSNotification(LocationResult result) {
        Intent locationIntent = new Intent("locationUpdate");
        Bundle bundle = new Bundle();
        bundle.putParcelable("loc", result);
        float[] a = accelerometer.getAccelerometerValues();
        bundle.putFloatArray("accel", a);
        locationIntent.putExtras(bundle);
        sendBroadcast(locationIntent);
    }

    /**
     * Default onCreate
     * @see android.app.Service
     * @author Emrik
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Stops the service when stopped and not restarted.
     * @see android.app.Service
     * @author Emrik, Johnny
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService();
    }

    /**
     * Called when the service is started.
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except {@link #START_STICKY_COMPATIBILITY}.
     * @see android.app.Service
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     * start.  Use with {@link #stopSelfResult(int)}.
     *
     * @return result of start command
     * @author Emrik, Johnny
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("STOP")) {
            stopService();
        } else if (intent.getAction().equals("START")) {
            startService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Used to run the instance as a ForegroundService and start all sensor threads.
     * @author Emrik, Johnny
     */
    private void startService() {
        context = getApplicationContext();
        if (context != null) {
            startSensorThreads();
        }
        startForeground(1, getNotification());
    }

    /**
     * Used to stop the instance and kill all sensor threads.
     * @author Emrik, Johnny
     */
    private void stopService() {
        stopSensorThreads();
        stopForeground(true);
        stopSelf();
    }

    /**
     * Used to create a notification for the Service
     * @see android.app.Service
     * @return notification for the Service.
     * @author Emrik, Johnny
     */
    private Notification getNotification() {
        String channelId = "sensor_service";
        NotificationChannel channel = new NotificationChannel(channelId,
                "Sensor Service", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        Intent gotoIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, gotoIntent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("PaceKeeper")
                .setContentText("Session active")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.pacekeeperlogo)
                .setOngoing(true)
                .build();
    }

    /**
     * Default onBind
     * @see android.app.Service;
     * @param intent The Intent that was used to bind to this service,
     * as given to {@link android.content.Context#bindService
     * Context.bindService}.  Note that any extras that were included with
     * the Intent at that point will <em>not</em> be seen here.
     *
     * @return null
     * @author Emrik
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

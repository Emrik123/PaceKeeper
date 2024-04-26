package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.location.*;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Session extends Service {
    private final ArrayList<Location> route;
    private final ArrayList<Double> storedSpeedArray;
    private final ArrayList<String> timePerKm;
    private Location currentLocation;
    private double distance;
    private double selectedSpeed;
    private double currentSpeed;
    private double conversionUnit = 3.6;
    private boolean isRunning;
    private final LocalDate sessionDate;
    private UnitOfVelocity unitOfVelocity;
    private final StopWatch stopwatch;
    private long timeExceptCurrentKm;
    private int kmDistance = 1000;
    private final Kalman kalmanFilter;
    private long timeDelta;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private final long UPDATE_INTERVAL_MS = 250;
    private TestBroadcastReceiver broadcastReceiver;


    public Session(double selectedSpeed) {
        kalmanFilter = new Kalman();
        this.sessionDate = LocalDate.now();
        this.selectedSpeed = selectedSpeed;
        this.isRunning = true;
        route = new ArrayList<>();
        storedSpeedArray = new ArrayList<>();
        timePerKm = new ArrayList<>();
        stopwatch = new StopWatch();
        stopwatch.start();
    }

    public Session() {
        System.out.println("session no args constructor");
        kalmanFilter = new Kalman();
        this.sessionDate = LocalDate.now();
        this.isRunning = true;
        route = new ArrayList<>();
        storedSpeedArray = new ArrayList<>();
        timePerKm = new ArrayList<>();
        stopwatch = new StopWatch();
        stopwatch.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        PowerManager.WakeLock wakeLock;
//        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakelocktag");
        broadcastReceiver = new TestBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("locationUpdate");
        registerReceiver(broadcastReceiver, intentFilter);
        System.out.println("on start command");
        System.out.println("current session referenced object in onStartCommand" + this.hashCode());
        selectedSpeed = intent.getDoubleExtra("speed", 10);
        System.out.println(selectedSpeed);
        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(getApplicationContext(), CHANNELID)
                .setContentText("Session active")
                .setContentTitle("Service enabled")
                .setSmallIcon(R.drawable.pacekeeperlogo)
                .setOngoing(true);
        startForeground(1, notification.build());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                System.out.println("Location updated in session");
                Intent locationIntent = new Intent("locationUpdate");
                Bundle bundle = new Bundle();
                bundle.putParcelable("loc", locationResult);
                locationIntent.putExtras(bundle);
                sendBroadcast(locationIntent);
            }
        };
        startLocationUpdates();
        return super.onStartCommand(intent, flags, startId);
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    public String updateTime() {
        long currentTimeMillis = stopwatch.getTime();

        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public void updateSessionData() {
        if (getDistance() >= kmDistance) {
            long time = getTotalTime() - getTimeExceptCurrentKm();
            addTimePerKm(time);
            addTime(time);
            kmDistance += 1000;
        }
    }

    public void addTime(long kmTime) {
        timeExceptCurrentKm += kmTime;
    }

    public long getTimeExceptCurrentKm() {
        return timeExceptCurrentKm;
    }

    public void setSelectedSpeed(double speed) {
        this.selectedSpeed = speed;
    }

    public void setConversionUnit(double value) {
        this.conversionUnit = value;
    }

    public void pauseSession() {
        this.isRunning = false;
        stopwatch.suspend();
    }

    public void continueSession() {
        isRunning = true;
        currentLocation = null;
        stopwatch.resume();
    }

    public void killSession() {
        isRunning = false;

    }

    public StoredSession getSerializableSession() {
        return new StoredSession(getSessionDate(), getDistance(), getTotalSessionTime(), getTimePerKm(), selectedSpeed);
    }

    public void updateLocation(Location location, int size, float[] a) {
        if (isRunning) {
            Location lastLocation = null;
            if (currentLocation != null) {
                lastLocation = currentLocation;
            }
            this.currentLocation = location;
            route.add(currentLocation);
            double tempTime;
            if (timeDelta != 0) {
                tempTime = stopwatch.getTime() - timeDelta;
                tempTime /= size;
            } else {
                tempTime = 0;
            }
            this.timeDelta = stopwatch.getTime();
            kalmanFilter.predict(a[0], a[1]);
            kalmanFilter.update(currentLocation.getSpeed(), tempTime / 1000);
            double[] result = kalmanFilter.getState();
            this.currentSpeed = Math.abs(result[1]);
//            System.out.println("Current speed Kalman: " + currentSpeed + " ||  Current speed GPS: " + currentLocation.getSpeed());
            if (currentSpeed > 0.5 && lastLocation != null) {
//                double distDelta = Math.abs(result[0]) - distance;
                distance = Math.abs(result[0]);
//                System.out.println("Current distance Kalman: " + distDelta + " ||  Current distance GPS: " + lastLocation.distanceTo(currentLocation));
            }
        }
    }

    public boolean getRunning() {
        return this.isRunning;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public double getSelectedSpeed() {
        return selectedSpeed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public String getFormattedSelectedSpeed() {
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                return selectedSpeed * conversionUnit + unitOfVelocity.toString();
            case MIN_PER_KM:
                return android.text.format.DateUtils.formatElapsedTime((Math.round(3600 / (selectedSpeed * conversionUnit)))) + unitOfVelocity.toString();
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedSpeed() {
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                return String.format("%.2f", currentSpeed * conversionUnit);
            case MIN_PER_KM:
                return android.text.format.DateUtils.formatElapsedTime((long) (1000 / currentSpeed));
        }
        return null;
    }


    public double getCurrentSpeedMinPerKm() {
        return (1000 / currentSpeed) / 60;
    }

    public double getDistance() {
        return distance;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedDistance() {
        if (getDistance() > 1000) {
            return String.format("%.2f", distance / 1000) + " km";
        } else {
            return String.format("%.2f", distance) + " m";
        }
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public String getTotalSessionTime() {
        long totalTimeMillis = stopwatch.getTime();
        long hours = totalTimeMillis / (60 * 60 * 1000);
        long minutes = (totalTimeMillis % (60 * 60 * 1000)) / (60 * 1000);

        StringBuilder formattedTime = new StringBuilder();

        if (hours > 0) {
            formattedTime.append(hours).append("h ");
        }
        if (minutes > 0 || hours == 0) {
            formattedTime.append(minutes).append("min");
        }

        return formattedTime.toString().trim();
    }

    public double getConversionUnit() {
        return conversionUnit;
    }

    public ArrayList<Location> getRoute() {
        return route;
    }

    public void setUnitOfVelocity(UnitOfVelocity unitOfVelocity) {
        this.unitOfVelocity = unitOfVelocity;
    }

    public double calculateAverageSpeed() {
        double avg = 0;
        for (Double d : storedSpeedArray) {
            avg += d;
        }
        return avg / storedSpeedArray.size();
    }

    public long getTotalTime() {
        return stopwatch.getTime();
    }

    public void addTimePerKm(long time) {
        long minutes = (time % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (time % (60 * 1000)) / 1000; //

        StringBuilder formattedTime = new StringBuilder();

        if (minutes > 0) {
            formattedTime.append(minutes).append("min ");
        }
        if (seconds > 0) {
            formattedTime.append(seconds).append("s");
        }

        timePerKm.add(formattedTime.toString());
    }


    public ArrayList<String> getTimePerKm() {
        return timePerKm;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static class StoredSession implements Serializable {

        private static final AtomicInteger idCount = new AtomicInteger(0);
        private final double totalDistance;
        private final String totalTime;
        private int sessionID;
        private static final long serialVersionUID = 0L;
        private LocalDate date;

        private double selectedSpeed;

        private ArrayList<String> timePerKm;

        private String sessionComment;


        public StoredSession(LocalDate date, double distance, String time, ArrayList<String> timePerKm, double selectedSpeed) {
            this.totalTime = time;
            this.totalDistance = distance;
            this.date = date;
            this.timePerKm = timePerKm;
            this.sessionID = idCount.incrementAndGet();
            this.selectedSpeed = selectedSpeed;
        }

        public void setSessionComment(String sessionComment) {
            this.sessionComment = sessionComment;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public String getTotalTime() {
            return totalTime;
        }

        public ArrayList<String> getTimePerKm() {
            return timePerKm;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getSessionComment() {
            return sessionComment;
        }

        public String getSelectedSpeed() {
            return Double.toString(selectedSpeed);
        }
    }


}

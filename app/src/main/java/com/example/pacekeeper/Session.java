package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import com.google.android.gms.location.LocationResult;
import com.mapbox.geojson.Point;
import org.apache.commons.lang3.time.StopWatch;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds and tracks all relevant variables for a running session.
 * It contains an instance of SessionBroadcastReceiver, which receives updates from the running sensor service.
 * @author Emrik, Johnny, Samuel, Jonathan
 */
public class Session {
    private final ArrayList<Point> routeCoordinates;
    private final ArrayList<String> timePerKm;
    private Location currentLocation;
    private double distance;
    private double selectedSpeed;
    private double currentSpeed;
    private double CONVERSION_UNIT_KMH = 3.6;
    private boolean isRunning;
    private final LocalDate sessionDate;
    private UnitOfVelocity unitOfVelocity;
    private final StopWatch stopwatch;
    private long timeExceptCurrentKm;
    private int kmDistance = 1000;
    private final SensorFusionFilter sensorFusionFilter;
    private long timeStep;
    private SessionBroadcastReceiver broadcastReceiver;
    private Context context;
    private FeedbackHandler feedbackHandler;
    private boolean isPaused;
    private String sessionComment;

    /**
     * Constructor.
     * @param selectedSpeed user input desired pace
     * @param context application context
     * @param feedbackHandler see the FeedbackHandler class
     * @see FeedbackHandler
     * @author Emrik, Johnny, Samuel, Jonathan
     */
    public Session(double selectedSpeed, Context context, FeedbackHandler feedbackHandler) {
        this.feedbackHandler = feedbackHandler;
        sensorFusionFilter = new SensorFusionFilter();
        this.sessionDate = LocalDate.now();
        this.selectedSpeed = selectedSpeed;
        isRunning = true;
        isPaused = false;
        routeCoordinates = new ArrayList<>();
        this.context = context;
        timePerKm = new ArrayList<>();
        stopwatch = new StopWatch();
        stopwatch.start();
        broadcastReceiver = new SessionBroadcastReceiver(this);
        initializeReceiver();
    }

    /**
     * Method to update the session timer. Uses a Stopwatch and converts the value into hours, minutes and seconds.
     * @return the converted time
     * @see StopWatch
     * @author Jonathan
     */
    public String updateTime(){
        long currentTimeMillis = stopwatch.getTime();
        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;
        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    /**
     * Method that registers an instance of SessionBroadcastReceiver to the application context.
     * Passes an IntentFilter that listens for "locationUpdate".
     * @see IntentFilter
     * @see android.content.BroadcastReceiver
     * @author Johnny
     */
    public void initializeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("locationUpdate");
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Method that collects a kilometer split when the current distance increments by 1km.
     * @author Samuel, Jonathan
     */
    public void updateSessionData() {
        if (getDistance() >= kmDistance) {
            long time = getTotalTime() - getTimeExceptCurrentKm();
            addTimePerKm(time);
            addTime(time);
            kmDistance += 1000;
        }
    }

    /**
     * Method used to pass the current kilometer split.
     * @param kmTime split for the latest kilometer
     * @author Jonathan
     */
    public void addTime(long kmTime) {
        timeExceptCurrentKm += kmTime;
    }

    public long getTimeExceptCurrentKm() {
        return timeExceptCurrentKm;
    }

    /**
     * Method used to pause the current session
     * @author Emrik, Samuel
     */
    public void pauseSession() {
        isPaused = true;
        stopwatch.suspend();
    }

    /**
     * Method used to continue the current session
     * @author Emrik, Samuel
     */
    public void continueSession() {
        isPaused = false;
        currentLocation = null;
        stopwatch.resume();
    }

    /**
     * Method used to kill (stop) the current session
     * @author Emrik
     */
    public void killSession() {
        isRunning = false;
    }

    /**
     * Method used to pass a serializable object containing the relevant statistics of the current session.
     * @return serializable object holding relevant session data
     * @see StoredSession
     * @author Jonathan
     */
    public StoredSession getSerializableSession() {
        return new StoredSession(sessionDate, getDistance(), getTotalSessionTime(), getTimePerKm(), getFormattedSelectedSpeed(), sessionComment, getRoute());
    }

    /**
     * Method used to update the session data when a locational update has been received.
     * @param location new LocationResult from the GPS
     * @param a accelerometer values
     * @see LocationResult
     * @author Johnny, Samuel, Jonathan, Emrik
     */
    public void updateLocation(LocationResult location, float[] a) {
        if (isRunning && !isPaused) {
            Location lastLocation = null;
            if (currentLocation != null) {
                lastLocation = currentLocation;
            }
            this.currentLocation = location.getLocations().get(location.getLocations().size() - 1);
            com.mapbox.geojson.Point point = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
            routeCoordinates.add(point);

            double deltaTime;
            if (timeStep != 0) {
                deltaTime = stopwatch.getTime() - timeStep;
            } else {
                deltaTime = 0;
            }
            this.timeStep = stopwatch.getTime();
            double acc_xy = Math.sqrt(Math.pow(a[0], 2) + Math.pow(a[1], 2));
            sensorFusionFilter.predict(acc_xy);
            sensorFusionFilter.update(currentLocation.getSpeed(), deltaTime / 1000);
            double[] result = sensorFusionFilter.getState();
            this.currentSpeed = result[1];
            if (currentSpeed > 0.5 && lastLocation != null) {
                distance = result[0];
                updateSessionData();
            }
            feedbackHandler.setRunning(isRunning);
            feedbackHandler.setCurrentSpeed(currentSpeed);
        }
    }

    public boolean getRunning() {
        return isRunning;
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
                return selectedSpeed * CONVERSION_UNIT_KMH + unitOfVelocity.toString();
            case MIN_PER_KM:
                return android.text.format.DateUtils.formatElapsedTime((Math.round(1000 / selectedSpeed))) + unitOfVelocity.toString();
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedCurrentSpeed() {
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                return String.format("%.2f", currentSpeed * CONVERSION_UNIT_KMH);
            case MIN_PER_KM:
                return android.text.format.DateUtils.formatElapsedTime((long) (1000 / currentSpeed));
        }
        return null;
    }

    public double getDistance() {
        return distance;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedDistance() {
        if (getDistance() > 1000) {
            return String.format("%.2f", distance / 1000) + " km";
        } else {
            return String.format("%.0f", distance) + " m";
        }
    }

    public void setSessionComment(String sessionComment) {
        this.sessionComment = sessionComment;
    }

    public String getSessionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        return sessionDate.format(formatter);
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

    public ArrayList<Point> getRoute(){
        return routeCoordinates;
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

    public void setUnitOfVelocity(UnitOfVelocity unitOfVelocity) {
        this.unitOfVelocity = unitOfVelocity;
    }

    /**
     * Inner class used to export Serializable object of current session.
     * @author Jonathan
     */
    public static class StoredSession implements Serializable{
        private final double totalDistance;
        private final String totalTime;
        private static final long serialVersionUID = 0L;
        private final LocalDate date;
        private final String selectedSpeed;
        private final ArrayList<String> timePerKm;
        private String sessionComment;
        private final ArrayList<Point> route;


        public StoredSession(LocalDate date, double distance, String time, ArrayList<String> timePerKm, String selectedSpeed, String sessionComment, List<Point> route){
            this.totalTime = time;
            this.totalDistance = distance;
            this.date = date;
            this.timePerKm = timePerKm;
            this.selectedSpeed = selectedSpeed;
            this.sessionComment = sessionComment;
            this.route = (ArrayList<Point>) route;
        }

        public void setSessionComment(String sessionComment){
            this.sessionComment = sessionComment;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public String getTotalTime() {
            return totalTime;
        }

        public ArrayList<String> getTimePerKm(){
            return timePerKm;
        }

        public String getDate(){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
            if(date!=null){
                return date.format(formatter);
            }else{
                return "no date found";
            }
        }

        public String getSessionComment(){
            return sessionComment;
        }
        public String getSelectedSpeed(){return selectedSpeed;}
        public ArrayList<Point> getRoute(){return route;}
    }
}

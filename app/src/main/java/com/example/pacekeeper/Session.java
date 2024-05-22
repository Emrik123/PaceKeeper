package com.example.pacekeeper;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.text.format.DateUtils;
import com.google.android.gms.location.LocationResult;
import com.mapbox.geojson.Point;
import org.apache.commons.lang3.time.StopWatch;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Session {
    private final ArrayList<Point> routeCoordinates;
    private final ArrayList<Double> storedSpeedArray;
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
    private final Kalman kalmanFilter;
    private long timeStep;
    private SessionBroadcastReceiver broadcastReceiver;
    private Context context;
    private FeedbackHandler feedbackHandler;
    private boolean isPaused;
    private String sessionComment;


    public Session(double selectedSpeed, Context context, FeedbackHandler feedbackHandler) {
        this.feedbackHandler = feedbackHandler;
        kalmanFilter = new Kalman();
        this.sessionDate = LocalDate.now();
        this.selectedSpeed = selectedSpeed;
        isRunning = true;
        isPaused = false;
        routeCoordinates = new ArrayList<>();
        this.context = context;
        storedSpeedArray = new ArrayList<>();
        timePerKm = new ArrayList<>();
        stopwatch = new StopWatch();
        stopwatch.start();
        broadcastReceiver = new SessionBroadcastReceiver(this);
        initializeReceiver();
    }

    public String updateTime(){
        long currentTimeMillis = stopwatch.getTime();
        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;
        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public void initializeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("locationUpdate");
        context.registerReceiver(broadcastReceiver, intentFilter);
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


    public void pauseSession() {
        isPaused = true;
        stopwatch.suspend();
    }


    public void continueSession() {
        isPaused = false;
        currentLocation = null;
        stopwatch.resume();
    }

    public void killSession() {
        isRunning = false;
    }

    public StoredSession getSerializableSession() {
        return new StoredSession(sessionDate, getDistance(), getTotalSessionTime(), getTimePerKm(), selectedSpeed, sessionComment, getRoute());
    }

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
            kalmanFilter.predict(a[0], a[1]);
            kalmanFilter.update(currentLocation.getSpeed(), deltaTime / 1000);
            double[] result = kalmanFilter.getState();
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

    public void setUnitOfVelocity(UnitOfVelocity unitOfVelocity) {
        this.unitOfVelocity = unitOfVelocity;
    }

    public static class StoredSession implements Serializable{
        private final double totalDistance;
        private final String totalTime;
        private static final long serialVersionUID = 0L;
        private LocalDate date;
        private double selectedSpeed;
        private ArrayList<String> timePerKm;
        private String sessionComment;
        private ArrayList<Point> route;

        public StoredSession(LocalDate date, double distance, String time, ArrayList<String> timePerKm, double selectedSpeed, String sessionComment, List<Point> route){
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
        public String getSelectedSpeed(){
            return Double.toString(selectedSpeed);
        }
        public ArrayList<Point> getRoute(){return route;}
    }
}

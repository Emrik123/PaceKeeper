package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.location.Location;
import android.text.format.DateUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Session {
    private ArrayList<Location> route;
    private ArrayList<Double> storedSpeedArray;
    private ArrayList<String> timePerKm;
    private Location currentLocation;
    private double distance;
    private double selectedSpeed;
    private double currentSpeed;
    private double setConversionUnit = 3.6;
    private boolean isRunning;
    private final LocalDate sessionDate;
    private String speedDisplayMode = "kmh";
    private final StopWatch stopwatch;
    private Boolean discardLocation = false;
    private long timeExceptCurrentKm;
    private int kmDistance;
    private Kalman kalmanFilter;

    public Session(double selectedSpeed){
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

    public String updateTime(){
        long currentTimeMillis = stopwatch.getTime();

        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public void updateSessionData() {
        int roundedDistance = (int) getDistance();
        if(roundedDistance>=kmDistance){
            long time = getTotalTime() - getTimeExceptCurrentKm();
            addTimePerKm(time);
            addTime(time);
            kmDistance+=1000;
        }
    }

    public void addTime(long kmTime){
        timeExceptCurrentKm+=kmTime;
    }

    public long getTimeExceptCurrentKm(){
        return timeExceptCurrentKm;
    }

    public void setSelectedSpeed(double speed){
        this.selectedSpeed = speed;
    }

    public void setConversionUnit(double value){
        this.setConversionUnit = value;
    }

    public void pauseSession(){
        isRunning = false;
        discardLocation = true;
        stopwatch.suspend();
    }

    public void continueSession(){
        isRunning = true;
        stopwatch.resume();
    }

    public void killSession(){
        isRunning = false;

    }

    public StoredSession getSerializableSession(){
        return new StoredSession(getSessionDate(), getDistance(), getTotalSessionTime(), getTimePerKm());
    }

    public void updateLocation(Location location) {
        if(currentSpeed > 1 && isRunning) {
            if (!discardLocation) {
                distance += location.distanceTo(currentLocation);
                route.add(currentLocation);
            } else {
                discardLocation = false;
            }
        }
        this.currentLocation = location;
        double[] result = kalmanFilter.update(currentLocation.getSpeed());
        this.currentSpeed = result[1];
        storedSpeedArray.add(currentSpeed);
    }

    public boolean getRunning(){
        return isRunning;
    }

    public Location getCurrentLocation(){
        return currentLocation;
    }

    public double getSelectedSpeed(){
        return selectedSpeed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public String getFormattedSelectedSpeed(){
        if (speedDisplayMode.equals("kmh")){
            return Double.toString(selectedSpeed*setConversionUnit) + " km/h";
        }
        else {
            return ((long)(1000/selectedSpeed)) + "min/km";
        }
    }

    public String getFormattedSpeed(){
        if (speedDisplayMode.equals("kmh")){
            return Double.toString(currentSpeed*setConversionUnit);
        }
        else {
            return DateUtils.formatElapsedTime((long)(1000/currentSpeed)) + "min/km";
        }
    }


    public double getCurrentSpeedMinPerKm(){
        return (1000/currentSpeed)/60;
    }

    public double getDistance(){
        return distance;
    }

    public LocalDate getSessionDate(){
        return sessionDate;
    }

    public String getTotalSessionTime(){
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

    public double getSetConversionUnit(){
        return setConversionUnit;
    }

    public ArrayList<Location> getRoute(){
        return route;
    }

    public void setSpeedDisplayMode(String mode){
        speedDisplayMode = mode;
    }

    public String getSpeedDisplayMode(){
        return speedDisplayMode;
    }

    public double calculateAverageSpeed(){
        double avg = 0;
        for(Double d : storedSpeedArray){
            avg+=d;
        }
        return avg/storedSpeedArray.size();
    }

    public long getTotalTime(){
        return stopwatch.getTime();
    }

    public void addTimePerKm(long time){
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


    public ArrayList<String> getTimePerKm(){
        return timePerKm;
    }


    public static class StoredSession implements Serializable{

        private static final AtomicInteger idCount = new AtomicInteger(0);
        private final double totalDistance;
        private final String totalTime;
        private int sessionID;
        private static final long serialVersionUID = 0L;
       private LocalDate date;

        private ArrayList<String> timePerKm;

        private String sessionComment;



        public StoredSession( LocalDate date, double distance, String time, ArrayList<String> timePerKm){
            this.totalTime = time;
            this.totalDistance = distance;
            this.date = date;
            this.timePerKm = timePerKm;

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

        public LocalDate getDate(){
            return date;
        }

        public String getSessionComment(){
            return sessionComment;
        }
    }
}

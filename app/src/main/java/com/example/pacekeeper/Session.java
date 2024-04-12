package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.location.Location;
import android.text.format.DateUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Session implements Serializable {
    private ArrayList<Location> route;
    private ArrayList<Double> storedSpeedArray;
    private Location currentLocation;
    private double distance;
    private double selectedSpeed;
    private double currentSpeed;
    private double setConversionUnit = 3.6;
    private long startTimeMillis;
    private boolean isRunning;
    private final LocalDate sessionDate;
    private String speedDisplayMode = "kmh";
    private StopWatch stopwatch;

    public Session(double selectedSpeed){
        this.sessionDate = LocalDate.now();
        this.selectedSpeed = selectedSpeed;
        startTimeMillis = System.currentTimeMillis();
        this.isRunning = true;
        route = new ArrayList<>();
        storedSpeedArray = new ArrayList<>();
    }

    public String updateTime(){
        long currentTimeMillis = System.currentTimeMillis()-startTimeMillis;

        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public void startSessionTime() {

    }

    public void setSelectedSpeed(double speed){
        this.selectedSpeed = speed;
    }

    public void setConversionUnit(double value){
        this.setConversionUnit = value;
    }

    public void pauseSession(){
        isRunning = false;
    }

    public void continueSession(){
        isRunning = true;
    }

    public void killSession(){
        isRunning = false;
    }

    public void updateLocation(Location location){
        if(currentSpeed > 1 && isRunning){
            distance+= location.distanceTo(currentLocation);
            route.add(currentLocation);
        }
        this.currentLocation = location;
        this.currentSpeed = currentLocation.getSpeed();
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

    public double getSetConversionUnit(){
        return setConversionUnit;
    }

    public ArrayList<Location> getRoute(){
        return route;
    }

    public void setSpeedDisplayMode(String mode){
        speedDisplayMode = mode;
    }

    public double calculateAverageSpeed(){
        double avg = 0;
        for(Double d : storedSpeedArray){
            avg+=d;
        }
        return avg/storedSpeedArray.size();
    }
}

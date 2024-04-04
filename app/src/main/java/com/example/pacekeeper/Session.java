package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.location.Location;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class Session implements Serializable {
    private ArrayList<Location> route;
    private Location currentLocation;
    private double distance;
    private double selectedSpeed;
    private double currentSpeed;
    private double setConversionUnit = 3.6;
    private long startTimeMillis;
    private boolean isRunning;
    private LocalDate sessionDate;

    public Session(double selectedSpeed){
        this.sessionDate = LocalDate.now();
        this.selectedSpeed = selectedSpeed;
        startTimeMillis = System.currentTimeMillis();
        this.isRunning = true;
        route = new ArrayList<>();
    }

    public LocalDate getSessionDate(){
        return sessionDate;
    }

    public String updateTime(){
        long currentTimeMillis = System.currentTimeMillis()-startTimeMillis;

        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeString;
    }

    public Location getCurrentLocation(){
        return currentLocation;
    }

    public void changeSelectedSpeed(double speed){
        this.selectedSpeed = speed;
    }

    public double getSelectedSpeed(){
        return selectedSpeed;
    }

    public void pauseSession(){
        isRunning = false;
    }

    public boolean getRunning(){
        return isRunning;
    }

    public void updateLocation(Location location){
        if(currentSpeed > 1 && isRunning){
            distance+= location.distanceTo(currentLocation);
            route.add(currentLocation);
        }
        this.currentLocation = location;
        this.currentSpeed = currentLocation.getSpeed();
    }

    public double getCurrentSpeed(){
        return currentSpeed * setConversionUnit;
    }

    public double getDistance(){
        return distance;
    }


}

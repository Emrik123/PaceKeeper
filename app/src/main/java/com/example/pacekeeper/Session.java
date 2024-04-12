package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Session implements Serializable {
    private ArrayList<StoredLocation> route;
    private ArrayList<Double> storedSpeedArray;
    private Location currentLocation;
    private double distance;
    private double selectedSpeed;
    private double currentSpeed;
    private double setConversionUnit = 3.6;
    private long startTimeMillis;
    private boolean isRunning;
    private final LocalDate sessionDate;

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
            route.add(new StoredLocation(currentLocation.getLongitude(), currentLocation.getLatitude(), currentLocation.getSpeed(), updateTime()));
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

    public double getCurrentSpeed(){
        return currentSpeed * setConversionUnit;
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

    public ArrayList<StoredLocation> getRoute(){
        return route;
    }

    public double calculateAverageSpeed(){
        double avg = 0;
        for(Double d : storedSpeedArray){
            avg+=d;
        }
        return avg/storedSpeedArray.size();
    }

    public void storeSessionToMemory(Context context){
        try{
            StoredSession temp = new StoredSession(route, sessionDate, distance, updateTime());
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "testDataFile_" + temp.dateStamp + "_" + temp.sessionID +".dat");
            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
            oos.writeObject(temp);
            oos.flush();
            oos.close();
            Toast.makeText(context, "File stored", Toast.LENGTH_SHORT).show();
            System.out.println("File successfully created and stored in: " + file.getPath());
        }catch (IOException e){
            Log.e("Store session", "ObjectStream couldn't be initialized, perhaps file not found " + e);
        }
    }

    public static class StoredLocation implements Serializable{
        private final double longitude;
        private final double latitude;
        private final double speed;
        private final String timeStamp;
        public StoredLocation(double longitude, double latitude, double speed, String timeStamp){
            this.timeStamp = timeStamp;
            this.longitude = longitude;
            this.latitude = latitude;
            this.speed = speed;
        }
    }

    public static class StoredSession implements Serializable{
        private ArrayList<StoredLocation> route;
        private LocalDate dateStamp;
        private static final AtomicInteger idCount = new AtomicInteger(0);
        private final double totalDistance;
        private final String totalTime;
        private int sessionID;

        public StoredSession(ArrayList<StoredLocation> route, LocalDate dateStamp, double distance, String time){
            this.totalTime = time;
            this.totalDistance = distance;
            this.route = route;
            this.dateStamp = dateStamp;
            this.sessionID = idCount.incrementAndGet();
        }
    }
}
//File successfully created and store in: /data/user/0/com.example.pacekeeper/files/testDataFile.dat
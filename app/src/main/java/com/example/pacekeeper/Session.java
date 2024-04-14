package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
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
            route.add(new StoredLocation(currentLocation.getLongitude(), currentLocation.getLatitude(), currentLocation.getSpeed(),
                    updateTime(), location.distanceTo(currentLocation)));
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

    public static class Generator{
        private ArrayList<StoredSession> storedSessions;

        public Generator(){
            storedSessions = new ArrayList<>();
        }
        public void loadAndPrint(){
            ArrayList<StoredSession> temp = readFile();
            ArrayList<StoredLocation> locationList = temp.get(0).route;
            ArrayList<StoredLocation> locationList2 = temp.get(1).route;

            int sessionID=1;
            System.out.println("Session : " + sessionID);
            for (StoredLocation location : locationList) {
                System.out.println("Time: " + location.timeStamp + " ||     Speed: " + location.speed);
            }
            System.out.println("Session : " + sessionID+1);
            for (StoredLocation storedLocation : locationList2) {
                System.out.println("Time: " + storedLocation.timeStamp + " ||     Speed: " + storedLocation.speed);
            }
        }

        public ArrayList<StoredSession> readFile(){
            LocalDate date = LocalDate.of(2024, Month.APRIL, 11);
            LocalDate date2 = LocalDate.of(2024, Month.APRIL, 12);
            try{
                ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get("app/src/DataSet/testDataFile_" + date + "_" + 2 + ".dat")));
                ObjectInputStream ois2 = new ObjectInputStream(Files.newInputStream(Paths.get("app/src/DataSet/testDataFile_" + date2 + "_" + 1 + ".dat")));
                storedSessions.add((StoredSession) ois.readObject());
                storedSessions.add((StoredSession) ois2.readObject());
                ois.close();
                ois2.close();

            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
            return storedSessions;
        }
    }


    public static class StoredLocation implements Serializable{
        private final double longitude;
        private final double latitude;
        private final double speed;
        private final String timeStamp;
        private double deltaDistance;
        public StoredLocation(double longitude, double latitude, double speed, String timeStamp, double dD){
            this.timeStamp = timeStamp;
            this.longitude = longitude;
            this.latitude = latitude;
            this.speed = speed;
            this.deltaDistance = dD;
        }

        public double getSpeed() {
            return speed;
        }

        public double getDeltaDistance() {
            return deltaDistance;
        }

        public String getTimeStamp() {
            return timeStamp;
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

        public ArrayList<StoredLocation> getRoute() {
            return route;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public String getTotalTime() {
            return totalTime;
        }
    }
}
//File successfully created and store in: /data/user/0/com.example.pacekeeper/files/testDataFile.dat
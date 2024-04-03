package com.example.pacekeeper;

import static com.example.pacekeeper.R.*;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunnerView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunnerView extends Fragment {
    private NumberPicker speedInput;
    private TextView speedDisplay;
    private TextView timeDisplay;
    private TextView distanceDisplay;
    private ImageButton pauseButton;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location location;
    private Kalman kalman;
    private double currentSpeed;
    private double speed;
    private double distance;
    private long startTimeMillis;
    private final double UPDATE_INTERVAL_MS = 500;
    private final double MEASUREMENT_NOISE_M = 5;
    private final double ACCEL_NOISE_MS = 0.1;
    private Bundle savedInstance;
    private MediaPlayer tooSlowAlert;
    private MediaPlayer tooFastAlert;

    public RunnerView() {
        // Required empty public constructor
    }

    public static RunnerView newInstance(int speed) {
        RunnerView fragment = new RunnerView();
        Bundle args = new Bundle();
        // You can pass arguments if needed
        args.putInt("speed", speed);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("VisibleForTests")
    @Override


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        this.savedInstance = savedInstanceState;
        timeDisplay = rootView.findViewById(R.id.time);
        speedDisplay = rootView.findViewById(R.id.speedDisplay);
        distanceDisplay = rootView.findViewById(R.id.distanceDisplay);
        pauseButton = rootView.findViewById(R.id.pauseButtonLogo);
        startTimeMillis = System.currentTimeMillis();
        tooFastAlert = MediaPlayer.create(getActivity(), raw.toofast_notification);
        tooSlowAlert = MediaPlayer.create(getActivity(), raw.tooslow_notification);

        Bundle args = getArguments();
        if (args != null) {
            speed = args.getInt("speed", 0);
        }

        locationRequest = new LocationRequest();
        locationRequest.setInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        // Kommer att fixa ett fungerande filter när jag förstått mig på den här skiten
        // Ignore for now
        kalman = new Kalman(UPDATE_INTERVAL_MS, MEASUREMENT_NOISE_M, ACCEL_NOISE_MS);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() != null) {
                    //Location rawLocation = locationResult.getLastLocation();
                    //Location filteredLocation = kalman.predictAndCorrect(rawLocation);
                    Location lastLocation = locationResult.getLastLocation();
                    if(location != null){
                        if(currentSpeed > 1){
                            distance+= lastLocation.distanceTo(location);
                        }
                    }
                    location = lastLocation;
                    currentSpeed = location.getSpeed();
                    updateUI();
                }
            }


        };

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //TODO få upp pausmenyn.
            }
        });


        start();

        return rootView;
    }

    @SuppressLint("SetTextI18n")
    public void updateUI(){
        int roundedDistance = (int) distance;
        distanceDisplay.setText(Integer.toString(roundedDistance));
        currentSpeed = currentSpeed * 3.6;
        int roundedSpeed = (int) currentSpeed;
        String s1 = Double.toString(roundedSpeed);
        String s2 = getString(R.string.viewString);
        String s3 = getString(R.string.viewString2);
        speedDisplay.setText(s1);


        if(roundedSpeed == speed || (roundedSpeed >= speed -1 && roundedSpeed <= speed +1)){
            speedDisplay.setTextColor(Color.parseColor("green"));
        }else if(roundedSpeed > speed+1){
            speedDisplay.setTextColor(Color.parseColor("red"));
            tooFastAlert.start();
        }else if(roundedSpeed < speed-1){
            speedDisplay.setTextColor(Color.parseColor("blue"));
            tooSlowAlert.start();
        }



        long currentTimeMillis = System.currentTimeMillis()-startTimeMillis;

        int hours = (int) (currentTimeMillis / (1000 * 60 * 60)) % 24;
        int minutes = (int) ((currentTimeMillis / (1000 * 60)) % 60);
        int seconds = (int) (currentTimeMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeDisplay.setText(timeString);

    }





    private void start() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

}
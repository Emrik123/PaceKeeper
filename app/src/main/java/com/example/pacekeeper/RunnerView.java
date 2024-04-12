package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
    private Kalman kalman;
    private double speed;
    private final double UPDATE_INTERVAL_MS = 500;
    private final double MEASUREMENT_NOISE_M = 5;
    private final double ACCEL_NOISE_MS = 0.1;
    private Bundle savedInstance;
    private MediaPlayer tooSlowAlert;
    private MediaPlayer tooFastAlert;
    private Session currentSession;
    private ArrayList<Session> sessionHistory; // For storing session when you stop a current one, also for loading up existing sessions from file.
    private FeedbackHandler feedback;
    private String speedDisplayMode;

    public RunnerView() {
        // Kommer att fixa ett fungerande filter när jag förstått mig på den här skiten
        // Ignore for now
        kalman = new Kalman(UPDATE_INTERVAL_MS, MEASUREMENT_NOISE_M, ACCEL_NOISE_MS);
        sessionHistory = new ArrayList<>();
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
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            feedback = (FeedbackHandler) intent.getSerializableExtra("feedbackHandler");
            speedDisplayMode = intent.getStringExtra("speedDisplayMode");
        }

        Bundle args = getArguments();
        if (args != null) {
            speed = args.getInt("speed", 0);
        }
        locationRequest = new LocationRequest();
        locationRequest.setInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSession.getRunning()) {
                    currentSession.pauseSession();
                    feedback.stopFeedback();
                    stopLocationUpdates();
                } else {
                    currentSession.continueSession();
                    startLocationUpdates();
                    feedback.runFeedback(currentSession.getSelectedSpeed());
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLastLocation() != null) {
                    //Location rawLocation = locationResult.getLastLocation(); <- Icke Kalman-filtrerad location med noise
                    //Location filteredLocation = kalman.predictAndCorrect(rawLocation); <- Kalman filtrerad location utan noise
                    Location lastLocation = locationResult.getLastLocation();
                    currentSession.updateLocation(lastLocation);
                    feedback.setRunning(currentSession.getRunning());
                    feedback.setCurrentSpeed(currentSession.getCurrentSpeed());
                    updateUI();
                }
            }
        };
        start();
        return rootView;
    }

    @SuppressLint("SetTextI18n")
    public void updateUI(){
        if(currentSession.getRunning()){
            int roundedDistance = (int) currentSession.getDistance();
            distanceDisplay.setText(Integer.toString(roundedDistance));
            if(speedDisplayMode.equals("kmh")){
                speedDisplay.setText(currentSession.getFormattedSpeed().substring(0,currentSession.getFormattedSpeed().indexOf(".")+2));
            }
            else{
                speedDisplay.setText(currentSession.getFormattedSpeed().substring(0,currentSession.getFormattedSpeed().indexOf(":")+3));
            }
            /*if(roundedSpeed == currentSession.getSelectedSpeed() ||
                    (roundedSpeed >= currentSession.getSelectedSpeed() -1
                            && roundedSpeed <= currentSession.getSelectedSpeed() +1)){
                speedDisplay.setTextColor(Color.parseColor("green"));
            }else if(roundedSpeed > currentSession.getSelectedSpeed()+1){
                speedDisplay.setTextColor(Color.parseColor("red"));
                tooFastAlert.start();
            }else if(roundedSpeed < currentSession.getSelectedSpeed()-1){
                speedDisplay.setTextColor(Color.parseColor("blue"));
                tooSlowAlert.start();
            }*/
            timeDisplay.setText(currentSession.updateTime());
        }
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void start() {
        startLocationUpdates();
        currentSession = new Session(speed);
        currentSession.setSpeedDisplayMode(speedDisplayMode);
        feedback.runFeedback(currentSession.getSelectedSpeed());
    }
}
package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RunnerView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunnerView extends Fragment {
    private MainActivity mainActivity;
    private SessionManager sessionManager;
    private NumberPicker speedInput;
    private TextView speedDisplay;
    private TextView timeDisplay;
    private TextView distanceDisplay;
    private ImageButton pauseButton;
    private ImageButton resumeButton;
    private ImageButton stopButton;
    private ImageButton settingsButton;
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
    private int kmDistance;
    private String kmTime;
    private FragmentManager fragmentManager;
    private Handler interfaceUpdateHandler;
    private Runnable uiUpdates;
    private TextToSpeech textToSpeech;

    public RunnerView() {
        // Kommer att fixa ett fungerande filter när jag förstått mig på den här skiten
        // Ignore for now
        kalman = new Kalman(UPDATE_INTERVAL_MS, MEASUREMENT_NOISE_M, ACCEL_NOISE_MS);
        sessionHistory = new ArrayList<>();
        kmDistance = 1000;
    }


    public static RunnerView newInstance(MainActivity mainActivity, double speed) {
        RunnerView fragment = new RunnerView();
        Bundle args = new Bundle();
        // You can pass arguments if needed
        args.putDouble("speed", speed);
        fragment.setArguments(args);
        fragment.setMainActivity(mainActivity);
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
        resumeButton = rootView.findViewById(R.id.playButtonLogo);
        stopButton = rootView.findViewById(R.id.stopButtonLogo);
        settingsButton = rootView.findViewById(R.id.settingsButton);
        stopButton.setVisibility(View.INVISIBLE);
        resumeButton.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.INVISIBLE);
        fragmentManager = mainActivity.getSupportFragmentManager();
        Intent intent = requireActivity().getIntent();
        interfaceUpdateHandler = new Handler(Looper.getMainLooper());

        if (intent != null) {
            feedback = (FeedbackHandler) intent.getSerializableExtra("feedbackHandler");
            speedDisplayMode = intent.getStringExtra("speedDisplayMode");
        }

        Bundle args = getArguments();
        if (args != null) {
            speed = args.getDouble("speed", 0);
        }
        if (mainActivity != null) {
            sessionManager = mainActivity.getSessionManager();
        }
        locationRequest = new LocationRequest();
        locationRequest.setInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval((long) UPDATE_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseButton.setVisibility(View.INVISIBLE);
                resumeButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                settingsButton.setVisibility(View.VISIBLE);
                currentSession.pauseSession();
                feedback.stopFeedback();
                stopLocationUpdates();
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.INVISIBLE);
                settingsButton.setVisibility(View.INVISIBLE);
                currentSession.continueSession();
                startLocationUpdates();
                feedback.runFeedback(currentSession.getSelectedSpeed());
                CharSequence lego = "Kom igen daa Britt-Marie, schör för faaaan";
                textToSpeech.speak(lego, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySettingsView();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableAllMainActivityButtons();
                sessionManager.add(currentSession);
              currentSession.killSession();
                getParentFragmentManager().popBackStackImmediate();
            }
        });

        uiUpdates = new Runnable() {
            @Override
            public void run() {
                updateUI();

                interfaceUpdateHandler.postDelayed(uiUpdates, (long) UPDATE_INTERVAL_MS);
            }
        };

        textToSpeech = new TextToSpeech(requireActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
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
                    // discard location
                    currentSession.updateLocation(lastLocation);
                    feedback.setRunning(currentSession.getRunning());
                    feedback.setCurrentSpeed(currentSession.getCurrentSpeed());
                    currentSession.updateSessionData();
                }
            }
        };
        start();
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
//        mainActivity.updateSettingsRunnersView();
    }

    @Override
    public void onPause() {
        super.onPause();
        interfaceUpdateHandler.removeCallbacks(uiUpdates);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
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

            timeDisplay.setText(currentSession.updateTime());
        }
    }

    public void runUiUpdates() {
        interfaceUpdateHandler.post(uiUpdates);
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
        runUiUpdates();

    }

    public void setSpeedDisplayMode(String speedDisplayMode){
        this.speedDisplayMode = speedDisplayMode;
    }


    private void displaySettingsView(){
        fragmentManager.beginTransaction().add(R.id.fragment_container, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    public Session getCurrentSession(){
        return currentSession;
    }

    public TextView getSpeedDisplay(){
        return speedDisplay;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void enableAllMainActivityButtons(){
        mainActivity.enableAllButtons();
    }
}
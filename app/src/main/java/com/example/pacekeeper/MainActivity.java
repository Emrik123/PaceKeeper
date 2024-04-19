package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private Activity mainActivity;
    private SessionManager sessionManager;
    private double speed;

    private ImageButton confirm;
    private NumberPicker leftNPicker;
    private NumberPicker rightNPicker;
    private ImageButton settingsButton;
    private Boolean vibration;
    private Boolean audio;
    private Boolean autoSaveSession;
    private String feedbackFrequency;
    private UnitOfVelocity unitOfVelocity;
    private SharedPreferences preferences;
    private FeedbackHandler feedback;
    private ImageButton sessions;
    private FragmentManager fragmentManager;
    private TextView unitTextView;
    private RunnerView runnerView;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        sessionManager = new SessionManager();
        sessionManager.readFile(this);
        //FragmentManager fragmentManager = getSupportFragmentManager();
        loadSharedPreferences();
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        confirm = findViewById(R.id.confirmButton);
        settingsButton = findViewById(R.id.settingsButton);
        leftNPicker = findViewById(R.id.leftNPicker);
        rightNPicker = findViewById(R.id.rightNPicker);
        feedback = new FeedbackHandler(getApplicationContext());
        sessions = findViewById(R.id.historyButton);
        unitTextView = findViewById(R.id.unitTextView);
        setFeedbackPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSpeed(unitOfVelocity);
                if (speed != 0) {
                    Toast.makeText(MainActivity.this, "Speed stored.", Toast.LENGTH_SHORT).show();
                    displayRunnerView(speed);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a valid speed.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                displaySettingsView();
            }
        });



        sessions.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick (View v){
                displaySessionsView();
                }
            });


    }

    public void setSpeed(UnitOfVelocity unitOfVelocity) {
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
            default:
                speed = leftNPicker.getValue();
                speed += (rightNPicker.getValue() / 10.0);
                speed /= 3.6;
                break;
            case MIN_PER_KM:
                double seconds = (leftNPicker.getValue() * 60);
                seconds += rightNPicker.getValue();
                speed = 1000 / seconds;
                break;
        }
    }

    public void updateSettings(){
        loadSharedPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);
    }

    public void updateSettingsRunnersView(){
        if(runnerView != null){
            setFeedbackPreferences();
            //runnerView.setSpeedDisplayMode(speedDisplayMode);
            runnerView.setUnitOfVelocity(unitOfVelocity);
            runnerView.getCurrentSession().setUnitOfVelocity(unitOfVelocity);
        }
    }

    public void loadSharedPreferences(){
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        vibration = preferences.getBoolean("vibrationFeedback", true);
        audio = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
        autoSaveSession = preferences.getBoolean("autoSaveSessions", false);

        String unit = preferences.getString("speedDisplayMode", "minPerKm");
        switch (unit) {
            case "kmh":
                unitOfVelocity = UnitOfVelocity.KM_PER_HOUR;
                break;
            case "minPerKm":
                unitOfVelocity = UnitOfVelocity.MIN_PER_KM;
                break;
        }
    }

    public void setFeedbackPreferences() {
        feedback.setVibrationAllowed(vibration);
        feedback.setAudioAllowed(audio);
        feedback.setFeedbackFrequency(feedbackFrequency);
        feedback.setUnitOfVelocity(unitOfVelocity);
    }

    @SuppressLint("SetTextI18n")
    public void setUnit(){
        unitTextView.setText(unitOfVelocity.toString());
    }

    private void displaySessionsView(){
        SessionFragment sessionFragment = SessionFragment.newInstance(sessionManager);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, sessionFragment); // Replace fragment_container with the id of your container layout
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

    private void displaySettingsView(){
        fragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    private void displayRunnerView(double speed) {
       // disableAllButtons();
        loadSharedPreferences();
        setFeedbackPreferences();
        // Create a new instance of RunnerView fragment with the selected speed
        runnerView = RunnerView.newInstance(this, speed);

        Bundle bundle = new Bundle();
        bundle.putDouble("speed", speed);
        getIntent().putExtra("feedbackHandler", feedback);
        //getIntent().putExtra("speedDisplayMode", speedDisplayMode);
        getIntent().putExtra("unitOfVelocity", unitOfVelocity);


        // Replace the current fragment with the RunnerView fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, runnerView); // Replace fragment_container with the id of your container layout
        transaction.addToBackStack("mainActivity"); // Optional: Add transaction to back stack
        transaction.commit();
    }

    public void setPickerStyle(UnitOfVelocity unitOfVelocity) {
        switch (unitOfVelocity) {
            case MIN_PER_KM:
                leftNPicker.setMinValue(1);
                leftNPicker.setMaxValue(59);
                rightNPicker.setMaxValue(0);
                rightNPicker.setMaxValue(59);
                findViewById(R.id.dot).setVisibility(View.INVISIBLE);
                findViewById(R.id.minutesTag).setVisibility(View.VISIBLE);
                findViewById(R.id.secondsTag).setVisibility(View.VISIBLE);
                break;
            case KM_PER_HOUR:
                leftNPicker.setMinValue(4);
                leftNPicker.setMaxValue(40);
                rightNPicker.setMaxValue(0);
                rightNPicker.setMaxValue(9);
                findViewById(R.id.dot).setVisibility(View.VISIBLE);
                findViewById(R.id.minutesTag).setVisibility(View.INVISIBLE);
                findViewById(R.id.secondsTag).setVisibility(View.INVISIBLE);
                break;
        }
    }

    public SessionManager getSessionManager(){
        return sessionManager;
    }


    public void disableAllButtons(){
        sessions.setEnabled(false);
        settingsButton.setEnabled(false);
        confirm.setEnabled(false);
    }
    public void enableAllButtons(){
        sessions.setEnabled(true);
        settingsButton.setEnabled(true);
        confirm.setEnabled(true);

    }

}


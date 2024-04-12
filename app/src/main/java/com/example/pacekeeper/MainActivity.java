package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private int speed;
    private Button confirm;
    private NumberPicker numberPicker;
    private ImageButton settingsButton;
    private Boolean vibration;
    private Boolean audio;
    private Boolean autoSaveSession;
    private String feedbackFrequency;
    private String speedDisplayMode;
    private SharedPreferences preferences;
    private FeedbackHandler feedback;
    private ImageButton sessions;
    private FragmentManager fragmentManager;
    private TextView unitTextView;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        loadSharedPreferences();
        confirm = findViewById(R.id.confirmButton);
        settingsButton = findViewById(R.id.settingsButton);
        numberPicker = findViewById(R.id.leftNPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(40);
        feedback = new FeedbackHandler(getApplicationContext());
        sessions = findViewById(R.id.historyButton);
        unitTextView = findViewById(R.id.unitTextView);
        setFeedbackPreferences();
        setUnit();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speed = numberPicker.getValue();
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
                displaySessionsView("test", "test");
                }
            });
    }

    public void updateSettings(){
        loadSharedPreferences();
        setUnit();
    }

    public void loadSharedPreferences(){
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        vibration = preferences.getBoolean("vibrationFeedback", true);
        audio = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
        autoSaveSession = preferences.getBoolean("autoSaveSessions", false);
        speedDisplayMode = preferences.getString("speedDisplayMode", "minPerKm");
    }

    public void setFeedbackPreferences() {
        feedback.setVibrationAllowed(vibration);
        feedback.setAudioAllowed(audio);
        feedback.setFeedbackFrequency(feedbackFrequency);
    }

    @SuppressLint("SetTextI18n")
    public void setUnit(){
        System.out.println(speedDisplayMode);
        if(speedDisplayMode.equals("minPerKm")){
            unitTextView.setText("min/km");
        }
        else{
            unitTextView.setText("km/h");
        }
    }

    private void displaySessionsView(String arg1, String arg2){
        SessionFragment sessionFragment = SessionFragment.newInstance(arg1, arg2);
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

    private void displayRunnerView(int speed) {
        loadSharedPreferences();
        setFeedbackPreferences();
        // Create a new instance of RunnerView fragment with the selected speed
        RunnerView runnerView = RunnerView.newInstance(speed);

        Bundle bundle = new Bundle();
        bundle.putInt("speed", speed);
        getIntent().putExtra("feedbackHandler", feedback);
        getIntent().putExtra("speedDisplayMode", speedDisplayMode);

        // Replace the current fragment with the RunnerView fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, runnerView); // Replace fragment_container with the id of your container layout
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

}

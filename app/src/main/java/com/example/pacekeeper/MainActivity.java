package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity {
    private int speed;
    private Button confirm;
    private NumberPicker numberPicker;
    private ImageButton settingsButton;
    private Boolean vibration;
    private Boolean audio;
    private String feedbackFrequency;
    private SharedPreferences preferences;
    private FeedbackHandler feedback;
    private ImageButton sessions;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getSupportFragmentManager();
        loadSharedPreferences();
        setContentView(R.layout.activity_main);
        confirm = findViewById(R.id.confirmButton);
        settingsButton = findViewById(R.id.settingsButton);
        numberPicker = findViewById(R.id.leftNPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(40);
        feedback = new FeedbackHandler(getApplicationContext());
        setFeedbackPreferences();
        sessions = findViewById(R.id.historyButton);

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
                fragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment.class, null)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }
        });

        sessions.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick (View v){
                displaySessionsView("test", "test");
                }
            });
    }

    public void loadSharedPreferences(){
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        vibration = preferences.getBoolean("vibrationFeedback", true);
        audio = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
    }

    public void setFeedbackPreferences() {
        feedback.setVibrationAllowed(vibration);
        feedback.setAudioAllowed(audio);
        feedback.setFeedbackFrequency(feedbackFrequency);
    }

    private void displaySessionsView(String arg1, String arg2){
        SessionFragment sessionFragment = SessionFragment.newInstance(arg1, arg2);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, sessionFragment); // Replace fragment_container with the id of your container layout
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

    private void displayRunnerView(int speed) {
        loadSharedPreferences();
        setFeedbackPreferences();
        // Create a new instance of RunnerView fragment with the selected speed
        RunnerView runnerView = RunnerView.newInstance(speed);

        Bundle bundle = new Bundle();
        bundle.putInt("speed", speed);
        getIntent().putExtra("feedbackHandler", feedback);

        // Replace the current fragment with the RunnerView fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, runnerView); // Replace fragment_container with the id of your container layout
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

}

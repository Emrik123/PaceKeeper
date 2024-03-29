package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity {
    private int speed;
    private Button confirm;
    private Vibrator vibrator;
    private VibrationEffect increaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{150, 75, 150, 75, 150}, new int[]{255, 0, 255, 0, 255}, -1); //Creates Vibration pattern for being too slow
    private VibrationEffect decreaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{900}, new int[]{255}, -1); //Creates Vibration pattern for being too fast
    private NumberPicker numberPicker;
    private ImageButton settingsButton;
    private Boolean vibration;
    private Boolean audio;
    private String feedbackFrequency;
    private SharedPreferences preferences;

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
    }

    public void loadSharedPreferences(){
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        vibration = preferences.getBoolean("vibrationFeedback", true);
        audio = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
    }

    private void displayRunnerView(int speed) {
        // Create a new instance of RunnerView fragment with the selected speed
        RunnerView runnerView = RunnerView.newInstance(speed);

        // Replace the current fragment with the RunnerView fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, runnerView); // Replace fragment_container with the id of your container layout
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
    }

}

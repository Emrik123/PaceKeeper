package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Activity mainActivity;
    private SessionManager sessionManager;
    private double targetVelocity;
    private ImageButton startSessionButton;
    private NumberPicker leftNumberPicker;
    private NumberPicker rightNumberPicker;
    private ImageButton settingsButton;
    private Boolean vibrationEnabled;
    private Boolean audioEnabled;
    private Boolean autoSaveSessionEnabled;
    private String feedbackFrequency;
    private UnitOfVelocity unitOfVelocity;
    private SharedPreferences preferences;
    private FeedbackHandler feedbackHandler;
    private ImageButton previousSessionsButton;
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
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        startSessionButton = findViewById(R.id.start_button);
        settingsButton = findViewById(R.id.settings_button);
        leftNumberPicker = findViewById(R.id.left_n_picker);
        rightNumberPicker = findViewById(R.id.right_n_picker);
        feedbackHandler = new FeedbackHandler(getApplicationContext());
        previousSessionsButton = findViewById(R.id.previous_sessions_button);
        unitTextView = findViewById(R.id.unitTextView);
        loadSharedPreferences();
        setFeedbackPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);

        startSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTargetVelocity(unitOfVelocity);
                if (targetVelocity != 0) {
                    displayRunnerView(targetVelocity);
                }
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySettingsView();
            }
        });

        previousSessionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySessionsView();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SensorUnitHandler.class));
    }

    public void setTargetVelocity(UnitOfVelocity unitOfVelocity) {
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                targetVelocity = leftNumberPicker.getValue();
                targetVelocity += (rightNumberPicker.getValue() / 10.0);
                targetVelocity /= 3.6;
                break;
            case MIN_PER_KM:
                double seconds = (leftNumberPicker.getValue() * 60);
                seconds += rightNumberPicker.getValue();
                targetVelocity = 1000 / seconds;
                break;
        }
    }

    public void updateSettings() {
        loadSharedPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);
    }

    public void updateSettingsRunnersView() {
        if (runnerView != null) {
            setFeedbackPreferences();
            runnerView.setUnitOfVelocity(unitOfVelocity);
            runnerView.getCurrentSession().setUnitOfVelocity(unitOfVelocity);
            runnerView.setUnitOfVelocityDisplay();
            runnerView.updateSelectedPaceUnit();
        }
    }

    public void loadSharedPreferences() {
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        vibrationEnabled = preferences.getBoolean("vibrationFeedback", true);
        audioEnabled = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
        autoSaveSessionEnabled = preferences.getBoolean("autoSaveSessions", false);
        String unit = preferences.getString("unitOfVelocity", "min/km");
        switch (unit.trim()) {
            case "km/h":
                unitOfVelocity = UnitOfVelocity.KM_PER_HOUR;
                break;
            case "min/km":
                unitOfVelocity = UnitOfVelocity.MIN_PER_KM;
                break;
        }
    }

    private void saveSharedPreferences() {
        SharedPreferences.Editor preferenceEditor;
        preferenceEditor = preferences.edit();
        if (unitOfVelocity == UnitOfVelocity.KM_PER_HOUR) {
            preferenceEditor.putInt("leftNumberPickerValueKmH", leftNumberPicker.getValue());
            preferenceEditor.putInt("rightNumberPickerValueKmH", rightNumberPicker.getValue());
        } else {
            preferenceEditor.putInt("leftNumberPickerValueMinKm", leftNumberPicker.getValue());
            preferenceEditor.putInt("rightNumberPickerValueMinKm", rightNumberPicker.getValue());
        }
        preferenceEditor.apply();
    }

    public void setFeedbackPreferences() {
        feedbackHandler.setVibrationAllowed(vibrationEnabled);
        feedbackHandler.setAudioAllowed(audioEnabled);
        feedbackHandler.setFeedbackFrequency(feedbackFrequency);
        feedbackHandler.setUnitOfVelocity(unitOfVelocity);
    }

    @SuppressLint("SetTextI18n")
    public void setUnit() {
        unitTextView.setText(unitOfVelocity.toString().toUpperCase(Locale.US));
    }

    private void displaySessionsView() {
        SessionFragment sessionFragment = SessionFragment.newInstance(sessionManager);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, sessionFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void displaySettingsView() {
        saveSharedPreferences();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    private void displayRunnerView(double speed) {
        loadSharedPreferences();
        setFeedbackPreferences();
        runnerView = RunnerView.newInstance(this, speed, autoSaveSessionEnabled);
        Bundle bundle = new Bundle();
        bundle.putDouble("speed", speed);
        getIntent().putExtra("feedbackHandler", feedbackHandler);
        getIntent().putExtra("unitOfVelocity", unitOfVelocity);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, runnerView);
        transaction.addToBackStack("mainActivity");
        transaction.commit();
    }

    public void setPickerStyle(UnitOfVelocity unitOfVelocity) {
        switch (unitOfVelocity) {
            case MIN_PER_KM:
                leftNumberPicker.setMinValue(1);
                leftNumberPicker.setMaxValue(59);
                rightNumberPicker.setMaxValue(0);
                rightNumberPicker.setMaxValue(59);
                findViewById(R.id.dot).setVisibility(View.INVISIBLE);
                findViewById(R.id.minutes_tag).setVisibility(View.VISIBLE);
                findViewById(R.id.seconds_tag).setVisibility(View.VISIBLE);
                leftNumberPicker.setValue(preferences.getInt("leftNumberPickerValueMinKm", 1));
                rightNumberPicker.setValue(preferences.getInt("rightNumberPickerValueMinKm", 0));
                break;
            case KM_PER_HOUR:
                leftNumberPicker.setMinValue(4);
                leftNumberPicker.setMaxValue(40);
                rightNumberPicker.setMaxValue(0);
                rightNumberPicker.setMaxValue(9);
                findViewById(R.id.dot).setVisibility(View.VISIBLE);
                findViewById(R.id.minutes_tag).setVisibility(View.INVISIBLE);
                findViewById(R.id.seconds_tag).setVisibility(View.INVISIBLE);
                leftNumberPicker.setValue(preferences.getInt("leftNumberPickerValueKmH", 4));
                rightNumberPicker.setValue(preferences.getInt("rightNumberPickerValueKmH", 0));
                break;
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}


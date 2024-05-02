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

    private ImageButton startSessionButton;
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
        leftNPicker = findViewById(R.id.left_n_picker);
        rightNPicker = findViewById(R.id.right_n_picker);
        feedback = new FeedbackHandler(getApplicationContext());
        previousSessionsButton = findViewById(R.id.previous_sessions_button);
        unitTextView = findViewById(R.id.unitTextView);
        loadSharedPreferences();
        setFeedbackPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);

        startSessionButton.setOnClickListener(new View.OnClickListener() {
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


        previousSessionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void updateSettings() {
        loadSharedPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);
    }

    public void updateSettingsRunnersView() {
        if (runnerView != null) {
            setFeedbackPreferences();
            //runnerView.setSpeedDisplayMode(speedDisplayMode);
            runnerView.setUnitOfVelocity(unitOfVelocity);
            runnerView.getCurrentSession().setUnitOfVelocity(unitOfVelocity);
        }
    }

    public void loadSharedPreferences() {
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        vibration = preferences.getBoolean("vibrationFeedback", true);
        audio = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
        autoSaveSession = preferences.getBoolean("autoSaveSessions", false);
        String unit = preferences.getString("unitOfVelocity", "min/km");
        switch (unit.trim()) {
            case "km/h":
                unitOfVelocity = UnitOfVelocity.KM_PER_HOUR;
                break;
            case "min/km":
                unitOfVelocity = UnitOfVelocity.MIN_PER_KM;
                break;
        }
        System.out.println("Unit set to: " + unitOfVelocity);
    }

    private void saveSharedPreferences() {
        SharedPreferences.Editor preferenceEditor;
        preferenceEditor = preferences.edit();
        if (unitOfVelocity == UnitOfVelocity.KM_PER_HOUR) {
            preferenceEditor.putInt("leftNumberPickerValueKmH", leftNPicker.getValue());
            preferenceEditor.putInt("rightNumberPickerValueKmH", rightNPicker.getValue());
        } else {
            preferenceEditor.putInt("leftNumberPickerValueMinKm", leftNPicker.getValue());
            preferenceEditor.putInt("rightNumberPickerValueMinKm", rightNPicker.getValue());
        }
        preferenceEditor.apply();
    }


    public void setFeedbackPreferences() {
        feedback.setVibrationAllowed(vibration);
        feedback.setAudioAllowed(audio);
        feedback.setFeedbackFrequency(feedbackFrequency);
        feedback.setUnitOfVelocity(unitOfVelocity);
    }

    @SuppressLint("SetTextI18n")
    public void setUnit() {
        unitTextView.setText(unitOfVelocity.toString());
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

        runnerView = RunnerView.newInstance(this, speed, autoSaveSession);

        Bundle bundle = new Bundle();
        bundle.putDouble("speed", speed);
        getIntent().putExtra("feedbackHandler", feedback);
        getIntent().putExtra("unitOfVelocity", unitOfVelocity);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, runnerView);
        transaction.addToBackStack("mainActivity");
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
                findViewById(R.id.minutes_tag).setVisibility(View.VISIBLE);
                findViewById(R.id.seconds_tag).setVisibility(View.VISIBLE);
                leftNPicker.setValue(preferences.getInt("leftNumberPickerValueMinKm", 1));
                rightNPicker.setValue(preferences.getInt("rightNumberPickerValueMinKm", 0));
                break;
            case KM_PER_HOUR:
                leftNPicker.setMinValue(4);
                leftNPicker.setMaxValue(40);
                rightNPicker.setMaxValue(0);
                rightNPicker.setMaxValue(9);
                findViewById(R.id.dot).setVisibility(View.VISIBLE);
                findViewById(R.id.minutes_tag).setVisibility(View.INVISIBLE);
                findViewById(R.id.seconds_tag).setVisibility(View.INVISIBLE);
                leftNPicker.setValue(preferences.getInt("leftNumberPickerValueKmH", 4));
                rightNPicker.setValue(preferences.getInt("rightNumberPickerValueKmH", 0));
                break;
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

}


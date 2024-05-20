package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Locale;

/**
 * The application Main Activity, this class serves as the entry point for the application.
 *
 * @author Jonathan, Samuel, Johnny, Emrik
 */
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

    /**
     * On create method, gets called every time the class is created and calls for
     * initializers to set up the elements needed.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * @author Samuel, Jonathan, Johnny, Emrik
     */
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        sessionManager = new SessionManager();
        sessionManager.readFile(this);
        setContentView(R.layout.activity_main);
        initializeGraphicalResources();
        fragmentManager = getSupportFragmentManager();
        loadSharedPreferences();
        setFeedbackPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);
        initializeEventListeners();
    }

    /**
     * Instantiates resources tied to the inflated XML.
     *
     * @author Samuel
     */
    public void initializeGraphicalResources() {
        startSessionButton = findViewById(R.id.start_button);
        settingsButton = findViewById(R.id.settings_button);
        leftNumberPicker = findViewById(R.id.left_n_picker);
        rightNumberPicker = findViewById(R.id.right_n_picker);
        feedbackHandler = new FeedbackHandler(getApplicationContext());
        previousSessionsButton = findViewById(R.id.previous_sessions_button);
        unitTextView = findViewById(R.id.unitTextView);
    }

    /**
     * Initializes event listeners for interactable elements such as buttons.
     *
     * @author Samuel
     */
    public void initializeEventListeners() {
        startSessionButton.setOnClickListener(v -> {
            setTargetVelocity(unitOfVelocity);
            if (targetVelocity != 0) {
                displayRunnerView(targetVelocity);
            }
        });
        settingsButton.setOnClickListener(v -> displaySettingsView());
        previousSessionsButton.setOnClickListener(v -> displaySessionsView());
    }

    /**
     * onDestroy method, called every time the activity is destroyed
     * Stops the service to keep it from running in the background while application is not running
     * @author Johnny
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SensorUnitHandler.class));
    }

    /**
     * onPause method, called every time the activity is paused.
     * Calls the save preference method for saving the selected pace
     * between changing fragments or closing the application
     * @author Johnny
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPreferences();
    }

    /**
     * Converts and sets the selected pace to m/s depending on what unit of velocity
     * is currently configured.
     *
     * @param unitOfVelocity Enum representing the current unit of velocity.
     *
     * @author Samuel
     */
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

    /**
     * Method for updating settings after they have been altered
     * @author Johnny, Samuel
     */
    public void updateSettings() {
        loadSharedPreferences();
        setUnit();
        setPickerStyle(unitOfVelocity);
    }

    /**
     * Method for updating settings in the runnerView
     * @author Johnny, Samuel, Emrik
     */
    public void updateSettingsRunnersView() {
        if (runnerView != null) {
            setFeedbackPreferences();
            runnerView.setUnitOfVelocity(unitOfVelocity);
            runnerView.getCurrentSession().setUnitOfVelocity(unitOfVelocity);
            runnerView.setUnitOfVelocityDisplay();
            runnerView.updateSelectedPaceUnit();
        }
    }

    /**
     * Method for setting values of variables to ones previously saved.
     * @author Johnny, Samuel
     */
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

    /**
     * Method for saving the currently set pace to shared preferences for future
     * use when creating the activity.
     * @author Johnny
     */
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

    /**
     * Method for setting the behaviour of the feedbackHandler.
     * @author Samuel
     */
    public void setFeedbackPreferences() {
        feedbackHandler.setVibrationAllowed(vibrationEnabled);
        feedbackHandler.setAudioAllowed(audioEnabled);
        feedbackHandler.setFeedbackFrequency(feedbackFrequency);
        feedbackHandler.setUnitOfVelocity(unitOfVelocity);
    }

    /**
     * Method for displaying the current unit of pace selected
     * @author Samuel
     */
    @SuppressLint("SetTextI18n")
    public void setUnit() {
        unitTextView.setText(unitOfVelocity.toString().toUpperCase(Locale.US));
    }

    /**
     * Method for displaying the sessionFragment
     * @author Jonathan
     */
    private void displaySessionsView() {
        SessionFragment sessionFragment = SessionFragment.newInstance(sessionManager);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, sessionFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Method for displaying the settingsFragment
     * @author Johnny
     */
    private void displaySettingsView() {
        fragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Method for creating a new instance of runnerView and displaying it
     * @param speed the desired pace for the session
     * @author Jonathan, Samuel
     */
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

    /**
     * Sets the correct scroll wheels depending on desired unit of pace
     * @param unitOfVelocity current set unit of pace
     * @author Jonathan, Samuel, Johnny
     */
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


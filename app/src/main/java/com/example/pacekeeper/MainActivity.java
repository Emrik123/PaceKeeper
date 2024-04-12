package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private int speed;
    private Button confirm;
    private NumberPicker leftNPicker;
    private NumberPicker rightNPicker;
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
        //FragmentManager fragmentManager = getSupportFragmentManager();
        loadSharedPreferences();
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        loadSharedPreferences();
        confirm = findViewById(R.id.confirmButton);
        settingsButton = findViewById(R.id.settingsButton);
        leftNPicker = findViewById(R.id.leftNPicker);
        rightNPicker = findViewById(R.id.rightNPicker);
        setPickerStyle("default");
        feedback = new FeedbackHandler(getApplicationContext());
        setFeedbackPreferences();
        sessions = findViewById(R.id.historyButton);
        unitTextView = findViewById(R.id.unitTextView);
        setFeedbackPreferences();
        setUnit();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speed = leftNPicker.getValue();
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
        setPickerStyle(speedDisplayMode);
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

    public void setPickerStyle(String speedDisplayMode) {
        switch (speedDisplayMode) {
            case "minPerKm":
                leftNPicker.setMinValue(1);
                leftNPicker.setMaxValue(59);
                rightNPicker.setMaxValue(0);
                rightNPicker.setMaxValue(59);
                findViewById(R.id.dot).setVisibility(View.INVISIBLE);
                findViewById(R.id.minutesTag).setVisibility(View.VISIBLE);
                findViewById(R.id.secondsTag).setVisibility(View.VISIBLE);
                break;
            case "km":
            default:
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

}

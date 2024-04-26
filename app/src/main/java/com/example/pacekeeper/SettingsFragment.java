package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment {

    private SwitchCompat vibrationSwitch;
    private SwitchCompat audioSwitch;
    private SwitchCompat autoSaveSessionsSwitch;
    private RadioGroup feedbackFrequencyRadioGroup;
    private RadioGroup displaySpeedRadioGroup;
    private Boolean audioFeedback;
    private Boolean vibrationFeedback;
    private Boolean autoSaveSessions;
    private RadioButton radioFrequencyLow;
    private RadioButton radioFrequencyMedium;
    private RadioButton radioFrequencyHigh;
    private RadioButton radioSpeedKmh;
    private RadioButton radioSpeedMinPerKm;
    private String feedbackFrequency;
    private UnitOfVelocity unitOfVelocity;
    private SharedPreferences preferences;
    private ImageButton returnButton;
    private Button soundSampleSpeedUpButton;
    private Button soundSampleSlowDownButton;
    private Button vibrationSampleSpeedUpButton;
    private Button vibrationSampleSlowDownButton;
    private AudioPlayer audioPlayer;
    private Vibrator vibrator;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = this.getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_settings, container, false);
        vibrationSwitch = rootView.findViewById(R.id.vibration_switch);
        audioSwitch = rootView.findViewById(R.id.audio_switch);
        feedbackFrequencyRadioGroup = rootView.findViewById(R.id.feedback_frequency_radiogroup);
        displaySpeedRadioGroup = rootView.findViewById(R.id.speed_display_radiogroup);
        radioFrequencyLow = rootView.findViewById(R.id.radio_frequency_low);
        radioFrequencyMedium = rootView.findViewById(R.id.radio_frequency_medium);
        radioFrequencyHigh = rootView.findViewById(R.id.radio_frequency_high);
        radioSpeedKmh = rootView.findViewById(R.id.radio_km_h);
        radioSpeedMinPerKm = rootView.findViewById(R.id.radio_min_km);
        returnButton = rootView.findViewById(R.id.return_button);
        autoSaveSessionsSwitch = rootView.findViewById(R.id.autosave_switch);
        soundSampleSpeedUpButton = rootView.findViewById(R.id.btn_speed_up_sound);
        soundSampleSlowDownButton = rootView.findViewById(R.id.btn_slow_down_sound);
        vibrationSampleSpeedUpButton = rootView.findViewById(R.id.btn_speed_up_vibration);
        vibrationSampleSlowDownButton = rootView.findViewById(R.id.btn_slow_down_vibration);
        audioPlayer = new AudioPlayer(requireContext());
        vibrator = new Vibrator(requireContext());

        loadAndSetCurrentSettings();
        setGraphicElements();

        vibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrationFeedback = isChecked;
            }
        });

        audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioFeedback = isChecked;
            }
        });

        autoSaveSessionsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoSaveSessions = isChecked;
            }
        });

        feedbackFrequencyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == radioFrequencyLow.getId()){
                    feedbackFrequency = "low";
                }
                else if(checkedId == radioFrequencyMedium.getId()){
                    feedbackFrequency = "medium";
                }
                else {
                    feedbackFrequency = "high";
                }
            }
        });

        displaySpeedRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == radioSpeedKmh.getId()){
                    unitOfVelocity = UnitOfVelocity.KM_PER_HOUR;
                }
                else {
                    unitOfVelocity = UnitOfVelocity.MIN_PER_KM;
                }
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStackImmediate();
            }
        });

        soundSampleSpeedUpButton.setOnClickListener(v -> audioPlayer.increaseSound());
        soundSampleSlowDownButton.setOnClickListener(v -> audioPlayer.decreaseSound());
        vibrationSampleSpeedUpButton.setOnClickListener(v -> vibrator.increaseVelocity());
        vibrationSampleSlowDownButton.setOnClickListener(v -> vibrator.decreaseVelocity());

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSettings();
    }

    private void saveSettings(){
        SharedPreferences.Editor preferenceEditor;
        preferenceEditor = preferences.edit();
        preferenceEditor.putBoolean("audioFeedback", audioFeedback);
        preferenceEditor.putBoolean("vibrationFeedback", vibrationFeedback);
        preferenceEditor.putString("feedbackFrequency", feedbackFrequency);
        preferenceEditor.putString("unitOfVelocity", unitOfVelocity.toString());
        preferenceEditor.putBoolean("autoSaveSessions", autoSaveSessions);
        preferenceEditor.apply();
        ((MainActivity) getActivity()).updateSettings();
        ((MainActivity) getActivity()).updateSettingsRunnersView();
    }

    private void loadAndSetCurrentSettings(){
        vibrationFeedback = preferences.getBoolean("vibrationFeedback", true);
        audioFeedback = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
        autoSaveSessions = preferences.getBoolean("autoSaveSessions", false);
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

    private void setGraphicElements(){
        audioSwitch.setChecked(audioFeedback);
        vibrationSwitch.setChecked(vibrationFeedback);
        autoSaveSessionsSwitch.setChecked(autoSaveSessions);
        setFeedBackFrequencyButton();
        setDisplaySpeedButton();
    }

    private void setFeedBackFrequencyButton(){
        if(feedbackFrequency.equals("low")){
            radioFrequencyLow.setChecked(true);
        }
        else if(feedbackFrequency.equals("medium")){
            radioFrequencyMedium.setChecked(true);
        }
        else{
            radioFrequencyHigh.setChecked(true);
        }
    }

    private void setDisplaySpeedButton(){
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                radioSpeedKmh.setChecked(true);
                break;
            case MIN_PER_KM:
                radioSpeedMinPerKm.setChecked(true);
                break;
        }
    }
}
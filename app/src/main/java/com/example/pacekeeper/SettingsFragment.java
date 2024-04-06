package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
    private RadioGroup feedbackFrequencyRadioGroup;
    private Boolean audioFeedback;
    private Boolean vibrationFeedback;
    private RadioButton radioFrequencyLow;
    private RadioButton radioFrequencyMedium;
    private RadioButton radioFrequencyHigh;
    private String feedbackFrequency;
    private Button saveSettingsButton;
    private SharedPreferences preferences;
    private ImageButton returnButton;


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
        radioFrequencyLow = rootView.findViewById(R.id.radio_frequency_low);
        radioFrequencyMedium = rootView.findViewById(R.id.radio_frequency_medium);
        radioFrequencyHigh = rootView.findViewById(R.id.radio_frequency_high);
        saveSettingsButton = rootView.findViewById(R.id.saveSettings);
        returnButton = rootView.findViewById(R.id.return_button);


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

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStackImmediate();
            }
        });

        return rootView;
    }

    private void saveSettings(){
        SharedPreferences.Editor preferenceEditor;
        preferenceEditor = preferences.edit();
        preferenceEditor.putBoolean("audioFeedback", audioFeedback);
        preferenceEditor.putBoolean("vibrationFeedback", vibrationFeedback);
        preferenceEditor.putString("feedbackFrequency", feedbackFrequency);
        preferenceEditor.apply();
    }

    private void loadAndSetCurrentSettings(){
        vibrationFeedback = preferences.getBoolean("vibrationFeedback", true);
        audioFeedback = preferences.getBoolean("audioFeedback", true);
        feedbackFrequency = preferences.getString("feedbackFrequency", "medium");
    }

    private void setGraphicElements(){
        audioSwitch.setChecked(audioFeedback);
        vibrationSwitch.setChecked(vibrationFeedback);
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
}
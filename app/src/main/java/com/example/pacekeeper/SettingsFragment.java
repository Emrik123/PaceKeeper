package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/**
 Class for managing GUI elements in the Settings fragment
 */
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
    private ImageButton soundSampleSpeedUpButton;
    private ImageButton soundSampleSlowDownButton;
    private ImageButton vibrationSampleSpeedUpButton;
    private ImageButton vibrationSampleSlowDownButton;
    private Vibrator vibrator;
    private ImageButton desiredUnitHelpButton;
    private ImageButton feedbackFrequencyHelpButton;
    private TextView desiredUnitHelpTextView;
    private TextView feedbackFrequencyHelpTextView;
    private TextToSpeech tts;

    /**
     * Create method, called when the fragment is created and loads settings
     * saved in shared preferences
     * @param savedInstanceState
     * @author Johnny, Emrik
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getActivity()
                .getSharedPreferences("preferences", Context.MODE_PRIVATE);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStackImmediate();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Method called when creating the fragment, the necessary initialization is done here
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * @author Johnny, Samuel
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        vibrator = new Vibrator(requireContext());
        initGraphicElements(rootView);
        initListeners();
        loadAndSetCurrentSettings();
        setGraphicElements();
        initTextToSpeech();
        return rootView;
    }

    /**
     * onPause method, called when the fragment is put out of focus
     * @author Johnny, Samuel
     */
    @Override
    public void onPause() {
        super.onPause();
        saveSettings();
        removeTextToSpeech();
    }

    /**
     * Method for initializing graphical elements
     * @param rootView
     * @author Johnny, Samuel
     */
    public void initGraphicElements(View rootView) {
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
        feedbackFrequencyHelpButton = rootView.findViewById(R.id.feedback_frequency_help);
        feedbackFrequencyHelpTextView = rootView.findViewById(R.id.feedback_frequency_textview);
        desiredUnitHelpButton = rootView.findViewById(R.id.desired_pace_unit_help);
        desiredUnitHelpTextView = rootView.findViewById(R.id.desired_pace_unit_textview);
    }

    /**
     * Method for setting the behaviour of the buttons
     * @author Johnny, Samuel
     */
    public void initListeners() {
        feedbackFrequencyRadioGroup.setOnCheckedChangeListener
                ((group, checkedId) -> {
                    if (checkedId == radioFrequencyLow.getId()) {
                        feedbackFrequency = "low";
                    } else if (checkedId == radioFrequencyMedium.getId()) {
                        feedbackFrequency = "medium";
                    } else {
                        feedbackFrequency = "high";
                    }
                });

        displaySpeedRadioGroup.setOnCheckedChangeListener
                ((group, checkedId) -> {
                    if (checkedId == radioSpeedKmh.getId()) {
                        unitOfVelocity = UnitOfVelocity.KM_PER_HOUR;
                    } else {
                        unitOfVelocity = UnitOfVelocity.MIN_PER_KM;
                    }
                });

        desiredUnitHelpButton.setOnClickListener(v -> {
            if (desiredUnitHelpTextView.getVisibility() == View.GONE) {
                desiredUnitHelpTextView.setVisibility(View.VISIBLE);
                feedbackFrequencyHelpTextView.setVisibility(View.GONE);
            } else {
                desiredUnitHelpTextView.setVisibility(View.GONE);
            }
        });

        feedbackFrequencyHelpButton.setOnClickListener(v -> {
            if (feedbackFrequencyHelpTextView.getVisibility() == View.GONE) {
                feedbackFrequencyHelpTextView.setVisibility(View.VISIBLE);
                desiredUnitHelpTextView.setVisibility(View.GONE);
            } else {
                feedbackFrequencyHelpTextView.setVisibility(View.GONE);
            }
        });

        vibrationSwitch.setOnCheckedChangeListener
                ((view, isChecked) -> vibrationFeedback = isChecked);

        audioSwitch.setOnCheckedChangeListener(
                (view, isChecked) -> audioFeedback = isChecked);

        autoSaveSessionsSwitch.setOnCheckedChangeListener
                ((view, isChecked) -> autoSaveSessions = isChecked);

        returnButton.setOnClickListener
                (v -> getParentFragmentManager().popBackStackImmediate());

        soundSampleSpeedUpButton.setOnClickListener
                (v -> speechSampleFaster());

        soundSampleSlowDownButton.setOnClickListener
                (v -> speechSampleSlower());

        vibrationSampleSpeedUpButton.setOnClickListener
                (v -> vibrator.increaseVelocity());

        vibrationSampleSlowDownButton.setOnClickListener
                (v -> vibrator.decreaseVelocity());

    }

    /**
     * Method for saving the settings to a shared preference, when the data has been saved
     * the main activity is called to update its Gui.
     * @author Johnny
     */
    private void saveSettings() {
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

    /**
     * Method for loading settings currently set in the shared preference
     * @author Johnny, Samuel
     */
    private void loadAndSetCurrentSettings() {
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

    /**
     * Method for setting the GUI to match current settings in the shared preferences.
     * @author Johnny
     */
    private void setGraphicElements() {
        audioSwitch.setChecked(audioFeedback);
        vibrationSwitch.setChecked(vibrationFeedback);
        autoSaveSessionsSwitch.setChecked(autoSaveSessions);
        setFeedBackFrequencyButton();
        setDisplaySpeedButton();
    }

    /**
     * Method for checking the radiobutton corresponding to the
     * current feedback frequency setting
     * @author Johnny
     */
    private void setFeedBackFrequencyButton() {
        if (feedbackFrequency.equals("low")) {
            radioFrequencyLow.setChecked(true);
        } else if (feedbackFrequency.equals("medium")) {
            radioFrequencyMedium.setChecked(true);
        } else {
            radioFrequencyHigh.setChecked(true);
        }
    }

    /**
     * Method for checking the radiobutton corresponding to the
     * current pace unit
     * @author Samuel
     */
    private void setDisplaySpeedButton() {
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                radioSpeedKmh.setChecked(true);
                break;
            case MIN_PER_KM:
                radioSpeedMinPerKm.setChecked(true);
                break;
        }
    }

    /**
     * Initializes a speech synthesizer with a set language.
     * @author Samuel
     */
    public void initTextToSpeech() {
        tts = new TextToSpeech(requireContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    /**
     * Interrupts and removes the text to speech.
     * @author Samuel
     */
    public void removeTextToSpeech() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    /**
     * Tells the text to speech to speak a sample text of what a user might hear
     * if they're moving too slow.
     * @author Samuel
     */
    public void speechSampleFaster() {
        CharSequence prompt = "";
        switch (unitOfVelocity) {
            case MIN_PER_KM:
                prompt = "Four minutes, 26 seconds, speed up";
                break;
            case KM_PER_HOUR:
                prompt = "13 km/h, speed up";
        }
        tts.speak(prompt, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * Tells the text to speech to speak a sample text of what a user might hear
     * if they're moving too fast.
     * @author Samuel
     */
    public void speechSampleSlower() {
        CharSequence prompt = "";
        switch (unitOfVelocity) {
            case MIN_PER_KM:
                prompt = "Three minutes, 15 seconds, slow down";
                break;
            case KM_PER_HOUR:
                prompt = "19 km/h, slow down";
        }
        tts.speak(prompt, TextToSpeech.QUEUE_FLUSH, null, null);
    }
}
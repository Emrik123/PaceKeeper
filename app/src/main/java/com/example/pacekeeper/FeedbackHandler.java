package com.example.pacekeeper;

import java.io.Serializable;
import java.util.*;

import android.content.Context;
import android.speech.tts.TextToSpeech;

public class FeedbackHandler implements Serializable {
    private Vibrator vibrator;
    private AudioPlayer audioPlayer;
    private boolean audioAllowed = true;
    private boolean vibrationAllowed = true;
    private long feedbackDelayMillis;
    private final double VELOCITY_DELTA_MPS = 1 / 3.6;
    private boolean isRunning = false;
    private double selectedSpeed;
    private double currentSpeed;
    private long initialDelayMillis = 2000;
    private Timer timer;
    private TimerTask timerTask;
    private Context context;
    private TextToSpeech tts;
    private final double LOWER_LIMIT_MPS = 3 / 3.6;
    private boolean deviated = false;
    private UnitOfVelocity unitOfVelocity;

    public FeedbackHandler(Context context) {
        this.context = context;
        audioPlayer = new AudioPlayer(context);
        vibrator = new Vibrator(context);

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ROOT);
                }
            }
        });

    }

    public void giveFeedback() {
        String correctPrompt = "good pace";
        String fasterPrompt = "speed up";
        String slowerPrompt = "slow down";
        CharSequence prompt = formattedVelocity();

        if (isRunning && currentSpeed > LOWER_LIMIT_MPS) {
            if (audioAllowed) {
                if (movingAtCorrectSpeed() && deviated) {
                    prompt += correctPrompt;
                    deviated = false;
                    speak(prompt);
                }
                if (movingTooFast()) {
                    //audioPlayer.decreaseSound();
                    prompt += slowerPrompt;
                    deviated = true;
                    speak(prompt);
                }
                if (movingTooSlow()) {
                    prompt += fasterPrompt;
                    //audioPlayer.increaseSound();
                    deviated = true;
                    speak(prompt);
                }
            }
            if (vibrationAllowed) {
                if (movingTooFast()) {
                    vibrator.vibrateSlower();
                }
                if (movingTooSlow()) {
                    vibrator.vibrateFaster();
                }
            }
        }
    }

    private void speak(CharSequence seq) {
        tts.speak(seq, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private CharSequence formattedVelocity() {
        CharSequence seq = "";
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                seq = String.format(Locale.US, "%.1f... ", currentSpeed * 3.6);
                break;
            case MIN_PER_KM:
                double minutesPerKm = currentSpeed * 60;
                minutesPerKm /= 1000;
                minutesPerKm = 1 / minutesPerKm;
                int minutes = (int) Math.floor(minutesPerKm);
                double decimal = minutesPerKm - minutes;
                int seconds = (int) Math.floor(decimal * 60);
                seq = String.format(Locale.US, "%d min, %d sec... ", minutes, seconds);
                break;
        }
        return seq;
    }

    public void removeTextToSpeech() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public void runFeedback(double selectedSpeed) {
        setSelectedSpeed(selectedSpeed);
        timer = new Timer();
        timerTask = new Task(this);
        timer.schedule(timerTask, initialDelayMillis, feedbackDelayMillis);
    }

    public void stopFeedback() {
        timerTask.cancel();
        timer.cancel();
        timer.purge();
    }

    public void setFeedbackFrequency(String keyword) {
        switch (keyword.toLowerCase(Locale.ROOT)) {
            case "medium":
                setFeedbackDelayMillis(4000);
                break;
            case "low":
                setFeedbackDelayMillis(7000);
                break;
            case "high":
                setFeedbackDelayMillis(1000);
                break;
        }
    }

    private boolean movingTooFast() {
        return currentSpeed >= selectedSpeed + VELOCITY_DELTA_MPS;
    }

    private boolean movingTooSlow() {
        return currentSpeed <= selectedSpeed - VELOCITY_DELTA_MPS;
    }

    private boolean movingAtCorrectSpeed() {
        return currentSpeed <= selectedSpeed + VELOCITY_DELTA_MPS && currentSpeed >= selectedSpeed - VELOCITY_DELTA_MPS;
    }

    public void setUnitOfVelocity(UnitOfVelocity unitOfVelocity) {
        this.unitOfVelocity = unitOfVelocity;
    }

    private void setFeedbackDelayMillis(long millis) {
        feedbackDelayMillis = millis;
    }

    public void setAudioAllowed(boolean bool) {
        audioAllowed = bool;
    }

    public void setVibrationAllowed(boolean bool) {
        vibrationAllowed = bool;
    }

    public long getFeedbackDelayMillis() {
        return feedbackDelayMillis;
    }

    public double getVelocityDelta() {
        return VELOCITY_DELTA_MPS;
    }

    public void setRunning(boolean bool) {
        this.isRunning = bool;
    }

    public void setSelectedSpeed(double selectedSpeed) {
        this.selectedSpeed = selectedSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }
}

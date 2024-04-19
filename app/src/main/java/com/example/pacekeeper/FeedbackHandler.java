package com.example.pacekeeper;

import java.io.Serializable;
import java.util.*;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

public class FeedbackHandler implements Serializable {
    private Vibrator vibrator;
    private AudioPlayer audioPlayer;
    private boolean audioAllowed = true;
    private boolean vibrationAllowed = true;
    private long feedbackDelayMillis;
    private double feedbackDeltaMPS = 1 / 3.6;
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
    private String velocityUnit;

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
        CharSequence speed = formattedVelocity();

        if (isRunning && currentSpeed > LOWER_LIMIT_MPS) {
            if (audioAllowed) {
                if (movingAtCorrectSpeed() && deviated) {
                    speed += correctPrompt;
                    deviated = false;
                    speak(speed);
                }
                if (movingTooFast()) {
                    //audioPlayer.decreaseSound();
                    speed += slowerPrompt;
                    deviated = true;
                    speak(speed);
                }
                if (movingTooSlow()) {
                    speed += fasterPrompt;
                    //audioPlayer.increaseSound();
                    deviated = true;
                    speak(speed);
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

    private String formattedVelocity() {
        String str = "";
        switch (velocityUnit) {
            case "kmh":
                str = String.format(Locale.US, "%.1f... ", currentSpeed * 3.6);
                break;
            case "minPerKm":
                double minutesPerKm = currentSpeed * 60;
                minutesPerKm /= 1000;
                minutesPerKm = 1 / minutesPerKm;
                int minutes = (int) Math.floor(minutesPerKm);
                double decimal = minutesPerKm - minutes;
                int seconds = (int) Math.floor(decimal * 60);
                str = String.format(Locale.US, "%d min, %d sec... ", minutes, seconds);
                break;
        }
        return str;
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
        return currentSpeed >= selectedSpeed + feedbackDeltaMPS;
    }

    private boolean movingTooSlow() {
        return currentSpeed <= selectedSpeed - feedbackDeltaMPS;
    }

    private boolean movingAtCorrectSpeed() {
        return currentSpeed <= selectedSpeed + feedbackDeltaMPS && currentSpeed >= selectedSpeed - feedbackDeltaMPS;
    }

    public void setVelocityUnit(String unit) {
        velocityUnit = unit;
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

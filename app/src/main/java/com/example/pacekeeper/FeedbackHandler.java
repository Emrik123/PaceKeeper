package com.example.pacekeeper;

import java.io.Serializable;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

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

    public FeedbackHandler(Context context) {
        audioPlayer = new AudioPlayer(context);
        vibrator = new Vibrator(context);
    }


    public void giveFeedback() {
        System.out.println("Feedback called");
        if (isRunning) {
            if (audioAllowed) {
                if (movingTooFast()) {
                    audioPlayer.decreaseSound();
                }
                if (movingTooSlow()) {
                    audioPlayer.increaseSound();
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
                setFeedbackDelayMillis(500);
                break;
        }
    }

    private boolean movingTooFast() {
        return currentSpeed >= selectedSpeed + feedbackDeltaMPS;
    }

    private boolean movingTooSlow() {
        return currentSpeed <= selectedSpeed - feedbackDeltaMPS;
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
        System.out.println("speed was set");
    }
}

package com.example.pacekeeper;

import java.io.Serializable;
import java.util.Locale;

import android.content.Context;

public class FeedbackHandler implements Serializable {
    private Vibrator vibrator;
    private AudioPlayer audioPlayer;
    private boolean audioAllowed = true;
    private boolean vibrationAllowed = true;
    private long feedbackDelayMillis;
    private double feedbackDeltaMPS = 1 / 3.6;

    public FeedbackHandler(Context context) {
        audioPlayer = new AudioPlayer(context);
        vibrator = new Vibrator(context);
        setFeedbackFrequency("medium");
    }


    public void giveFeedback(double selectedSpeed, double currentSpeed) {
        if (audioAllowed) {
            if (movingTooFast(selectedSpeed, currentSpeed)) {
                audioPlayer.decreaseSound();
            }
            if (movingTooSlow(selectedSpeed, currentSpeed)) {
                audioPlayer.increaseSound();
            }
        }
        if (vibrationAllowed) {
            if (movingTooFast(selectedSpeed, currentSpeed)) {
                vibrator.vibrateSlower();
            }
            if (movingTooSlow(selectedSpeed, currentSpeed)) {
                vibrator.vibrateFaster();
            }
        }

    }

    public void setFeedbackFrequency(String keyword) {
        switch (keyword.toLowerCase(Locale.ROOT)) {
            case "medium":
                setFeedbackDelayMillis(2000);
                break;
            case "low":
                setFeedbackDelayMillis(4000);
                break;
            case "high":
                setFeedbackDelayMillis(1000);
                break;
        }
    }

    private boolean movingTooFast(double selectedSpeed, double currentSpeed) {
        return currentSpeed >= selectedSpeed + feedbackDeltaMPS;
    }

    private boolean movingTooSlow(double selectedSpeed, double currentSpeed) {
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

}

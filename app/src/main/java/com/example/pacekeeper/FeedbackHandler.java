package com.example.pacekeeper;

import java.io.Serializable;
import java.util.*;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

/**
 * This class handles methods of haptic and auditory feedback, specifically a text to speech
 * implementation and the system vibrator.
 *
 * @author Samuel
 */
public class FeedbackHandler implements Serializable {
    private final Vibrator vibrator;
    private boolean audioAllowed = true;
    private boolean vibrationAllowed = true;
    private long feedbackDelayMillis;
    private final double VELOCITY_DELTA_MS = 1 / 3.6;
    private boolean isRunning = false;
    private double selectedSpeed;
    private double currentSpeed;
    private final long INITIAL_DELAY_MILLIS = 2000;
    private Timer timer;
    private TimerTask timerTask;
    private Context context;
    private TextToSpeech tts;
    private final double LOWER_LIMIT_MS = 0.5;
    private boolean deviated = false;
    private UnitOfVelocity unitOfVelocity;

    /**
     * Class constructor.
     * Instantiates a vibrator that is used by this feedback handler.
     *
     * @param context the context of the global application object.
     * @author Samuel
     */
    public FeedbackHandler(Context context) {
        this.context = context;
        vibrator = new Vibrator(context);
    }

    /**
     * Gives either auditory feedback or haptic feedback, or both, given
     * that a user moves faster than a lower limit of 0.5 m/s.
     *
     * @author Samuel
     */
    public void giveFeedback() {
        String correctPrompt = "good pace.";
        String fasterPrompt = "speed up.";
        String slowerPrompt = "slow down.";
        CharSequence prompt = formattedVelocity();

        if (isRunning && currentSpeed > LOWER_LIMIT_MS) {
            if (audioAllowed) {
                if (movingAtCorrectSpeed() && deviated) {
                    prompt += correctPrompt;
                    deviated = false;
                    speak(prompt);
                } else if (movingTooFast()) {
                    prompt += slowerPrompt;
                    deviated = true;
                    speak(prompt);
                } else if (movingTooSlow()) {
                    prompt += fasterPrompt;
                    deviated = true;
                    speak(prompt);
                }
            }
            if (vibrationAllowed) {
                if (movingTooFast()) {
                    vibrator.decreaseVelocity();
                } else if (movingTooSlow()) {
                    vibrator.increaseVelocity();
                }
            }
        }
    }

    /**
     * Triggers the TextToSpeech to speak a given text,
     * see {@link android.speech.tts.TextToSpeech#speak(CharSequence, int, Bundle, String)}.
     * @param seq the sequence of Chars, or String, that is to be spoken.
     *
     * @author Samuel
     */
    private void speak(CharSequence seq) {
        tts.speak(seq, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * Returns a correctly formatted text to be spoken by the TextToSpeech,
     * depending on what unit of velocity is configured.
     *
     * @return a sequence of Chars.
     *
     * @author Samuel
     */
    private CharSequence formattedVelocity() {
        CharSequence seq = "";
        switch (unitOfVelocity) {
            case KM_PER_HOUR:
                seq = String.format(Locale.US, "%.1f km/h... ", currentSpeed * 3.6);
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

    /**
     * Stops and removes the current TextToSpeech. Should be called in
     * the onDestroy() method of an Activity to prevent memory leaks.
     *
     * @author Samuel
     */
    public void removeTextToSpeech() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    /**
     * Initiates a timer which will trigger feedback at set intervals,
     * beginning after an initial delay.
     * Instantiates the TextToSpeech used by this feedback handler.
     *
     * @param selectedSpeed the velocity chosen by the user.
     *
     * @author Samuel
     */
    public void startFeedback(double selectedSpeed) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        setSelectedSpeed(selectedSpeed);
        timer = new Timer();
        timerTask = new Task(this);
        timer.schedule(timerTask, INITIAL_DELAY_MILLIS, feedbackDelayMillis);
    }

    /**
     * Stops all feedback and removes the TextToSpeech.
     *
     * @author Samuel
     */
    public void stopFeedback() {
        removeTextToSpeech();
        timerTask.cancel();
        timer.cancel();
        timer.purge();
    }

    /**
     * Specifies the frequency, in milliseconds, at which feedback is provided.
     *
     * @param keyword a String specifying the level of frequency.
     *
     * @author Samuel
     */
    public void setFeedbackFrequency(String keyword) {
        switch (keyword.toLowerCase(Locale.ROOT)) {
            case "low":
                setFeedbackDelayMillis(10000);
                break;
            case "medium":
                setFeedbackDelayMillis(6000);
                break;
            case "high":
                setFeedbackDelayMillis(3000);
                break;
        }
    }

    /**
     * Returns true if the user is moving at a velocity higher than
     * the selected velocity + approximately 0.5 m/s.
     *
     * @return true if the user is moving too fast, false otherwise.
     *
     * @author Samuel
     */
    private boolean movingTooFast() {
        return currentSpeed >= selectedSpeed + VELOCITY_DELTA_MS;
    }

    /**
     * Returns true if the user is moving at a velocity lower than
     * the selected velocity - approximately 0.5 m/s.
     *
     * @return true if the user is moving too slow, false otherwise.
     *
     * @author Samuel
     */
    private boolean movingTooSlow() {
        return currentSpeed <= selectedSpeed - VELOCITY_DELTA_MS;
    }

    /**
     * Returns true if the user is moving at a velocity within the accepted interval.
     *
     * @return true if the user is moving at the correct speed, false otherwise.
     *
     * @author Samuel
     */
    private boolean movingAtCorrectSpeed() {
        return currentSpeed <= selectedSpeed + VELOCITY_DELTA_MS && currentSpeed >= selectedSpeed - VELOCITY_DELTA_MS;
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

    public double getVelocityDelta() {
        return VELOCITY_DELTA_MS;
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

package com.example.pacekeeper;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.VibratorManager;

import java.io.Serializable;

/*
* This class could hold a collection of vibrationpatterns, ways to add and remove patterns etc.
 */
public class Vibrator implements Serializable {
    private final android.os.Vibrator vibrator;
    private VibrationEffect increaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{150, 75, 150, 75, 150}, new int[]{255, 0, 255, 0, 255}, -1); //Creates Vibration pattern for being too slow
    private VibrationEffect decreaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{900}, new int[]{255}, -1); //Creates Vibration pattern for being too fast

    public Vibrator(Context context) {
        vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    // ;)
    public void vibrateFaster() {
        vibrator.vibrate(increaseSpeedVibrationPattern);
    }

    public void vibrateSlower() {
        vibrator.vibrate(decreaseSpeedVibrationPattern);
    }
}

package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.CombinedVibration;
import android.os.VibrationEffect;
import android.os.VibratorManager;

import java.io.Serializable;

/*
* This class could hold a collection of vibrationpatterns, ways to add and remove patterns etc.
 */
public class Vibrator implements Serializable {
    private final boolean runningS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    private android.os.Vibrator vibrator;
    private VibratorManager vibratorManager;
    private final VibrationEffect increaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{150, 50, 150, 50, 150, 50, 150}, new int[]{255, 0, 255, 0, 255, 0, 255}, -1); //Creates Vibration pattern for being too slow
    private final VibrationEffect decreaseSpeedVibrationPattern = VibrationEffect.createWaveform(new long[]{0, 700}, new int[]{0, 255},-1); //Creates Vibration pattern for being too fast

    @SuppressLint("NewApi")
    public Vibrator(Context context) {
        if (runningS) {
            vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        } else {
            vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    @SuppressLint("NewApi")
    public void increaseVelocity() {
        if (runningS) {
            vibratorManager.vibrate(CombinedVibration.createParallel(increaseSpeedVibrationPattern));
        } else {
            vibrator.vibrate(increaseSpeedVibrationPattern);
        }
    }

    @SuppressLint("NewApi")
    public void decreaseVelocity() {
        if (runningS) {
            vibratorManager.vibrate(CombinedVibration.createParallel(decreaseSpeedVibrationPattern));
        } else {
            vibrator.vibrate(decreaseSpeedVibrationPattern);
        }
    }
}

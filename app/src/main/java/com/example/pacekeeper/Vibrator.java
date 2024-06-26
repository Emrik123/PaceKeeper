package com.example.pacekeeper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.CombinedVibration;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import java.io.Serializable;

/**
 * This class provides haptic feedback through the use of the system vibrator.
 *
 * @author Samuel, Johnny
 */
public class Vibrator implements Serializable {
    private final boolean runningS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    private android.os.Vibrator vibrator;
    private VibratorManager vibratorManager;
    private final VibrationEffect increaseSpeedVibrationPattern = VibrationEffect.createWaveform(
            new long[]{150, 50, 150, 50, 150, 50, 150},
            new int[]{255, 0, 255, 0, 255, 0, 255}, -1);
    private final VibrationEffect decreaseSpeedVibrationPattern = VibrationEffect.createWaveform(
            new long[]{0, 700},
            new int[]{0, 255},-1);

    /**
     * Class Constructor.
     * If the device is running Android 12 (SDK 31) or later {@link android.os.VibratorManager} is used
     * to get the system vibrator. Else, {@link android.os.Vibrator} is used instead.
     *
     * @param context the context of the global application object.
     *
     * @author Samuel, Johnny
     */
    @SuppressLint("NewApi")
    public Vibrator(Context context) {
        if (runningS) {
            vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        } else {
            vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    /**
     * Vibrates with the vibration effect indicating that a user is moving too slow.
     *
     * @author Samuel, Johnny
     */
    @SuppressLint("NewApi")
    public void increaseVelocity() {
        if (runningS) {
            vibratorManager.vibrate(CombinedVibration.createParallel(increaseSpeedVibrationPattern));
        } else {
            vibrator.vibrate(increaseSpeedVibrationPattern);
        }
    }

    /**
     * Vibrates with the vibration effect indicating that a user is moving too fast.
     *
     * @author Samuel, Johnny
     */
    @SuppressLint("NewApi")
    public void decreaseVelocity() {
        if (runningS) {
            vibratorManager.vibrate(CombinedVibration.createParallel(decreaseSpeedVibrationPattern));
        } else {
            vibrator.vibrate(decreaseSpeedVibrationPattern);
        }
    }
}

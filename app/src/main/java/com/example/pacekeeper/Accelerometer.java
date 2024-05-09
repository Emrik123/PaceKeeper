package com.example.pacekeeper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * This class holds an Accelerometer sensor of type LINEAR_ACCELERATION.
 * It implements the SensorEventListener interface to register a listener to the sensor.
 * Each time the sensor values change, the onSensorChanged method is called.
 * The accelerometer is set to a sensor update frequency of SENSOR_DELAY_GAME, i.e. a rate of 50 Hz.
 * @author Emrik, Johnny
 */
public class Accelerometer implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] accelerometerValues;
    private static final float ALPHA = 0.8f;
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    /**
     * Class constructor.
     * Initializes a HandlerThread and starts is with a default looper.
     * @param sensorManager takes in an instance of the device's
     *                      sensor manager to gain access to the accelerometer.
     * @author Emrik
     */
    public Accelerometer(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        sensorThread = new HandlerThread("Accelerometer");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    /**
     * Used to start the accelerometer and register a listener to the representing object.
     * @author Emrik, Johnny
     */
    public void startAccelerometer() {
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME, sensorHandler);
        }
    }

    /**
     * Used to stop the accelerometer and kill the HandlerThread.
     * @author Emrik
     */
    public void stopAccelerometer() {
        if (accelerometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
            accelerometer = null;
        }
        if (sensorThread != null) {
            sensorThread.quitSafely();
            try {
                sensorThread.join();
                sensorThread = null;
                sensorHandler = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Each time the sensor values are updated, (values are checked at a rate of 50 Hz)
     * this method is called. Passing along the event holding the sensor values as parameter.
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     * @author Emrik, Johnny
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (accelerometerValues == null) {
            accelerometerValues = event.values.clone();
        } else {
            for (int i = 0; i < 3; i++) {
                accelerometerValues[i] = ALPHA * accelerometerValues[i]
                        + (1 - ALPHA) * event.values[i];
            }
        }
    }

    /**
     * Not used.
     * @param sensor of which the accuracy of has changed.
     * @param accuracy The new accuracy of this sensor, one of
     *         {@code SensorManager.SENSOR_STATUS_*}
     * @author Emrik
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Setter.
     * @param values to set.
     * @author Emrik
     */
    public void setAccelerometerValues(float[] values) {
        accelerometerValues = values;
    }

    /**
     * Getter.
     * @return the current accelerometer values.
     * @author Emrik
     */
    public float[] getAccelerometerValues() {
        return accelerometerValues;
    }
}

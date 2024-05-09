package com.example.pacekeeper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * This class is used to determine the current orientation of the device, which is necessary
 * to determine correct values for the different axes of acceleration.
 * It's derived off a basic model of transformation, only minor alterations were made to the calculations in
 * order to better fit the current application.
 * @author Emrik
 */
public class OrientationHandler implements SensorEventListener {

    private SensorUnitHandler sensorUnitHandler;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private RealMatrix matrix;
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    /**
     * Class constructor.
     * Initializes the inverse matrix and localizes the parameters.
     * @param sensorManager the device's sensor manager, used to access the gyroscope.
     * @param sensorUnitHandler the object handling all the sensors.
     * @author Emrik
     */
    public OrientationHandler(SensorManager sensorManager, SensorUnitHandler sensorUnitHandler) {
        this.sensorUnitHandler = sensorUnitHandler;
        this.sensorManager = sensorManager;
        matrix = new Array2DRowRealMatrix(3, 3);
    }

    /**
     * Used to initialize the sensor, start the HandlerThread
     * and register a listener using a default Looper in order to
     * start listening for orientation changes.
     * @author Emrik
     */
    public void startOrientationSensor() {
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorThread = new HandlerThread("Orientation Sensor");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME, sensorHandler);
    }

    /**
     * Used to stop listening to orientation changes and kill the thread.
     */
    public void stopOrientationSensor() {
        sensorManager.unregisterListener(this);
        sensorThread.quitSafely();
    }

    /**
     * Each time the sensor values are updated, (values are checked at a rate of 50 Hz)
     * this method is called. Passing along the event holding the sensor values as parameter.
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     * @author Emrik
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            updateMatrix(rotationMatrix);
            float[] temp = sensorUnitHandler.getAccelerometer().getAccelerometerValues();
            if(temp != null){
                transformAcceleration(sensorUnitHandler.getAccelerometer().getAccelerometerValues());
            }
        }
    }

    /**
     * Updates the matrix inverse of the rotation matrix.
     * @param rotationMatrix passed from getRotationMatrixFromVector.
     * @author Emrik
     */
    private void updateMatrix(float[] rotationMatrix) {
        matrix.setEntry(0, 0, rotationMatrix[0]);
        matrix.setEntry(0, 1, rotationMatrix[1]);
        matrix.setEntry(0, 2, rotationMatrix[2]);
        matrix.setEntry(1, 0, rotationMatrix[3]);
        matrix.setEntry(1, 1, rotationMatrix[4]);
        matrix.setEntry(1, 2, rotationMatrix[5]);
        matrix.setEntry(2, 0, rotationMatrix[6]);
        matrix.setEntry(2, 1, rotationMatrix[7]);
        matrix.setEntry(2, 2, rotationMatrix[8]);
    }

    /**
     * Used to transform the acceleration values from the accelerometer to real north, east and down.
     * @param accelerometerValues values collected from the accelerometer.
     * @author Emrik
     */
    private void transformAcceleration(float[] accelerometerValues) {
        RealVector vector = new ArrayRealVector(new double[]{
                accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]
        });
        RealVector resultVector = matrix.operate(vector);
        sensorUnitHandler.getAccelerometer().setAccelerometerValues(new float[]{(float) resultVector.getEntry(0),
                (float) resultVector.getEntry(1), (float) resultVector.getEntry(2)});
        // transformedAcceleration[0] is east, transformedAcceleration[1] is north, transformedAcceleration[2] is vertical
    }

    /**
     * Not used.
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *         {@code SensorManager.SENSOR_STATUS_*}
     * @author Emrik
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

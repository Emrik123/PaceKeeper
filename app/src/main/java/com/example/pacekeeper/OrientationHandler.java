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

public class OrientationHandler implements SensorEventListener {

    /**
     * This class is used to determine the current orientation of the device, which is necessary
     * to determine correct values for the different axes of acceleration.
     * It's derived off a basic model of transformation, only minor alterations were made to the calculations in
     * order to better fit the current application.
     */

    private SensorUnitHandler sensorUnitHandler;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private RealMatrix matrix;
    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private final AccelerometerFilter eastAxisFilter = new AccelerometerFilter();
    private final AccelerometerFilter northAxisFilter = new AccelerometerFilter();

    public OrientationHandler(SensorManager sensorManager, SensorUnitHandler sensorUnitHandler) {
        this.sensorUnitHandler = sensorUnitHandler;
        this.sensorManager = sensorManager;
        matrix = new Array2DRowRealMatrix(3, 3);
    }

    public void startOrientationSensor() {
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorThread = new HandlerThread("Orientation Sensor");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME, sensorHandler);
    }

    public void stopOrientationSensor() {
        sensorManager.unregisterListener(this);
    }

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

    private void transformAcceleration(float[] accelerometerValues) {
        RealVector vector = new ArrayRealVector(new double[]{
                accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]
        });
        RealVector resultVector = matrix.operate(vector);
        // Unfiltered
        sensorUnitHandler.getAccelerometer().setAccelerometerValues(new float[]{(float) resultVector.getEntry(0),
                (float) resultVector.getEntry(1), (float) resultVector.getEntry(2)});
        // transformedAcceleration[0] is east, transformedAcceleration[1] is north, transformedAcceleration[2] is vertical

        // Filtered
        double timeStep = sensorUnitHandler.getAccelerometer().getTimeStep();
        eastAxisFilter.update(resultVector.getEntry(0), timeStep);
        northAxisFilter.update(resultVector.getEntry(1), timeStep);
        sensorUnitHandler.getAccelerometer().setAccelerometerValues(new float[]{(float) eastAxisFilter.getState()[1], (float) northAxisFilter.getState()[1], 0});

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

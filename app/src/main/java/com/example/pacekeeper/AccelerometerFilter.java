package com.example.pacekeeper;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Kalman filter for filtering a single axis of accelerometer data. This filter does <b>not</b> take
 * gravitational forces into account when a vertical axis is filtered.
 * @author Samuel
 */
public class AccelerometerFilter {
    private RealMatrix A, Q, R, H, K, P;
    private RealVector x;
    private double dt;

    /**
     * Class constructor.
     * Initializes matrices of the model.
     * An arbitrary delta time is set as an initial estimate.
     * @author Samuel
     */
    public AccelerometerFilter() {
        dt = 0.03; // Initial value, will be recalculated
        initMatrices();
    }

    /**
     * Initializes matrices A, Q, R, H and P to an initial state.
     * @author Samuel
     */
    private void initMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        double processNoiseVariance = 0.01;
        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * dt * dt * processNoiseVariance + 0.001, 0.5 * dt * processNoiseVariance},
                {0.5 * dt * processNoiseVariance, processNoiseVariance}
        });

        double measurementNoiseVariance = 0.1;
        R = MatrixUtils.createRealMatrix(new double[][]{{measurementNoiseVariance}});

        x = new ArrayRealVector(new double[]{0, 0});
        H = MatrixUtils.createRowRealMatrix(new double[]{1, 0});
        P = MatrixUtils.createRealIdentityMatrix(2);
    }

    /**
     * Recalculates matrices A and Q with a dynamically updated delta time.
     * @author Samuel
     */
    private void recalculateMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        double processNoiseVariance = 0.01;
        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * dt * dt * processNoiseVariance + 0.001, 0.5 * dt * processNoiseVariance},
                {0.5 * dt * processNoiseVariance, processNoiseVariance}
        });
    }

    /**
     * Predicts a presumptive estimate. This predicted state serves as a preliminary guess
     * before considering the next measurement. This predicted state sets the stage for
     * the calculation of the actual estimate.
     * @author Samuel
     */
    private void predict() {
        x = A.operate(x);
        P = A.multiply(P).multiply(A.transpose()).add(Q);
    }

    /**
     * Integrates the last measurement into the state estimate.
     * @param acc The accelerometer measurement to be filtered
     * @author Samuel
     */
    private void estimate(double acc) {
        RealVector accVector = new ArrayRealVector(new double[]{acc});

        RealMatrix S = H.multiply(P).multiply(H.transpose()).add(R);
        K = P.multiply(H.transpose()).multiply(MatrixUtils.inverse(S));

        x = x.add(K.operate(accVector.subtract(H.operate(x))));
        P = P.subtract(K.multiply(H).multiply(P));
    }

    /**
     * Updates the state estimate with a new measurement.
     * @param acc The accelerometer measurement to be filtered
     * @param dt The current delta time
     * @author Samuel
     */
    public void update(double acc, double dt) {
        this.dt = dt;
        recalculateMatrices();
        predict();
        estimate(acc);
    }

    /**
     * Returns the state estimate.
     * @return The values of vector x.
     * @author Samuel
     */
    public double[] getState() {
        return x.toArray();
    }
}

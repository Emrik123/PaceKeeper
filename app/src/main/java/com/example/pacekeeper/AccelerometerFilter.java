package com.example.pacekeeper;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class AccelerometerFilter {
    private RealMatrix A, Q, R, H, K, P;
    private RealVector x;
    private double dt;
    //private final double variance = 0.15;
    private final double initialVariance = 0.15;
    private double processNoiseFactor = 0.5;

    public AccelerometerFilter() {
        dt = 0.5; // Initial value, will be recalculated
        initMatrices();
    }

    private void initMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        // Q is replaced with a scalar, essentially assuming a constant process noise variance across all state variables.
        /*Q = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}}).scalarMultiply(variance);*/

        // Adaptive Q
        /*Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * dt * dt * dt * dt, 0.5 * dt * dt * dt},
                {0.5 * dt * dt * dt, dt * dt}}).scalarMultiply(initialVariance);*/

        Q = MatrixUtils.createRealMatrix(new double[][]{
                {dt * dt, dt},
                {dt, 1}}).scalarMultiply(initialVariance * processNoiseFactor);

        double measurementNoiseVariance = 0.1;
        R = MatrixUtils.createRealMatrix(new double[][]{{measurementNoiseVariance}});

        x = new ArrayRealVector(new double[]{0, 0});
        H = MatrixUtils.createRowRealMatrix(new double[]{1, 0});
        P = MatrixUtils.createRealIdentityMatrix(2);
    }

    private void recalculateMatrices() {
        // Recalculate A and Q with updated dt
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        /*Q = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}}).scalarMultiply(variance);*/

        /*Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * dt * dt * dt * dt, 0.5 * dt * dt * dt},
                {0.5 * dt * dt * dt, dt * dt}}).scalarMultiply(initialVariance * processNoiseFactor);*/

        Q = MatrixUtils.createRealMatrix(new double[][]{
                {dt * dt, dt},
                {dt, 1}}).scalarMultiply(initialVariance * processNoiseFactor);
    }

    private void predict() {
        x = A.operate(x);
        P = A.multiply(P).multiply(A.transpose()).add(Q);
    }

    private void estimate(double acc) {
        RealVector accVector = new ArrayRealVector(new double[]{acc});

        RealMatrix S = H.multiply(P).multiply(H.transpose()).add(R);
        K = P.multiply(H.transpose()).multiply(MatrixUtils.inverse(S));

        /*RealVector innovation = accVector.subtract(H.operate(x));*/
        x = x.add(K.operate(accVector.subtract(H.operate(x))));
        P = P.subtract(K.multiply(H).multiply(P));

        // Dynamic weighting of process noise (Q)?
        /*double innovationNorm = innovation.getNorm();
        if (innovationNorm > Math.sqrt(R.getEntry(0, 0))) {
            processNoiseFactor *= 1.1; // Increase process noise
        } else {
            processNoiseFactor *= 0.9; // Decrease process noise
        }*/
    }

    public void update(double acc, double dt) {
        this.dt = dt;
        recalculateMatrices();
        predict();
        estimate(acc);
    }

    public double[] getState() {
        return x.toArray();
    }
}

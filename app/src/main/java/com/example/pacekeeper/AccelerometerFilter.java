package com.example.pacekeeper;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class AccelerometerFilter {
    private RealMatrix A, Q, R, H, K, P;
    private RealVector x;
    private double dt;

    public AccelerometerFilter() {
        dt = 0.5; // Initial value, will be recalculated
        initMatrices();
    }

    private void initMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        double processNoiseVariance = 0.01;
        Q = MatrixUtils.createRealMatrix(new double[][]{
                {dt * dt * processNoiseVariance, dt * processNoiseVariance},
                {dt * processNoiseVariance, processNoiseVariance}
        });

        double measurementNoiseVariance = 0.1;
        R = MatrixUtils.createRealMatrix(new double[][]{{measurementNoiseVariance}});

        x = new ArrayRealVector(new double[]{0, 0});
        H = MatrixUtils.createRowRealMatrix(new double[]{1, 0});
        P = MatrixUtils.createRealIdentityMatrix(2);
    }

    private void recalculateMatrix() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });
    }

    private void predict() {
        x = A.operate(x);
        P = A.multiply(P).multiply(A.transpose()).add(Q);
    }

    private void estimate(double acc) {
        RealVector accVector = new ArrayRealVector(new double[]{acc});

        RealMatrix S = H.multiply(P).multiply(H.transpose()).add(R);
        K = P.multiply(H.transpose()).multiply(MatrixUtils.inverse(S));

        x = x.add(K.operate(accVector.subtract(H.operate(x))));
        P = P.subtract(K.multiply(H).multiply(P));
    }

    public void update(double acc, double dt) {
        this.dt = dt;
        recalculateMatrix();
        predict();
        estimate(acc);
    }

    public double[] getState() {
        return x.toArray();
    }
}

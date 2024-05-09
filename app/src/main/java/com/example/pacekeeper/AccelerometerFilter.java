package com.example.pacekeeper;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class AccelerometerFilter {
    private RealMatrix A, B, Q, R, H, K, P;
    private RealVector x;
    private double dt;

    public AccelerometerFilter() {
        dt = 0.5; // Initial value, will be recalculated
        initMatrices();
    }

    public void initMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        //B = MatrixUtils.createRealMatrix(new double[][]{})

        //double std; //standard covariance
        //Q = MatrixUtils.createRealMatrix(new double[][]{}).scalarMultiply(std);

        //double noiseVariance;
        //R = MatrixUtils.createRealMatrix(new double[][]{noiseVariance});
        H = MatrixUtils.createRowRealMatrix(new double[]{1, 0});
        P = MatrixUtils.createRealIdentityMatrix(2);
    }

    public void recalculateMatrices() {
        // Recalculate A, B and Q with updated dt
    }

    public void predict() {
        x = A.operate(x);
        P = A.multiply(P).multiply(A.transpose()).add(Q);
    }

    public void update(double acc, double dt) {
        this.dt = dt;
        recalculateMatrices();


    }

    public double[] getState() {
        return x.toArray();
    }
}

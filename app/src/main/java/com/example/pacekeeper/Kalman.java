package com.example.pacekeeper;

import org.apache.commons.math3.linear.*;

public class Kalman {
    /**
     * A Basic Kalman filter implementation used to filter noise from GPS signal when evaluating velocity.
     * The filter follows the extended model for estimating two variables related through a linear state model.
     *  A - State transition matrix describing how the "measured state" changes over time. Acceleration is ignored in the measurement.
     *      [1 dt  0  0] - Where dt is the time interval of change.
     *      [0  1  0  0]
     *      [0  0  1 dt]
     *      [0  0  0  1]
     * <p>
     *  B - State control matrix, describing the linear state model of the transition matrix from state x0 to x1.
     *      [0.5*dt^2  0]
     *      [dt        0]
     *      [0  0.5*dt^2]
     *      [0        dt]
     * <p>
     *  H - Observation matrix determining which variables we observe. In this case velocity.
     *      [0  1  0  0]
     * <p>
     *  Q - Process noise matrix. Sensor noise is determined to be low (ranging from 0.09-0.16)
     *      NOTE: Android accelerometer standard deviation is between 0.025 and 0.044 m/s
     *      [0.25*dt^4  0.5*dt^3  0  0]  * 0.035
     *      [0.5*dt^3    dt^2     0  0]
     *      [0  0  0.25*dt^4  0.5*dt^3]
     *      [0  0  0.5*dt^3       dt^2]
     * <p>
     *  R - Measurement noise covariance. In this case GPS sensor noise, measured to be low (ranging from 0.4-0.7 km/h).
     *      R = 0.06
     * <p>
     *  P - Identity matrix for noise covariance (4x4)
     *      [1  0  0  0]
     *      [0  1  0  0]
     *      [0  0  1  0]
     *      [0  0  0  1]
     * <p>
     *     Note that matrices A, B and Q are recalculated each cycle with a dynamically changing dt.
     */

    private RealMatrix A, B, Q, R, P, K, H;
    private RealVector x, u;
    private double dt;  // Time step

    public Kalman() {
        dt = 0.5;
        initializeMatrices();
    }

    private void initializeMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, dt},
                {0, 0, 0, 1}
        });

        B = MatrixUtils.createRealMatrix(new double[][]{
                {0.5 * Math.pow(dt, 2), 0},
                {dt, 0},
                {0, 0.5 * Math.pow(dt, 2)},
                {0, dt}
        });

        double std = 0.125;
        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3), 0, 0},
                {0.5 * Math.pow(dt, 3), Math.pow(dt, 2), 0, 0},
                {0, 0, 0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3)},
                {0, 0, 0.5 * Math.pow(dt, 3), Math.pow(dt, 2)}
        }).scalarMultiply(std);

        double gpsNoiseVariance = 0.06;
        R = MatrixUtils.createRealMatrix(new double[][]{{gpsNoiseVariance}});
        x = new ArrayRealVector(new double[]{0, 0, 0, 0});
        P = MatrixUtils.createRealIdentityMatrix(4);
        H = MatrixUtils.createRowRealMatrix(new double[]{0, 1, 0, 0});
    }

    public void recalculateMatrices(){
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, dt},
                {0, 0, 0, 1}
        });

        B = MatrixUtils.createRealMatrix(new double[][]{
                {0.5 * Math.pow(dt, 2), 0},
                {dt, 0},
                {0, 0.5 * Math.pow(dt, 2)},
                {0, dt}
        });

        double std = 0.125;
        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3), 0, 0},
                {0.5 * Math.pow(dt, 3), Math.pow(dt, 2), 0, 0},
                {0, 0, 0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3)},
                {0, 0, 0.5 * Math.pow(dt, 3), Math.pow(dt, 2)}
        }).scalarMultiply(std);
    }

    public void predict(double ax, double ay) {
        u = new ArrayRealVector(new double[]{ax, ay});

        x = A.operate(x).add(B.operate(u));
        P = A.multiply(P).multiply(A.transpose()).add(Q);
    }

    public void update(double z, double dt) {
        this.dt = dt;
        recalculateMatrices();
        // Below lines are for measuring weightings for v0 and v1 in a given state (0 current, 1 previous)
        // Not working as intended for now, not sure if necessary but may look into more in the future.
//        double speedMagnitude = Math.sqrt(Math.pow(x.getEntry(1), 2) + Math.pow(x.getEntry(3), 2));
//        if (speedMagnitude == 0) {
//            speedMagnitude = 1;
//        }
//        double vxWeight = x.getEntry(1) / speedMagnitude;
//        double vyWeight = x.getEntry(3) / speedMagnitude;

        RealVector zVector = new ArrayRealVector(new double[]{z});

        RealMatrix S = H.multiply(P).multiply(H.transpose()).add(R);
        K = P.multiply(H.transpose()).multiply(MatrixUtils.inverse(S));

        x = x.add(K.operate(zVector.subtract(H.operate(x))));
        P = P.subtract(K.multiply(H).multiply(P));
    }

    public double[] getState() {
        return x.toArray();
    }
}

package com.example.pacekeeper;

import org.apache.commons.math3.linear.*;

/**
 * Extended Kalman filter for estimating position and velocity.
 * @author Emrik
 */
public class Kalman {
    /**
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
     *  R - Measurement noise covariance. In this case GPS sensor noise, measured to be low (ranging from 0.4-0.7 m/s).
     *      R = 0.6
     * <p>
     *  P - Identity matrix for noise covariance (4x4)
     *      [1  0  0  0]
     *      [0  1  0  0]
     *      [0  0  1  0]
     *      [0  0  0  1]
     * <p>
     *     Note that matrices A, B and Q are recalculated each cycle with a dynamically changing dt.
     *
     */

    private RealMatrix A, B, Q, R, P, K, H;
    private RealVector x, u;
    private double dt;  // Time step
    private double processNoise;
    private double gpsNoise;


    /**
     * Class constructor.
     * Initializes the matrices and sets an arbitrary initial estimate of dt.
     * @author Emrik
     */
    public Kalman() {
        dt = 0.5;
        processNoise = 0.125;
        gpsNoise = 0.06;
        initializeMatrices();
    }

    /**
     * Used to initialize the matrices of the model.
     * @author Emrik
     */
    private void initializeMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        B = MatrixUtils.createRealMatrix(new double[][]{
                {0.5 * Math.pow(dt, 2)},
                {dt}
        });

        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3)},
                {0.5 * Math.pow(dt, 3), Math.pow(dt, 2)}
        }).scalarMultiply(processNoise);

        R = MatrixUtils.createRealMatrix(new double[][]{{gpsNoise}});
        x = new ArrayRealVector(new double[]{0, 0});
        P = MatrixUtils.createRealIdentityMatrix(2);
        H = MatrixUtils.createRowRealMatrix(new double[]{0, 1});
    }

    /**
     * Used to recalculate the matrices affected by the difference in each time step.
     * @author Emrik
     */
    private void recalculateMatrices() {
        A = MatrixUtils.createRealMatrix(new double[][]{
                {1, dt},
                {0, 1}
        });

        B = MatrixUtils.createRealMatrix(new double[][]{
                {0.5 * Math.pow(dt, 2)},
                {dt}
        });

        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3)},
                {0.5 * Math.pow(dt, 3), Math.pow(dt, 2)}
        }).scalarMultiply(gpsNoise);
    }

    /**
     * Used to predict the next measurement.
     * @param a true acceleration
     * @author Emrik
     */
    public void predict(double a) {
        u = new ArrayRealVector(new double[]{a});
        x = A.operate(x).add(B.operate(u));
        P = A.multiply(P).multiply(A.transpose()).add(Q);
    }

    /**
     * Used to update the current model to match against the most recent predicted state.
     * The velocity collected from the GPS is used to correct any major deviance in the estimate.
     * dt represents the time step from the last update.
     * @param z velocity harvested from the GPS.
     * @param dt time step from the last update.
     * @author Emrik
     */
    public void update(double z, double dt) {
        this.dt = dt;
        recalculateMatrices();

        RealVector zVector = new ArrayRealVector(new double[]{z});
        RealMatrix S = H.multiply(P).multiply(H.transpose()).add(R);
        K = P.multiply(H.transpose()).multiply(MatrixUtils.inverse(S));
        x = x.add(K.operate(zVector.subtract(H.operate(x))));
        P = P.subtract(K.multiply(H).multiply(P));
    }

        /**
         * Getter.
         * @return the current values in the measurement matrix x.
         * @author Emrik
         */
    public double[] getState() {
        return x.toArray();
    }
}

package com.example.pacekeeper;

import org.apache.commons.math3.linear.*;

public class Kalman {
    /**
     * A Basic Kalman filter implementation used to filter noise from GPS signal when evaluating velocity.
     * The filter follows the standard model for estimating two variables related through a linear state model.
     *  A - State transition matrix describing how the "measured state" changes over time. Acceleration is ignored in the measurement.
     *      [1 dt] - Where dt is the time interval of change, averaged to about 0.5.
     *      [0  1]
     * <p>
     *  H - Observation matrix determining which variables we observe. In this case velocity.
     *      [0  1]
     * <p>
     *  Q - Process noise matrix. Sensor noise is determined to be low (ranging from 0.1-0.4)
     *      [0.25*dt^4 0.5*dt^3]  * 0.04
     *      [0.5*dt^3      dt^2]
     * <p>
     *  R - Measurement noise covariance
     * <p>
     *  P - Identity matrix for noise covariance (2x2)
     *      [1  0]
     *      [0  1]
     */
    private RealMatrix A, H, Q, R, P, K;

    private RealVector x, xp;
    private double dt;

    public Kalman() {
        initMatrices();
    }

    /**
     * This method updates the current estimate based on the last measurement z.
     * @param z the last measurement update representing velocity.
     * @return the updated prediction of the velocity.
     */
    public double[] update(double z, long dt) {
        if(dt != 0){
            this.dt = dt;
        }
        /*
         * Here the model predicts the next measurement
         */
        xp = A.operate(x);
        P = A.multiply(P).multiply(A.transpose()).add(Q);

        /*
         * Here the model updates the current weighting on the previous measurements to more accurately
         * predict the next value.
         */
        RealMatrix S = H.multiply(P).multiply(H.transpose()).add(R);
        K = P.multiply(H.transpose()).multiply(MatrixUtils.inverse(S));
        x = xp.add(K.operate(new ArrayRealVector(new double[]{z}).subtract(H.operate(xp))));
        P = P.subtract(K.multiply(H).multiply(P));

        /*
         * The returned value is the predicted values in a 2x1 matrix
         * [position, velocity]
         */
        return new double[]{x.getEntry(0), x.getEntry(1)};
    }

    /**
     * Initializes the matrices with determined values of model found above.
     */
    private void initMatrices() {
        dt = 0.5;
        A = MatrixUtils.createRealMatrix(new double[][]{{1, dt}, {0, 1}});
        H = MatrixUtils.createRealMatrix(new double[][]{{0, 1}});
        Q = MatrixUtils.createRealMatrix(new double[][]{
                {0.25 * Math.pow(dt, 4), 0.5 * Math.pow(dt, 3)},
                {0.5 * Math.pow(dt, 3), dt * dt}}).scalarMultiply(0.04);
        x = new ArrayRealVector(new double[]{0, 0});
        R = MatrixUtils.createRealMatrix(new double[][]{{0.12}});
        P = MatrixUtils.createRealIdentityMatrix(2);
    }
}

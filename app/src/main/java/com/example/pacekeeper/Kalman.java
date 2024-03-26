package com.example.pacekeeper;

import android.location.Location;
import org.apache.commons.math3.filter.*;
import org.apache.commons.math3.linear.*;

public class Kalman {
    private KalmanFilter kalmanFilter;
    private ProcessModel processModel;
    private MeasurementModel measurementModel;
    private double timeInterval;
    private double mNoise;
    private double aNoise;

    public Kalman(double tInterval, double mNoise, double aNoise){
        this.timeInterval = tInterval;
        this.mNoise = mNoise;
        this.aNoise = aNoise;
        processModel = initProcessModel();
        measurementModel = initMeasurementModel();
        kalmanFilter = new KalmanFilter(processModel, measurementModel);
    }

    public ProcessModel initProcessModel(){
        double dt = timeInterval;
        double sigmaA2 = Math.pow(aNoise, 2);

        RealMatrix A = new Array2DRowRealMatrix(new double[][]{
                {1, dt, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, dt},
                {0, 0, 0, 1}
        });

        RealMatrix B = new Array2DRowRealMatrix(new double[][]{
                {0},
                {0},
                {0},
                {0}
        });

        RealVector x = new ArrayRealVector(new double[]{0, 0, 0, 0});

        RealMatrix Q = new Array2DRowRealMatrix(new double[][]{
                {Math.pow(dt, 4) / 4, Math.pow(dt, 3) / 2, 0, 0},
                {Math.pow(dt, 3) / 2, Math.pow(dt, 2), 0, 0},
                {0, 0, Math.pow(dt, 4) / 4, Math.pow(dt, 3) / 2},
                {0, 0, Math.pow(dt, 3) / 2, Math.pow(dt, 2)}
        }).scalarMultiply(sigmaA2);

        RealMatrix P0 = MatrixUtils.createRealIdentityMatrix(4).scalarMultiply(1000);

        return new DefaultProcessModel(A, B, Q, x, P0);
    }

    public MeasurementModel initMeasurementModel(){

        RealMatrix H = new Array2DRowRealMatrix(new double[][] { {1d, 0, 0, 0}, {0, 0, 1d, 0} });

        RealMatrix R = new Array2DRowRealMatrix(new double[][] { {Math.pow(mNoise, 2), 0}, {0, Math.pow(mNoise, 2)} });

        return new DefaultMeasurementModel(H, R);
    }


    public Location predictAndCorrect(Location location){
        if(kalmanFilter != null && location!= null){
            double[] m = new double[]{location.getLatitude(), location.getLongitude()};
            kalmanFilter.predict();
            kalmanFilter.correct(m);
            double[] mCorrected = kalmanFilter.getStateEstimation();
            if(mCorrected[0] < 5000 && mCorrected[1] < 5000){
                location.setLatitude(mCorrected[0]/1000);
                location.setLongitude(mCorrected[1]/1000);
            }
        }
        return location;
    }
}

package com.example.android.effectivenavigation;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Debosmit on 5/27/15.
 */
public class Motion {
    private int[][] accelerometer;
    private int[][] gyroscope;
    private int[] accelMagVector;
    private int[] gyroMagVector;
    private boolean isPositive;
    private boolean isNegative;
    private int accelxMax, accelyMax, accelzMax;
    private int[] absX, absY, absZ;
    private final double timeBetweenData = 80.0d;
    private double[] velocity;
    private double maxVelocity;

    private final int SMOOTH_WINDOW_SIZE = 4;       // change depending on length????

    public Motion(int[][] accelerometer, int[][] gyroscope) {
        this(accelerometer, gyroscope, false, false);
    }

    public Motion(int[][] accelerometer, int[][] gyroscope, boolean isPositive, boolean isNegative) {

        Log.d("Accelerometer length: ", "" + accelerometer.length);
        // getting data lines at 80 lines per second
        // getting rid of 40 lines from the top
        if(accelerometer.length > 200) {
            List<int[]> listAccel = new ArrayList<int[]>(Arrays.asList(accelerometer));
            List<int[]> listGyro = new ArrayList<int[]>(Arrays.asList(gyroscope));
            int i = (int)(80*2.25);        // 2.25 seconds worth of data
            // removing data from the top
            while(i > 0) {
                listAccel.remove(0);
                listGyro.remove(0);
                i--;
            }
            i = (int)(80*0.25);             // 0.5 seconds worth of data
            // removing data from the bottom
            while(i > 0) {
                listAccel.remove(listAccel.size()-1);
                listGyro.remove(listGyro.size()-1);
                i--;
            }
            this.accelerometer = listAccel.toArray(new int[][]{});
            this.gyroscope = listGyro.toArray(new int[][]{});
        } else {
            this.accelerometer = accelerometer;
            this.gyroscope = gyroscope;
        }
        this.velocity = new double[accelerometer.length];

        /*this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;*/

        Log.d("this.accelerometer length: ", "" + this.accelerometer.length);


//        this.accelerometer = smoothSliding(accelerometer);
//        this.gyroscope = smoothSliding(gyroscope);


        this.isPositive = isPositive;
        this.isNegative = isNegative;
        accelMagVector = new int[accelerometer.length];
        accelxMax = 0;
        accelyMax = 0;
        accelzMax = 0;
        absX = new int[accelerometer.length];
        absY = new int[accelerometer.length];
        absZ = new int[accelerometer.length];
        this.maxVelocity = 0;
        setMagAcceleration();
        setAbsAccel();
    }

    private void setAbsAccel() {
        int[] accelX = getAccelX();
        int[] accelY = getAccelY();
        int[] accelZ = getAccelZ();
        for(int i = 0 ; i < accelerometer.length ; i++) {
            absX[i] = Math.abs(accelX[i]);
            absY[i] = Math.abs(accelY[i]);
            absZ[i] = Math.abs(accelZ[i]);
        }
    }

    private void setMagAcceleration() {
        double netAcceleration = 0.0d;
        for(int row = 0 ; row < accelerometer.length ; row++) {
            accelMagVector[row] = getMagnitude(accelerometer[row][0],
                    accelerometer[row][1], accelerometer[row][2]);

            accelxMax = Math.abs(accelerometer[row][0]) > accelxMax ? Math.abs(accelerometer[row][0]) : accelxMax;
            accelyMax = Math.abs(accelerometer[row][1]) > accelyMax ? Math.abs(accelerometer[row][1]) : accelyMax;
            accelzMax = Math.abs(accelerometer[row][2]) > accelzMax ? Math.abs(accelerometer[row][2]) : accelzMax;

            if(row == 0)
                this.velocity[row] = 0;
            else {
                netAcceleration = (double)(accelMagVector[row]);
                Log.d("accelMagVector" + row, "" + accelMagVector[row]);
                netAcceleration = ((netAcceleration) / (16384 / 8))*(9.81);
                Log.d("netAcceleration " + row, "" + netAcceleration);
                netAcceleration =- (9.81/2);
                Log.d("netAcceleration - grav: " + row, "" + netAcceleration);
                velocity[row] = velocity[row - 1] + (netAcceleration / timeBetweenData);
                Log.d("Vel " + row, "" + velocity[row]);
            }
        }
        setMaxVelocity();

    }

    private void setMaxVelocity() {
        for(int i = 0 ; i < velocity.length ; i++) {
            maxVelocity = Math.max(maxVelocity, velocity[i]);

        }
    }

    public double getMaxVelocity() { return maxVelocity; }

    public int[] getabsX() { return absX; }

    public int[] getabsY() { return absY; }

    public int[] getabsZ() { return absZ; }

    public int getAbsAvgX() { return getAvg(absX); }

    public int getAbsAvgY() { return getAvg(absY); }

    public int getAbsAvgZ() { return getAvg(absZ); }

    private int getAvg(int[] arr) {
        int sum = 0;

        for(int i = 0 ; i < arr.length ; i++)
            sum += arr[i];

        return (int)sum/arr.length;
    }

    public int getxMax() { return accelxMax; }

    public int getyMax() { return accelyMax; }

    public int getzMax() { return accelzMax; }

    private int getMagnitude(int a, int b, int c) {
        return (int)Math.sqrt(Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2));
    }

    public int[] getAccelMagVector() { return accelMagVector; }

    public void setPositive(boolean setPositive) { this.isPositive = setPositive; }

    public void setNegative(boolean setNegative) { this.isNegative = setNegative; }

    public int[] getAccelX() { return getColumn(accelerometer, 0); }

    public int[] getAccelY() { return getColumn(accelerometer, 1); }

    public int[] getAccelZ() { return getColumn(accelerometer, 2); }

    public int[][] getAccelerometer() { return accelerometer; }

    public int[][] getGyroscope() { return gyroscope; }

    public int[] getGyroX() {
        return getColumn(gyroscope, 0);
    }

    public int[] getGyroY() {
        return getColumn(gyroscope, 1);
    }

    public int[] getGyroZ() {
        return getColumn(gyroscope, 2);
    }

    public boolean getIsPositive() {
        return isPositive;
    }

    public boolean getIsNegative() { return isNegative; }

    private int[] getColumn(int[][] arrayOfInterest2d, int columnOfInterest) {
        int numRows = accelerometer.length;
        int[] colArray = new int[numRows];
        for(int row = 0; row < numRows; row++)
            colArray[row] = arrayOfInterest2d[row][columnOfInterest];
        return colArray;
    }


    // smoothed the passed in 2D int array by using a sliding window to set
    // the center index to the mean of the window
    // returns an integer containing a smoothed array,
    // values < SMOOTH_WINDOW_SIZE && values > SMOOTH_WINDOW_SIZE aren't set
    // smooths columns
    private int[][] smoothSliding(int[][] arr) {
        int[][] smoothArr = new int[arr.length][arr[0].length];

        int[] tempArr;     // to temporarily store columns

        // going over columns
        for(int i = 0 ; i < arr[0].length ; i++) {

            tempArr = getColumn(arr, i);        // storing column

            int[] smoothedTemp = smoothSliding(tempArr);        // getting smoothed columns (vector)

            // storing column in required index to be returned
            for(int  j = 0 ; j < arr.length ; j++)
                smoothArr[j][i] = smoothedTemp[j];
        }

        return smoothArr;
    }

    // smoothed the passed in int array by using a sliding window to set
    // the center index to the mean of the window
    // returns an integer containing a smoothed array,
    // values < SMOOTH_WINDOW_SIZE && values > SMOOTH_WINDOW_SIZE aren't set
    private int[] smoothSliding(int[] arr) {
        int[] smoothArr = new int[arr.length];

        int windowTotal;    // stores the total of the window elements

        for(int i = 0 ; i < arr.length ; i++)
            smoothArr[i] = arr[i];

        // start the sliding window
        for(int i = SMOOTH_WINDOW_SIZE ; i < arr.length - SMOOTH_WINDOW_SIZE ; i++) {
            windowTotal = 0;        // initialized to 0

            // computes the window average
            for(int j = i - SMOOTH_WINDOW_SIZE ; j < i + SMOOTH_WINDOW_SIZE ; j++)
                windowTotal += arr[j];

            smoothArr[i] = (int) windowTotal/SMOOTH_WINDOW_SIZE;        // setting center index
        }
        return smoothArr;
    }

    /*
        TO BE IMPLEMENTED LATER
    // smooths the data in the column vector in int array using a moving average filter
    // returns an integer containing a smoothed array,
    // values < SMOOTH_WINDOW_SIZE && values > SMOOTH_WINDOW_SIZE aren't set
    private int[] smoothMovingAverage(int[] arr) {
        int[] smoothArr = new int[arr.length];

        int windowTotal;    // stores the total of the window elements

        fr()


        // start the sliding window
        for(int i = 0 ; i < arr.length ; i++) {
            windowTotal = 0;        // initialized to 0

            // computes the window average
            for(int j = i - SMOOTH_WINDOW_SIZE ; j < i + SMOOTH_WINDOW_SIZE ; j++)
                windowTotal += arr[j];

            smoothArr[i] = (int) windowTotal/SMOOTH_WINDOW_SIZE;        // setting center index
        }
        return smoothArr;
    }
*/

}

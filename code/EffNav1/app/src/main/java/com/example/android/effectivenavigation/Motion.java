package com.example.android.effectivenavigation;

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

    public Motion(int[][] accelerometer, int[][] gyroscope) {
        this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;
    }

    public void setPositive(boolean setPositive) {
        this.isPositive = setPositive;
    }

    public void setNegative(boolean setNegative) {
        this.isNegative = setNegative;
    }

    public int[] getX() {
        return getColumn(accelerometer, 0);
    }

    public int[] getY() {
        return getColumn(accelerometer, 1);
    }

    public int[] getZ() {
        return getColumn(accelerometer, 2);
    }

    public int[] getGyroX() {
        return getColumn(gyroscope, 0);
    }

    public int[] getGyroY() {
        return getColumn(gyroscope, 1);
    }

    public int[] getGyroZ() {
        return getColumn(gyroscope, 2);
    }

    private int[] getColumn(int[][] arrayOfInterest2d, int columnOfInterest) {
        int numRows = accelerometer.length;
        int[] colArray = new int[numRows];
        for(int row = 0; row < numRows; row++)
            colArray[row] = arrayOfInterest2d[row][columnOfInterest];
        return colArray;
    }
}

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
    private int accelxMax, accelyMax, accelzMax;

    public Motion(int[][] accelerometer, int[][] gyroscope) {
        this(accelerometer, gyroscope, false, false);
    }

    public Motion(int[][] accelerometer, int[][] gyroscope, boolean isPositive, boolean isNegative) {
        this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;
        this.isPositive = isPositive;
        this.isNegative = isNegative;
        accelMagVector = new int[accelerometer.length];
        accelxMax = 0;
        accelyMax = 0;
        accelzMax = 0;
        setMagAcceleration();
    }

    private void setMagAcceleration() {
        for(int row = 0 ; row < accelerometer.length ; row++) {
            accelMagVector[row] = getMagnitude(accelerometer[row][0],
                    accelerometer[row][1], accelerometer[row][2]);
            accelxMax = Math.abs(accelerometer[row][0]) > accelxMax ? Math.abs(accelerometer[row][0]) : accelxMax;
            accelyMax = Math.abs(accelerometer[row][1]) > accelyMax ? Math.abs(accelerometer[row][1]) : accelyMax;
            accelzMax = Math.abs(accelerometer[row][2]) > accelzMax ? Math.abs(accelerometer[row][2]) : accelzMax;
        }
    }

    public int getxMax() { return accelxMax; }

    public int getyMax() { return accelyMax; }

    public int getzMax() { return accelzMax; }

    private int getMagnitude(int a, int b, int c) {
        return (int)Math.sqrt(Math.pow(a,2) + Math.pow(b,2) + Math.pow(c,2));
    }

    public int[] getAccelMagVector() { return getAccelMagVector(); }

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
}

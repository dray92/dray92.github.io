package com.lannbox.rfduinotest;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Jango on 5/11/2015.
 */

public class SensorData {
    public int accelX, accelY, accelZ;
    public int gyroX, gyroY, gyroZ;

    public SensorData(int accelX, int accelY, int accelZ, int gyroX, int gyroY, int gyroZ) {
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;

    }

    public SensorData(String lineOfData) {


        this.accelX = Integer.valueOf(lineOfData.substring(0,4), 16).shortValue();
        this.accelY = Integer.valueOf(lineOfData.substring(4,8), 16).shortValue();
        this.accelZ = Integer.valueOf(lineOfData.substring(8,12), 16).shortValue();
        this.gyroX = Integer.valueOf(lineOfData.substring(12,16), 16).shortValue();
        this.gyroY = Integer.valueOf(lineOfData.substring(16,20), 16).shortValue();
        this.gyroZ = Integer.valueOf(lineOfData.substring(20,24), 16).shortValue();


    }

    public double getMagAccel() {
        return Math.sqrt(Math.pow(accelX,2) + Math.pow(accelY,2) + Math.pow(accelZ,2)) / (Math.pow(2,5));
    }

    public double getMagGyro() {
        return Math.sqrt((Math.pow(gyroX,2) + Math.pow(gyroY,2) + Math.pow(gyroZ,2)) / (Math.pow(2,5)));
    }

    public String toString() {
        return this.accelX + ", " + this.accelY + ", " + this.accelZ + ", " + this.gyroX + ", " + this.gyroY + ", " + this.gyroZ;

    }
}





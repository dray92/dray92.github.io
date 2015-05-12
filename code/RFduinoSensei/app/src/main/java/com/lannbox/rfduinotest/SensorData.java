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
        this.accelX = Integer.parseInt(lineOfData.substring(0,4), 16);
        this.accelY = Integer.parseInt(lineOfData.substring(4,8), 16);
        this.accelZ = Integer.parseInt(lineOfData.substring(8,12), 16);
        this.gyroX = Integer.parseInt(lineOfData.substring(12,16), 16);
        this.gyroY = Integer.parseInt(lineOfData.substring(16,20), 16);
        this.gyroZ = Integer.parseInt(lineOfData.substring(20,24), 16);

    }




    public String toString() {
        return this.accelX + ", " + this.accelY + ", " + this.accelZ + ", " + this.gyroX + ", " + this.gyroY + ", " + this.gyroZ;

    }
}





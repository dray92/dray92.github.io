package com.lannbox.rfduinotest;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Jango on 5/11/2015.
 */
public class SensorData {

    public SensorDataPoint[][] strokeArray;
    public int maxStrokes;
    public int maxNumberOfDataPointsPerStroke;
    public int numberOfStrokes;
    public SensorData(File file, int maxStrokes, int maxNumberOfDataPointsPerStroke) throws FileNotFoundException {
        strokeArray = new SensorDataPoint[maxStrokes+1][maxNumberOfDataPointsPerStroke+1];
        Scanner scanFile = new Scanner(file);
        int i = 0;
        int j = 0;
        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            if (line.contains("SSSSSSSSSSSS")) {
                i++;
                j = 0;

            } else if (!line.contains("NNNNNNNNNNNN") && !line.contains("PPPPPPPPPPPP"))  {
                j++;
                insertDataInStrokeArray(i, j, line);

            }
        }
        numberOfStrokes = i;
    }
    // returns the number of Strokes given a data file
    public int numberOfStrokes(File file) throws FileNotFoundException {
        Scanner scanFile = new Scanner(file);
        int numberOfStrokes = 0;
        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            if (line.contains("SSSSSSSSSSSS")) {
                numberOfStrokes++;

            }
        }
        return numberOfStrokes;

    }

    public void insertDataInStrokeArray(int i, int j, String line) {
        SensorDataPoint dataPoint = new SensorDataPoint(line);
        if ((i <= maxStrokes + 1) && (i <= maxNumberOfDataPointsPerStroke + 1))
            strokeArray[i][j] = dataPoint;
    }

    public class SensorDataPoint {
        public int accelX, accelY, accelZ;
        public int gyroX, gyroY, gyroZ;


        public SensorDataPoint(int accelX, int accelY, int accelZ, int gyroX, int gyroY, int gyroZ) {
            this.accelX = accelX;
            this.accelY = accelY;
            this.accelZ = accelZ;
            this.gyroX = gyroX;
            this.gyroY = gyroY;
            this.gyroZ = gyroZ;

        }

        public SensorDataPoint(String lineOfData) {
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

}



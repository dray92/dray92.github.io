package com.example.android.effectivenavigation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created by Debosmit on 5/27/15.
 */
public class TempFileHandler {

    private String myFile;  // 'return' delimited string containing contents of temp file
    private int numMotions;
    // initializes fileHandler for temp file, global file contents string
    public TempFileHandler(String filepath, String filename) throws IOException {
        //first thing -> getting pieces of motion (sss ... rrr)
        // create motion object
        // have a getMotion[] or something
        // each Motion object will have it's own bunch of stuff
        // it's already pretty complex, simplicity is key


        myFile = readFile(filepath + "/" + filename);         // converts stream to string


    }

    private String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    // returns string array, each index of which contains 'one' motion data string,
    // 'return' delimited
    private String[] getMotionStrings() {
        String[] allLines = myFile.split("\n");
        String oneMotion = "";
        boolean dataIncoming = false;

        // android doesn't have stringutils
        // int numMotions = StringUtils.countOccurrencesOf("a.b.c.d", "."); // doesn't work here

        // so I came up with something cooler :)
        // number of stop presses = number of motions
        numMotions = myFile.length() - myFile.replace("R", "").length();
        // for those who didn't get what I did there
        // if I replace the R's with nothing, the string reduces in size
        // that means the reduction is the number of R's
        // here, the number of R's is the number of stops = number of motions :)

        String[] motions = new String[numMotions];
        int currentIndex = 0;

        for(int i = 0 ; i < allLines.length ; i++) {
            // start button is encountered => time to start collecting data from the next line
            if(allLines[i].startsWith("S")) {
                dataIncoming = true;
                continue;
            }

            // when single motion stream is on, and stop button is pressed
            // need to save existing data, empty buffer and proceed to next line
            if(dataIncoming && allLines[i].startsWith("R")) {
                dataIncoming = false;
                // need to put oneMotion somewhere for storage
                // clear out oneMotion to prep for next motion
                motions[currentIndex++] = oneMotion;
                oneMotion = "";
                continue;
            }

            // in array indices that contain real data, need to store in buffer
            if(dataIncoming)
                oneMotion += allLines[i] + "\n";

        }

        return motions;
    }

    public int getNumMotions() {
        return numMotions;
    }

    // horrible style for getter
    // SHOULD NOT DO PROCESSING HERE
    // doing it for quick turnover
    private Motion[] getMotions() {
        String[] myMotionStrings = getMotionStrings();
        Motion[] myMotions = new Motion[getNumMotions()];

        int accelerometer[][];
        int gyroscope[][];
        // parsing over each string[] index of individual motion
        for(int i = 0; i < myMotions.length ; i++) {
            // accel, gyro data lines
            String quaternions[] = myMotionStrings[i].split("\n");

            // this is where the data is stored
            accelerometer = new int[quaternions.length][3];
            gyroscope = new int[quaternions.length][3];

            // going over every single line in an individual motion
            for(int j = 0 ; j < quaternions.length ; j++) {
                accelerometer[j][0] = Integer.parseInt(quaternions[j].substring(0,4));
                accelerometer[j][1] = Integer.parseInt(quaternions[j].substring(4,8));
                accelerometer[j][2] = Integer.parseInt(quaternions[j].substring(8,12));
                gyroscope[j][0] = Integer.parseInt(quaternions[j].substring(12,16));
                gyroscope[j][1] = Integer.parseInt(quaternions[j].substring(16,20));
                gyroscope[j][2] = Integer.parseInt(quaternions[j].substring(20,24));
            }
            myMotions[i] = new Motion(accelerometer, gyroscope);
        }
        return myMotions;

    }
}

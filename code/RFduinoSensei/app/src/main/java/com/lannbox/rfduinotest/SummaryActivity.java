package com.lannbox.rfduinotest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


// todo: make a function that trims the data based on the threshold. 7FFF or some other value
// detect shots by seeing when data exceeds the threshold



public class SummaryActivity extends AppCompatActivity {
    public final int CONSISTENCY_THRESHOLD = 20000000;
    private String sportSelected;
    private String formSelected;



    private List<List<SensorData>> sensorData;




    public double dtw(double[] t, double[] r) {
        int d[][];
        d = new int[t.length][r.length];
        for(int i = 0  ; i < t.length; i++)
            for(int j = 0  ; j < r.length ; j++)
                d[i][j]= (int) Math.pow((t[i]-r[j]), 2);

        double[][] D = new double[t.length][r.length];

        D[0][0] = d[0][0];

        for(int i = 1 ; i < t.length ; i++)
            D[i][1]=d[i][1]+D[i-1][1];

        for(int i = 1 ; i < r.length ; i++)
            D[1][i]=d[1][i]+D[1][i-1];

        for(int n = 1 ; n < t.length ; n++)
            for(int m = 1 ; m < r.length ; m++)
                D[n][m] = d[n][m] + min( D[n-1][m], D[n-1][m-1], D[n][m-1] );

        return D[t.length-1][r.length-1];

    }

    private double min(double a, double b, double c) {
        double min = Math.min( a, b);
        return Math.min(min, c);
    }

    public void fillSensorDataList(File file) throws FileNotFoundException {
        sensorData = new LinkedList<List<SensorData>>();
        Scanner scanFile = new Scanner(file);
        String line = "";
        int i = 0;
        int j = 0;
        while (scanFile.hasNextLine()) {

            line = scanFile.nextLine();
            if (line.equals("SSSSSSSSSSSS")) {
                sensorData.add(i, new LinkedList<SensorData>());
                while (scanFile.hasNextLine() && !line.equals("NNNNNNNNNNNN")  && !line.equals("PPPPPPPPPPPP") && !line.equals("RRRRRRRRRRRR")) {


                    line = scanFile.nextLine();
                    if (!line.equals("NNNNNNNNNNNN") && !line.equals("PPPPPPPPPPPP") && !line.equals("RRRRRRRRRRRR")) {
                        sensorData.get(i).add(j, new SensorData(line));
                        j++;
                    }


                }

                i++;
                j = 0;

            }
        }



    }
    public void fillSensorDataList2(File file) throws FileNotFoundException {
        sensorData = new LinkedList<List<SensorData>>();
        Scanner scanFile = new Scanner(file);
        int i = 0;
        int j = 0;
        while (scanFile.hasNextLine()) {

            String line = scanFile.nextLine();
            if (line.contains("SSSSSSSSSSSS")) {
                sensorData.add(i, new LinkedList<SensorData>());

                while (scanFile.hasNextLine() && !line.contains("NNNNNNNNNNNN") && !line.contains("PPPPPPPPPPPP") && !line.contains("523030303030303030303030")) {
                    line = scanFile.nextLine();
                    if (!line.contains("NNNNNNNNNNNN") && !line.contains("PPPPPPPPPPPP") && !line.contains("523030303030303030303030")) {
                        sensorData.get(i).add(j, new SensorData(line));
                        j++;
                    }


                }

                i++;
                j = 0;

            }
        }



    }

    public double calculateMag(int x, int y, int z) {
        return Math.sqrt(1.0*(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z, 2)));
    }

    public void printSensorDataList() {
        for (int i = 0; i < sensorData.size(); i++) {
            Log.d("Sensor Data:", "new stroke");
            for (int j = 0; j < sensorData.get(i).size(); j++) {
                SensorData s = sensorData.get(i).get(j);
                Log.d("Sensor Data:", s.toString());
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();




        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);


    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent();
//        intent.putExtra("SPORT_ID", formSelected);
//        intent.putExtra("FORM_ID", sportSelected);
//        setResult(RESULT_OK, intent);
//        finish();
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_summary, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        switch(item.getItemId()) {
//        case android.R.id.home:
//            Intent upIntent = NavUtils.getParentActivityIntent(this);
//            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
//                TaskStackBuilder.create(this)
//                    .addNextIntentWithParentStack(upIntent)
//                    .startActivities();
//            } else {
//                NavUtils.navigateUpTo(this, upIntent);
//
//
//            }
//
//            return true;
//
//
//        }
//
//
//        //noinspection SimplifiableIfStatement
//        if (item.getItemId() == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    public void calculateConsistencyScore(View view) throws FileNotFoundException {
        int consistencyScore = 0;
        // get SD card directory
        File sdcard = new File(Environment.getExternalStorageDirectory(), "temp_sensei");
        // get the text file
        String formSelected = returnFormSelected();
        Log.d("Form selected in ccs:", formSelected);
        String filename = "data" + formSelected + ".txt";
        File file = new File(sdcard, filename);

        Scanner scanFile = new Scanner(file);

        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            Log.d("line:", line);
        }

        fillSensorDataList(file);

        printSensorDataList();
        int numberOfStrokes = sensorData.size();


        Log.d("Number of strokes", Integer.toString(numberOfStrokes));

        for (int i = 0; i < numberOfStrokes - 1; i++) {
            consistencyScore += calculateConsistencyScoreForTwoData2(i, i+1);
        }
        Log.d("consitencyScore:", Integer.toString(consistencyScore)+" out of " + Integer.toString((numberOfStrokes-1)*3));



//        double t[] = {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0};
//        double u[] = {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0};
//        double doublet[] = {2.0, 4,0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0};
//
//        Log.d("DTW: ", Double.toString(dtw(t,doublet)));










    }
    public int calculateConsistencyScoreForTwoData2(int index1, int index2){
        double dtwAccelX = 0.0;
        double dtwAccelY = 0.0;
        double dtwAccelZ = 0.0;
        int consistencyScore = 0;
        if (index1 < sensorData.size() && index2 < sensorData.size() && index1 != index2) {
            List<SensorData> data1 = sensorData.get(index1);
            List<SensorData> data2 = sensorData.get(index2);
            double raX[] = new double[data1.size()];
            double saX[] = new double[data2.size()];
            double raY[] = new double[data1.size()];
            double saY[] = new double[data2.size()];
            double raZ[] = new double[data1.size()];
            double saZ[] = new double[data2.size()];


            for (int i = 0; i < data1.size(); i++) {
                raX[i] = data1.get(i).accelX;
                raY[i] = data1.get(i).accelY;
                raZ[i] = data1.get(i).accelZ;

            }
            for (int j = 0; j < data2.size(); j++) {
                saX[j] = data2.get(j).accelX;
                saY[j] = data2.get(j).accelY;
                saZ[j] = data2.get(j).accelZ;
            }






            Log.d("array1 accelX:", Arrays.toString(raX));
            Log.d("array2 accelX:", Arrays.toString(saX));

            Log.d("array1 accelY:", Arrays.toString(raY));
            Log.d("array2 accelY:", Arrays.toString(saY));

            Log.d("array1 accelZ:", Arrays.toString(raZ));
            Log.d("array2 accelZ:", Arrays.toString(saZ));

            dtwAccelX = dtw(raX, saX);
            dtwAccelY = dtw(raY, saY);
            dtwAccelZ = dtw(raZ, saZ);


            Log.d("DTW accelX: ", Double.toString(dtwAccelX));
            Log.d("DTW accelY: ", Double.toString(dtwAccelY));
            Log.d("DTW accelZ: ", Double.toString(dtwAccelZ));
        } else {
            Log.d("Error: index OOB", Integer.toString(index1) + " " + Integer.toString(index2));
        }
        if (dtwAccelX < CONSISTENCY_THRESHOLD) {
            consistencyScore+= 1;
        }
        if (dtwAccelY < CONSISTENCY_THRESHOLD) {
            consistencyScore+= 1;
        }
        if (dtwAccelZ < CONSISTENCY_THRESHOLD) {
            consistencyScore+= 1;
        }
        Log.d("consitencyScore for 2 data: ", Integer.toString(consistencyScore));
        return consistencyScore;
    }



    public double calculateConsistencyScoreForTwoData(int index1, int index2){
        double dtwAccel = 0.0;
        double dtwGyro = 0.0;

        if (index1 < sensorData.size() && index2 < sensorData.size() && index1 != index2) {
            List<SensorData> data1 = sensorData.get(index1);
            List<SensorData> data2 = sensorData.get(index2);
            double r[] = new double[data1.size()];
            double s[] = new double[data2.size()];
            double r2[] = new double[data1.size()];
            double s2[] = new double[data2.size()];

            for (int i = 0; i < data1.size(); i++) {
                r[i] = data1.get(i).getMagAccel();
            }
            for (int j = 0; j < data2.size(); j++) {
                s[j] = data2.get(j).getMagAccel();
            }

            for (int k = 0; k < data1.size(); k++) {
                r2[k] = data1.get(k).getMagGyro();
            }
            for (int l = 0; l < data2.size(); l++) {
                s2[l] = data2.get(l).getMagGyro();
            }



            Log.d("R array:", Arrays.toString(r));
            Log.d("S array:", Arrays.toString(s));

            Log.d("R2 array:", Arrays.toString(r2));
            Log.d("S2 array:", Arrays.toString(s2));

            dtwAccel = dtw(r, s);
            dtwGyro = dtw(r2, s2);

            Log.d("DTW accel: ", Double.toString(dtwAccel));
            Log.d("DTW gyro: ", Double.toString(dtwGyro));
            Log.d("DTW combined: ", Double.toString(dtwAccel + dtwGyro));
        } else {
            Log.d("Error: index OOB", Integer.toString(index1) + " " + Integer.toString(index2));
        }
        return dtwAccel + dtwGyro;
    }


    public void reportStatistics(View view) throws FileNotFoundException {
        int totalStrokes = 0;
        int madeStrokes = 0;
        int missedStrokes = 0;
        double percentage = 0.0;
        String formSelected = returnFormSelected();
        String fileNameToReportStatistics = "data" + formSelected + ".txt";
        File root = new File(Environment.getExternalStorageDirectory(), "temp_sensei");
        if (!root.exists()) {
            root.mkdirs();
        }

        File fileToReportStatistics = new File(root, fileNameToReportStatistics);
        if (fileToReportStatistics.exists()) {
            Scanner scanFile = new Scanner(fileToReportStatistics);

            while (scanFile.hasNextLine()) {
                String line = scanFile.nextLine();
                if (line.contains("NNNNNNNNNNNN")) {

                    missedStrokes++;
                    totalStrokes++;
                } else if (line.contains("PPPPPPPPPPPP")) {

                    madeStrokes++;
                    totalStrokes++;
                }

                Log.d("Summary-Line:", line);
            }

            percentage = (madeStrokes*1.0)/totalStrokes;

            Log.d("Statistics: Made:", Integer.toString(madeStrokes));
            Log.d("Statistics: Missed:", Integer.toString(missedStrokes));
            Log.d("Summary-Statistics:", Double.toString(percentage));
        }



    }
    public void deleteData(View view) throws FileNotFoundException {

        RadioGroup radioFormGroup = (RadioGroup) findViewById(R.id.formGroupSummary);
        //radioSportGroup = (RadioGroup) findViewById(R.id.sportGroup);
        int selectedFormId = radioFormGroup.getCheckedRadioButtonId();
        //int selectedSportId = radioSportGroup.getCheckedRadioButtonId();
        RadioButton radioFormSelected = (RadioButton) findViewById(selectedFormId);
        // radioFormSelected = (RadioButton) findViewById(selectedSportId);

        String formSelected = returnFormSelected();

        // String sportSelected = radioSportSelected.getText().toString();
        String fileNameToDelete = "data" + formSelected + ".txt";
        FileHelper.clearContents(fileNameToDelete, getApplicationContext());
    }

    private String returnFormSelected(){
        RadioGroup radioFormGroup = (RadioGroup) findViewById(R.id.formGroupSummary);
        //radioSportGroup = (RadioGroup) findViewById(R.id.sportGroup);
        int selectedFormId = radioFormGroup.getCheckedRadioButtonId();
        //int selectedSportId = radioSportGroup.getCheckedRadioButtonId();
        RadioButton radioFormSelected = (RadioButton) findViewById(selectedFormId);
        // radioFormSelected = (RadioButton) findViewById(selectedSportId);

        return radioFormSelected.getText().toString();


    }
}

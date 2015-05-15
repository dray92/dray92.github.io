package com.lannbox.rfduinotest;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.lannbox.rfduinotest.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;






public class SummaryActivity extends AppCompatActivity {
    private String sportSelected;
    private String formSelected;
    private int formSelectedInt;


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
        int i = 0;
        int j = 0;
        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            if (line.contains("SSSSSSSSSSSS")) {
                sensorData.add(i, new LinkedList<SensorData>());
                while (scanFile.hasNextLine() && !line.contains("NNNNNNNNNNNN") && !line.contains("PPPPPPPPPPPP") && !line.contains("RRRRRRRRRRRR")) {
                    line = scanFile.nextLine();
                    if (!line.contains("NNNNNNNNNNNN") && !line.contains("PPPPPPPPPPPP") && !line.contains("RRRRRRRRRRRR")) {
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
        sportSelected = intent.getStringExtra("SPORT_ID");
        formSelected = intent.getStringExtra("FORM_ID");
        formSelectedInt = Integer.parseInt(formSelected);
        Log.d("sport id:", sportSelected);
        Log.d("form id:", formSelected);


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

        // get SD card directory
        File sdcard = new File(Environment.getExternalStorageDirectory(), "temp_sensei");
        // get the text file
        String filename = "data" + formSelectedInt + ".txt";
        File file = new File(sdcard, filename);

        Scanner scanFile = new Scanner(file);

        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            Log.d("line:", line);
        }

        fillSensorDataList(file);

        printSensorDataList();

        List<SensorData> data1 = sensorData.get(0);
        List<SensorData> data2 = sensorData.get(1);
        double r[] = new double[data1.size()];
        double s[] = new double[data2.size()];
        double r2[] = new double[data1.size()];
        double s2[] = new double[data2.size()];
        for (int k = 0; k < data1.size(); k++) {
            r2[k] = data1.get(k).getMagGyro();
        }
        for (int l = 0; l < data2.size(); l++) {
            s2[l] = data2.get(l).getMagGyro();
        }
        for (int i = 0; i < data1.size(); i++) {
            r[i] = data1.get(i).getMagAccel();
        }
        for (int j = 0; j < data2.size(); j++) {
            s[j] = data2.get(j).getMagAccel();
        }


        Log.d("R array:", Arrays.toString(r));
        Log.d("S array:", Arrays.toString(s));

        Log.d("R2 array:", Arrays.toString(r2));
        Log.d("S2 array:", Arrays.toString(s2));
        Log.d("DTW combined: ", Double.toString(dtw(r, s) + dtw(r2, s2)));

//        double t[] = {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0};
//        double u[] = {1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0};
//        double doublet[] = {2.0, 4,0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0};
//
//        Log.d("DTW: ", Double.toString(dtw(t,doublet)));










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

        return formSelected = radioFormSelected.getText().toString();


    }
}

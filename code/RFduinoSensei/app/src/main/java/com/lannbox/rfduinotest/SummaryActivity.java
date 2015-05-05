package com.lannbox.rfduinotest;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.lannbox.rfduinotest.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class SummaryActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            calculateConsistencyScore(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void calculateConsistencyScore(int formNumber) throws FileNotFoundException {

        // get SD card directory
        File sdcard = new File(Environment.getExternalStorageDirectory(), "Notes");
        // get the text file
        File file = new File(sdcard, "data1.txt");

        Scanner scanFile = new Scanner(file);

        while (scanFile.hasNextLine()) {
            String line = scanFile.nextLine();
            Log.d("line:", line);
        }



    }
}

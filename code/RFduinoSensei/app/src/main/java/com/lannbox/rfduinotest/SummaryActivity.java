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

import com.lannbox.rfduinotest.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class SummaryActivity extends AppCompatActivity {
    private String sportSelected;
    private String formSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        sportSelected = intent.getStringExtra("SPORT_ID");
        formSelected = intent.getStringExtra("FORM_ID");

        Log.d("sport id:", sportSelected);
        Log.d("form id:", formSelected);


        try {
            calculateConsistencyScore(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

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
import java.util.Scanner;

public class SummaryActivity extends AppCompatActivity {
    private String sportSelected;
    private String formSelected;
    private int formSelectedInt;
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



    }

    public void deleteData(View view) throws FileNotFoundException {

        RadioGroup radioFormGroup = (RadioGroup) findViewById(R.id.formGroupSummary);
        //radioSportGroup = (RadioGroup) findViewById(R.id.sportGroup);
        int selectedFormId = radioFormGroup.getCheckedRadioButtonId();
        //int selectedSportId = radioSportGroup.getCheckedRadioButtonId();
        RadioButton radioFormSelected = (RadioButton) findViewById(selectedFormId);
        // radioFormSelected = (RadioButton) findViewById(selectedSportId);

        String formSelected = radioFormSelected.getText().toString();

        // String sportSelected = radioSportSelected.getText().toString();
        String fileNameToDelete = "data" + formSelected + ".txt";
        FileHelper.clearContents(fileNameToDelete, getApplicationContext());
    }
}

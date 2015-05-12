package com.lannbox.rfduinotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private RadioGroup radioFormGroup;
    private RadioButton radioFormSelected;
    private RadioGroup radioSportGroup;
    private RadioButton radioSportSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void goToAnalyzeScreen(View view) {
        radioFormGroup = (RadioGroup) findViewById(R.id.formGroup);
        radioSportGroup = (RadioGroup) findViewById(R.id.sportGroup);
        int selectedFormId = radioFormGroup.getCheckedRadioButtonId();
        int selectedSportId = radioSportGroup.getCheckedRadioButtonId();
        radioFormSelected = (RadioButton) findViewById(selectedFormId);
        radioSportSelected = (RadioButton) findViewById(selectedSportId);

        String formSelected = radioFormSelected.getText().toString();
        String sportSelected = radioSportSelected.getText().toString();
        Log.d("Radio Form Selected:", formSelected);
        Log.d("Radio Sport Selected:", sportSelected);


        Intent intent = new Intent(this, AnalyzeActivity.class);

        intent.putExtra("SPORT_ID", formSelected);
        intent.putExtra("FORM_ID", sportSelected);

        startActivity(intent);
    }


}


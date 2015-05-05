package com.lannbox.rfduinotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class MainActivity extends Activity {
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
        Log.d("Radio Form Selected:", radioFormSelected.getText().toString());
        Log.d("Radio Sport Selected:", radioSportSelected.getText().toString());

        Intent intent = new Intent(this, AnalyzeActivity.class);
        startActivity(intent);
    }


}


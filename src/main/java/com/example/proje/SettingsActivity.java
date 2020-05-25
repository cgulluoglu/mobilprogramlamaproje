package com.example.proje;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner,ringtoneSpinner;
    private EditText timeEt;
    private CheckBox darkThemeCb;
    SharedPreferences sharedPreferences;
    private Button saveB, homeB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveB = findViewById(R.id.saveB);
        homeB = findViewById(R.id.homeB);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.remind_again, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        ringtoneSpinner = (Spinner)findViewById(R.id.ringtoneSpinner);
        ringtoneSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.ringtone, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtoneSpinner.setAdapter(adapter1);

        timeEt = findViewById(R.id.timeEt);
        darkThemeCb = findViewById(R.id.darkThemeCb);

        sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);

        timeEt.setText(sharedPreferences.getString("remind_time", null));
        darkThemeCb.setChecked(sharedPreferences.getBoolean("darkTheme", false));
        String spinnerChoice = sharedPreferences.getString("spinner", "None");
        String ringtoneChoice = sharedPreferences.getString("ringtone_choice", "Default");

        if (spinnerChoice.equals("Daily")) {
            spinner.setSelection(0);
        } else if (spinnerChoice.equals("Weekly")) {
            spinner.setSelection(1);
        } else if (spinnerChoice.equals("Monthly")) {
            spinner.setSelection(2);
        } else if (spinnerChoice.equals("Yearly")) {
            spinner.setSelection(3);
        } else {
            spinner.setSelection(4);
        }

        if(ringtoneChoice.equals("Default")) {
            ringtoneSpinner.setSelection(0);
        } else {
            ringtoneSpinner.setSelection(1);
        }

        timeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog(timeEt);
            }
        });
        timeEt.setFocusable(false);
        timeEt.setKeyListener(null);
    }

    public void saveSettings(View view) { // ayarları saved preferences'a kaydeden fonksiyon
        String time,spinnerChoice,ringtoneChoice;
        Boolean darkTheme;

        time = timeEt.getText().toString();
        spinnerChoice = spinner.getSelectedItem().toString();
        darkTheme = darkThemeCb.isChecked();
        ringtoneChoice = ringtoneSpinner.getSelectedItem().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("remind_time", time);
        editor.putBoolean("darkTheme", darkTheme);
        editor.putString("spinner", spinnerChoice);
        editor.putString("ringtone_choice", ringtoneChoice);
        editor.commit();
        Toast.makeText(SettingsActivity.this, "Settings saved successfully!", Toast.LENGTH_LONG).show();

    }
    public void goToMainActivity(View view) { //ana menü
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void showTimeDialog(final EditText remindTimeEt) { //timepicker fonksiyonu
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                calendar.set(Calendar.HOUR_OF_DAY, i);
                calendar.set(Calendar.MINUTE,i1);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                remindTimeEt.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };
        new TimePickerDialog(SettingsActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { //spinnerın rengini değiştiren fonksiyon, dark theme için
        String item = parent.getItemAtPosition(position).toString();
        sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        Boolean darkMode = sharedPreferences.getBoolean("darkTheme",false);
        if(darkMode && ((TextView) parent.getChildAt(0))!=null)
            ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

package com.example.proje;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventInfoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static mySQLiteDBHandler dbHandler;
    private SQLiteDatabase sqLiteDatabase;
    private EditText eventNameEt, eventDescriptionEt,startDateEt, endDateEt,remindTimeEt, addressEt;
    private Spinner spinner;
    private SharedPreferences sharedPreferences;
    private Button saveB;
    private CheckBox ringCb,vibrateCb;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //bu aktivite eventActivity ile hemen hemen aynı, bu activity ilk event ekleme ekranı
        super.onCreate(savedInstanceState);             // eventActivity'de ise güncelleme ve silme işlemleri yapılabiliyor.
        setContentView(R.layout.activity_event_info);

        sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.remind_again, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        eventNameEt = findViewById(R.id.eventNameEt);
        eventDescriptionEt = findViewById(R.id.descriptionEt);
        startDateEt = findViewById(R.id.startDateEt);
        endDateEt = findViewById(R.id.endDateEt);
        remindTimeEt = findViewById(R.id.remindTimeEt);
        addressEt = findViewById(R.id.addressEt);
        vibrateCb = findViewById(R.id.vibrateCb);
        ringCb = findViewById(R.id.ringCb);
        saveB = findViewById(R.id.saveB);

        dbHandler = MainActivity.getDbHandler();
        sqLiteDatabase = dbHandler.getWritableDatabase();

        /// Default belirlenen değerlerin sharedpreferences'dan alınması
        remindTimeEt.setText(sharedPreferences.getString("remind_time", "21:00"));

        String spinnerChoice = sharedPreferences.getString("spinner", "None");
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


        startDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(startDateEt);
            }
        });
        endDateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(endDateEt);
            }
        });
        remindTimeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog(remindTimeEt);
            }
        });

        // bu 6 satır datepicker ve timepickerda kullanıcının onları kullanmadan yazmasını engellemek için
        // ayrıca datepickerin açılması için 2 tık gerekiyordu, onun da önüne böyle geçebildim
        startDateEt.setFocusable(false);
        startDateEt.setKeyListener(null);
        endDateEt.setFocusable(false);
        endDateEt.setKeyListener(null);
        remindTimeEt.setFocusable(false);
        remindTimeEt.setKeyListener(null);
    }

    private void showTimeDialog(final EditText remindTimeEt) {
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
        new TimePickerDialog(EventInfoActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true).show();
    }

    private void showDateDialog(final EditText startDateEt) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                calendar.set(Calendar.YEAR, i);
                calendar.set(Calendar.MONTH, i1);
                calendar.set(Calendar.DAY_OF_MONTH, i2);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                startDateEt.setText(simpleDateFormat.format(calendar.getTime()));

            }
        };
        new DatePickerDialog(EventInfoActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    public void saveToDatabase(View View) {

        // start date su anki tarih karsilastirma
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar today = Calendar.getInstance();
        String todayDate = sdf.format(today.getTime());
        try {
            Date start_date = sdf.parse(startDateEt.getText().toString());
            Date end_date = sdf.parse(endDateEt.getText().toString());
            Date today_date = sdf.parse(todayDate);
            if(start_date.compareTo(today_date) >= 0 && start_date.compareTo(end_date) <= 0) {

                // ALARM MUHABBETLERİ GİRİŞİMLERİ
                Calendar calNow = Calendar.getInstance();
                Calendar calSet = (Calendar) calNow.clone();
                calSet.set(Calendar.HOUR_OF_DAY, Integer.parseInt(remindTimeEt.getText().toString().substring(0,2)));
                calSet.set(Calendar.MINUTE, Integer.parseInt(remindTimeEt.getText().toString().substring(3,5)));
                calSet.set(Calendar.SECOND, 0);
                calSet.set(Calendar.MILLISECOND, 0);
                calSet.set(Calendar.YEAR, Integer.parseInt(startDateEt.getText().toString().substring(0,4)));
                calSet.set(Calendar.MONTH, Integer.parseInt(startDateEt.getText().toString().substring(5,7))-1); //months are zero based
                calSet.set(Calendar.DATE, Integer.parseInt(startDateEt.getText().toString().substring(8,10)));

                if(calSet.compareTo(calNow) <= 0) {
                    Toast.makeText(EventInfoActivity.this, "Invalid time.", Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues contentValues = new ContentValues();

                    contentValues.put("Start_Date",startDateEt.getText().toString());
                    contentValues.put("Event", eventNameEt.getText().toString());
                    contentValues.put("End_Date", endDateEt.getText().toString());
                    contentValues.put("Description", eventDescriptionEt.getText().toString());
                    contentValues.put("Time", remindTimeEt.getText().toString());
                    contentValues.put("Address", addressEt.getText().toString());
                    contentValues.put("Remind_Again",spinner.getSelectedItem().toString());

                    if( vibrateCb.isChecked()){
                        contentValues.put("Vibrate", "True");
                    } else {
                        contentValues.put("Vibrate", "False");
                    }

                    if( ringCb.isChecked()){
                        contentValues.put("Ring", "True");
                    } else {
                        contentValues.put("Ring", "False");
                    }
                    long entry = sqLiteDatabase.insert("EventCalendar", null, contentValues);
                    Integer entryId = (int) entry;
                    setAlarm(calSet,entryId);
                }

            } else {
                Toast.makeText(EventInfoActivity.this, "Invalid date", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void setAlarm(Calendar alarmCalender, int entryId){

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.putExtra("EventName", eventNameEt.getText().toString());
        intent.putExtra("EventDescription", eventDescriptionEt.getText().toString());
        intent.putExtra("id", entryId);
        if( vibrateCb.isChecked()){
            intent.putExtra("Vibrate", "True");
        } else {
            intent.putExtra("Vibrate", "False");
        }
        if( ringCb.isChecked()){
            intent.putExtra("Ring", "True");
        } else {
            intent.putExtra("Ring", "False");
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), entryId, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        String spinnerText = spinner.getSelectedItem().toString();
        switch (spinnerText) {
            case "Daily":
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalender.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
            case "Weekly":
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalender.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, pendingIntent);
                break;
            case "Monthly":
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalender.getTimeInMillis(), AlarmManager.INTERVAL_DAY*30, pendingIntent);
                break;
            case "Yearly":
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalender.getTimeInMillis(), AlarmManager.INTERVAL_DAY*365, pendingIntent);
                break;
            default:
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalender.getTimeInMillis(), pendingIntent);
                break;
        }
        Toast.makeText(EventInfoActivity.this, "Saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // bu iki fonksiyon spinnerın rengini ayarlamak için
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


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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
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

public class EventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static mySQLiteDBHandler dbHandler;
    private SQLiteDatabase sqLiteDatabase;
    private EditText eventNameEt, eventDescriptionEt,startDateEt, endDateEt,remindTimeEt,addressEt;
    private Spinner spinner;
    public int entryId;
    SharedPreferences sharedPreferences;
    private CheckBox ringCb,vibrateCb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

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
        /////
        dbHandler = MainActivity.getDbHandler();
        sqLiteDatabase = dbHandler.getWritableDatabase();

        Intent incomingIntent = getIntent();
        String id = incomingIntent.getStringExtra("id");
        String Day = incomingIntent.getStringExtra("Day");
        String Month = incomingIntent.getStringExtra("Month");
        String Year = incomingIntent.getStringExtra("Year");

        String query;
        query = "Select * from EventCalendar where id =" + "'" + id +"'"; // id'ye göre event seçiliyor ve değerler ui componentlara yerleştiriliyor.
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            cursor.moveToFirst();
            eventNameEt.setText(cursor.getString(4));
            eventDescriptionEt.setText(cursor.getString(5));
            startDateEt.setText(cursor.getString(1));
            endDateEt.setText(cursor.getString(2));
            remindTimeEt.setText(cursor.getString(3));
            addressEt.setText(cursor.getString(7));
            entryId = Integer.parseInt(cursor.getString(0));

            String spinnerText = cursor.getString(6);

            if (spinnerText.equals("Daily")) {
                spinner.setSelection(0);
            } else if (spinnerText.equals("Weekly")) {
                spinner.setSelection(1);
            } else if (spinnerText.equals("Monthly")) {
                spinner.setSelection(2);
            } else if (spinnerText.equals("Yearly")) {
                spinner.setSelection(3);
            } else {
                spinner.setSelection(4);
            }
            String vibrateText = cursor.getString(8);
            String ringText = cursor.getString(9);
            if (vibrateText.equals("True")){
                vibrateCb.setChecked(true);
            } else {
                vibrateCb.setChecked(false);
            }

            if (ringText.equals("True")){
                ringCb.setChecked(true);
            } else {
                ringCb.setChecked(false);
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            eventNameEt.setText("Not found.");
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
    private void showTimeDialog(final EditText remindTimeEt) { // saat için timepicker
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
        new TimePickerDialog(EventActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true).show();
    }

    private void showDateDialog(final EditText startDateEt) { // tarih için datepicker
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
        new DatePickerDialog(EventActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    public void updateDatabase(View View) { // tarih ve zaman kontrolünden sonra databasedeki veriyi güncelleyen fonksiyon

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
                    Toast.makeText(EventActivity.this, "Invalid time.", Toast.LENGTH_SHORT).show();
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
                    Intent incomingIntent = getIntent();
                    String id = incomingIntent.getStringExtra("id");
                    String buttonChoice = incomingIntent.getStringExtra("ButtonChoice");
                    String Day = incomingIntent.getStringExtra("Day");
                    String Month = incomingIntent.getStringExtra("Month");
                    String Year = incomingIntent.getStringExtra("Year");

                    dbHandler = MainActivity.getDbHandler();
                    sqLiteDatabase = dbHandler.getWritableDatabase();

                    sqLiteDatabase.update("EventCalendar",contentValues, "id =" + id, null);
                    updateAlarm(calSet,entryId);
                    Toast.makeText(EventActivity.this, "Updated!", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(this, ListActivity.class);

                    i.putExtra("Day", Day);
                    i.putExtra("Month", Month);
                    i.putExtra("Year", Year);
                    i.putExtra("Button", buttonChoice);
                    startActivity(i);
                }

            } else {
                Toast.makeText(EventActivity.this, "Invalid date", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void deleteFromDatabase(View View) { //databaseden eventi silen fonksiyon
        Intent incomingIntent = getIntent();
        String id = incomingIntent.getStringExtra("id");
        String buttonChoice = incomingIntent.getStringExtra("ButtonChoice");
        String Day = incomingIntent.getStringExtra("Day");
        String Month = incomingIntent.getStringExtra("Month");
        String Year = incomingIntent.getStringExtra("Year");

        dbHandler = MainActivity.getDbHandler();
        sqLiteDatabase = dbHandler.getWritableDatabase();
        sqLiteDatabase.delete("EventCalendar", "id="+ id, null);
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra("Day", Day);
        i.putExtra("Month", Month);
        i.putExtra("Year", Year);
        i.putExtra("Button", buttonChoice);
        deleteAlarm(entryId);
        Toast.makeText(EventActivity.this, "Deleted.", Toast.LENGTH_SHORT).show();
        startActivity(i);
    }

    public void updateAlarm(Calendar alarmCalender, int entryId) { //updateDatabase'den çağırılan bir fonksiyon, alarmı güncelliyor.
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

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), entryId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

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

    }

    public void showMap(View view){ // adresi googlemaps'te gösteren fonksiyon
        Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(addressEt.getText().toString()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void deleteAlarm(int entryId) { // deleteFromDatabase'den çağrılan fonksiyon, alarmı siliyor.
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.putExtra("EventName", eventNameEt.getText().toString());
        intent.putExtra("EventDescription", eventDescriptionEt.getText().toString());
        intent.putExtra("id", entryId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), entryId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    public void share(View view){ // Basit bir metin ile eventi özetleyerek paylaşabilen fonksiyon
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, eventNameEt.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hello! " + eventNameEt.getText().toString() +
                " starts at " + remindTimeEt.getText().toString() + " Address: " + addressEt.getText().toString());
        try {
            startActivity(Intent.createChooser(shareIntent, "Share!"));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(EventActivity.this, "There is no client", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // bu iki override eden fonksiyon spinnerın rengini ayarlamak için
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

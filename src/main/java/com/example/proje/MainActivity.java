package com.example.proje;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static mySQLiteDBHandler dbHandler;

    private EditText editText, etDay, etMonth, etYear;
    private CalendarView calendarView;
    private String selectedDay,selectedMonth,selectedYear,selectedDate;
    private SQLiteDatabase sqLiteDatabase;
    private Button listDayB, listMonthB, listYearB, addEventB, settingsB;
    private SharedPreferences sharedPreferences;

    public static mySQLiteDBHandler getDbHandler() {
        return dbHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        editText = findViewById(R.id.eventNameEt);
        etDay = findViewById(R.id.etDay);
        etMonth = findViewById(R.id.etMonth);
        etYear = findViewById(R.id.etYear);
        listDayB = findViewById(R.id.listDayB);
        listMonthB = findViewById(R.id.listMonthB);
        listYearB = findViewById(R.id.listWeekB);
        addEventB = findViewById(R.id.addEventB);
        settingsB = findViewById(R.id.settingsB);

        calendarView = findViewById(R.id.calendarView);

        //dark mode
        sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);
        if ( sharedPreferences.getBoolean("darkTheme", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //CalendarView'den date almak
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {

                selectedDay = Integer.toString(i2);
                selectedMonth =Integer.toString( + i1+1);
                selectedYear =Integer.toString(i);

                if( Integer.parseInt(selectedMonth) < 10) {
                    selectedMonth = "0" + selectedMonth;
                }
                if (Integer.parseInt(selectedDay) < 10){
                    selectedDay = "0" + selectedDay;
                }
                selectedDate = selectedYear +"-"+ selectedMonth +"-" + selectedDay ;

                etDay.setText(selectedDay);
                etMonth.setText(selectedMonth);
                etYear.setText(selectedYear);
            }
        });

        try{ //Database oluşturma
            dbHandler = new mySQLiteDBHandler(this, "CalendarDatabase", null, 1);
            sqLiteDatabase = dbHandler.getWritableDatabase();
            sqLiteDatabase.execSQL("CREATE TABLE EventCalendar(id INTEGER PRIMARY KEY AUTOINCREMENT,START_DATE DATE NOT NULL,END_DATE DATE NOT NULL," +
                    "Time TEXT NOT NULL, Event TEXT NOT NULL, Description TEXT, Remind_Again TEXT NOT NULL, Address TEXT, Vibrate TEXT NOT NULL, Ring TEXT NOT NULL)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void ListEvents(View view) { //Günlük,haftalık ve aylık gösterim
        Intent i = new Intent(this, ListActivity.class);
        i.putExtra("Date", selectedDate);
        i.putExtra("Day", selectedDay);
        i.putExtra("Month", selectedMonth);
        i.putExtra("Year", selectedYear);
        if( view == listDayB) {
            i.putExtra("Button", "day");
        } else if ( view == listMonthB) {
            i.putExtra("Button", "month");
        } else {
            i.putExtra("Button", "week");
        }
        startActivity(i);
    }

    public void goToActivityInfoPage(View view) { //event oluşturma
        Intent i = new Intent(this, EventInfoActivity.class);
        i.putExtra("Date", selectedDate);
        i.putExtra("Day", selectedDay);
        i.putExtra("Month", selectedMonth);
        i.putExtra("Year", selectedYear);
        startActivity(i);

    }

    private void createNotificationChannel() { // Notification channel oluşturma, 6 adet channel var, vibrate,sound ve sound2 için
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Uri ringtone2 = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.ringtone);

            NotificationChannel channel1 = new NotificationChannel("onlyvibrate", "Vibration Only", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This channel only vibrates");
            channel1.enableVibration(true);
            channel1.setSound(null,null);
            channel1.enableLights(true);
            notificationManager.createNotificationChannel(channel1);

            NotificationChannel channel2 = new NotificationChannel("vibrateandsound", "Vibration and Sound", NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("This channel vibrates and gives a sound");
            channel2.enableVibration(true);
            channel2.enableLights(true);
            notificationManager.createNotificationChannel(channel2);

            NotificationChannel channel3 = new NotificationChannel("onlysound", "Sound Only", NotificationManager.IMPORTANCE_HIGH);
            channel3.setDescription("This channel only gives a sound");
            channel3.enableVibration(false);
            channel3.enableLights(true);
            notificationManager.createNotificationChannel(channel3);

            NotificationChannel channel4 = new NotificationChannel("nothing", "No vibration no sound", NotificationManager.IMPORTANCE_HIGH);
            channel4.setDescription("This channel only gives notification");
            channel4.enableVibration(false);
            channel4.enableLights(true);
            channel4.setSound(null,null);
            notificationManager.createNotificationChannel(channel4);

            NotificationChannel channel5 = new NotificationChannel("ringtone2withvibrate", "Sound and vibrate with ringtone2", NotificationManager.IMPORTANCE_HIGH);
            channel5.setDescription("This channel vibrates and gives a sound with ringtone2");
            channel5.enableVibration(true);
            channel5.enableLights(true);
            channel5.setSound(ringtone2,null);
            notificationManager.createNotificationChannel(channel5);

            NotificationChannel channel6 = new NotificationChannel("ringtone2withoutvibrate", "Sound only with ringtone2", NotificationManager.IMPORTANCE_HIGH);
            channel6.setDescription("This gives a sound with ringtone2");
            channel6.enableVibration(true);
            channel6.enableLights(true);
            channel6.setSound(ringtone2,null);
            notificationManager.createNotificationChannel(channel6);
        }
    }

    public void goToSettings(View view){ //ayarlar activity
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

}

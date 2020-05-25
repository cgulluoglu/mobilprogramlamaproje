package com.example.proje;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ListView eventListView;
    private mySQLiteDBHandler dbHandler;
    private SQLiteDatabase sqLiteDatabase;
    private Button homeB;
    private TextView dateTw;


    @Override
    protected void onCreate(Bundle savedInstanceState) { //listview ile eventleri günlük haftalık ve aylık sıralayan fonksiyon
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        final String buttonChoice, Month, Day, Year;

        homeB = findViewById(R.id.homeB);

        Intent incomingIntent = getIntent();
        buttonChoice = incomingIntent.getStringExtra("Button");
        Month = incomingIntent.getStringExtra("Month");
        Day = incomingIntent.getStringExtra("Day");
        Year = incomingIntent.getStringExtra("Year");

        String Date = Year + "-" + Month + "-" + Day;

        eventListView = findViewById(R.id.eventListView);
        dateTw = findViewById(R.id.dateTw);
        dbHandler = MainActivity.getDbHandler();
        sqLiteDatabase = dbHandler.getReadableDatabase();
        final ArrayList<String> arrayList = new ArrayList<>();
        final ArrayList<String> arrayListId = new ArrayList<>();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList){ //bu fonksiyon dark theme için listitemların rengini
            @Override //beyaza çeviriyor.
            public View getView(int position, View convertView, ViewGroup parent){
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                SharedPreferences sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);
                if ( sharedPreferences.getBoolean("darkTheme", false)) {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };

        String query;

        if( buttonChoice.equals("day")) { // girilen tarihle aynı günde olan entryleri çeken query
            query = "Select Event,id from EventCalendar where strftime('%d', start_date) =" + "'"+ Day + "'" + " and strftime('%m', start_date) =" +
                    "'"+ Month + "'" + " and strftime('%Y', start_date)=" + "'"+ Year + "'" + " order by date(start_date) desc";
            dateTw.setText("Daily List");
        } else if (buttonChoice.equals("month")) { // girilen tarihle aynı ayda olan entryleri çeken query
            query = "Select Event,id from EventCalendar where strftime('%m', start_date) =" + "'"+ Month + "'" +
                    " and strftime('%Y', start_date)=" + "'"+ Year + "'" + " order by date(start_date) desc";
            dateTw.setText("Monthly List");
        } else { // girilen tarihle aynı haftada olan entryleri çeken query
            query = "Select Event,id from EventCalendar where strftime('%W', start_date) =" + "strftime('%W'," + "'" + Date + "')" + " order by date(start_date) desc";
            dateTw.setText("Weekly List");
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if( cursor.getCount() == 0) { // eğer hiç entry yoksa
            Toast.makeText(ListActivity.this, "No data to show", Toast.LENGTH_SHORT).show();
        } else {
            while( cursor.moveToNext()) {
                arrayList.add(cursor.getString(0)); // bu ismi tutan liste
                arrayListId.add(cursor.getString(1 )); // bu da idyi tutan liste
            }
        }
        cursor.close();

        eventListView.setAdapter(arrayAdapter); // isimleri belirleyip setAdapter ile listView'e gönderdik.
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // listView'in tıklanabilir olmasını sağlayan fonksiyon
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ListActivity.this, EventActivity.class);
                intent.putExtra("id", arrayListId.get(i));
                intent.putExtra("ButtonChoice", buttonChoice);
                intent.putExtra("Day", Day);
                intent.putExtra("Month", Month);
                intent.putExtra("Year", Year);
                startActivity(intent);
            }
        });
    }

    public void goToMainActivity(View view) { //ana menüye dönüş
        Intent i = new Intent(ListActivity.this, MainActivity.class);
        startActivity(i);
    }

}

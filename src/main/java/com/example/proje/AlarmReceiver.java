package com.example.proje;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class AlarmReceiver extends BroadcastReceiver {
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPreferences = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        String ringtoneChoice = sharedPreferences.getString("ringtone_choice", "Default"); // sharedpreferences'dan seçilmiş ringtone alınıyor

        Intent intent2 = new Intent(context, MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent2, 0);
        NotificationCompat.Builder builder; // kullanıcının titreşim ve ses isteklerine göre notification channellar seçiliyor.
        if(intent.getStringExtra("Vibrate").equals("True") && intent.getStringExtra("Ring").equals("True") && ringtoneChoice.equals("Default")){
            builder = new NotificationCompat.Builder(context, "vibrateandsound");

        } else if(intent.getStringExtra("Vibrate").equals("True") && intent.getStringExtra("Ring").equals("True") && ringtoneChoice.equals("Drum")){
            builder = new NotificationCompat.Builder(context, "ringtone2withvibrate");

        } else if(intent.getStringExtra("Vibrate").equals("True") && intent.getStringExtra("Ring").equals("False")){
             builder = new NotificationCompat.Builder(context, "onlyvibrate");

        } else if(intent.getStringExtra("Vibrate").equals("False") && intent.getStringExtra("Ring").equals("True") && ringtoneChoice.equals("Default")) {
             builder = new NotificationCompat.Builder(context, "onlysound");

        } else if(intent.getStringExtra("Vibrate").equals("False") && intent.getStringExtra("Ring").equals("True") && ringtoneChoice.equals("Drum")) {
            builder = new NotificationCompat.Builder(context, "ringtone2withoutvibrate");

        } else {
             builder = new NotificationCompat.Builder(context, "nothing");
        }
        builder.setSmallIcon(R.drawable.notification)
                .setContentTitle(intent.getStringExtra("EventName"))
                .setContentText(intent.getStringExtra("EventDescription"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(intent.getIntExtra("id",0), builder.build()); // ve notification, databasedeki id'ye göre build ediliyor.


    }
}
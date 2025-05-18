package com.example.project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.sql.Timestamp;
import com.mobile.app.R;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Helpers", "Helpers", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Intent intent1=new Intent(context,Homepage.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,reqID(System.currentTimeMillis()),intent1, PendingIntent.FLAG_IMMUTABLE);
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Helpers")
                .setContentTitle(intent.getStringExtra("name"))
                .setContentText("time is up")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_stat_name);

        manager.notify(reqID(System.currentTimeMillis()), builder.build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private int reqID(long time) {
        if((int)time<1)
            return (int) time*-1;
        return (int)time;
    }
}

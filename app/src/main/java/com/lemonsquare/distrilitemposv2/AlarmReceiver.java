package com.lemonsquare.distrilitemposv2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static String CHANNEL_ID = ""; //"channel1"
    String strChannel;

    @Override
    public void onReceive(Context arg0, Intent arg1) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            CHANNEL_ID = arg1.getStringExtra("Activity");

            if (CHANNEL_ID.equals("channel1")){
                strChannel = "Channel 1";
            }else{
                strChannel = "Channel 2";
            }


            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,strChannel,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("This is " + strChannel);

            if (CHANNEL_ID.equals("channel1")){
                NotificationManager manager = arg0.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);

                Intent i = new Intent(arg0, SalesDailyReportActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(arg0,
                        0 /* Request code */,
                        i,
                        PendingIntent.FLAG_ONE_SHOT);


                NotificationCompat.Builder notification = new NotificationCompat.Builder(arg0, CHANNEL_ID)
                        .setContentTitle("Reminder")
                        .setContentText("Please submit your daily sales report")
                        .setSmallIcon(R.mipmap.dpos)
                        .setContentIntent(pendingIntent);

                manager.notify(1, notification.build());
            }else{
                NotificationManager manager = arg0.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);

                Intent i = new Intent(arg0, DailyAttendanceActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(arg0,
                        0 /* Request code */,
                        i,
                        PendingIntent.FLAG_ONE_SHOT);


                NotificationCompat.Builder notification = new NotificationCompat.Builder(arg0, CHANNEL_ID)
                        .setContentTitle("Reminder")
                        .setContentText("Please click time out")
                        .setSmallIcon(R.mipmap.dpos)
                        .setContentIntent(pendingIntent);

                manager.notify(1, notification.build());
            }

        }

    }

}
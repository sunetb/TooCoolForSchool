package dk.stbn.alarm.lyttere;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.ArrayList;

import dk.stbn.alarm.R;
import dk.stbn.alarm.aktivitetFragment.Forside_akt;
import dk.stbn.alarm.data.AlarmLogik;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.IO;

/**
 * Created by sune on 6/6/16.
 */
public class Alarm_Lytter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        ArrayList<Integer> gamle = (ArrayList<Integer>) IO.læsObj("gamle", context);
        Bundle b = intent.getExtras();

        String overskrift = b.getString("overskrift");
        String id = b.getString("tekstId");
        int id_int = b.getInt("id_int");

        p("Alarm_Lytter.onRecieve() modtog "+overskrift);
        //if (A.debugging) Toast.makeText(context, "Alarm modtaget"+id, Toast.LENGTH_LONG).show();

        if (gamle != null && gamle.contains(id))
            p("Notifikation for "+id+" findes har været vist");
        else
            bygNotifikation(context, overskrift, id, id_int);

        Util.baglog("Alarm_Lytter.onRecieve(): "+ id + " " +overskrift, context);
        AlarmLogik.getInstance().opdaterKalender(context, "Alarm_Lytter.onrecieve");


    }


    public static void bygNotifikation (Context context, String overskrift, String id, int id_int) {
        //opdateret i henhold til https://stackoverflow.com/questions/44489657/android-o-reporting-notification-not-posted-to-channel-but-it-is

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = context.getString(R.string.app_name);
        String description = "Too Cool for School";
        final String NOTIFICATION_CHANNEL_ID = "4565";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }


         p("bygnotifikation modtog: "+overskrift+ " IDStreng: "+id + " id_int: "+id_int);
        Util.baglog("Notifikation bygget: "+overskrift+ " IDStreng: "+id + " id_int: "+id_int, context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        mBuilder
                .setSmallIcon(R.drawable.cool_nobkgr_71x71)
                .setContentTitle("Too Cool for School")
                .setContentText(overskrift)
                .setAutoCancel(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true);
        //ingen effekt: .setDeleteIntent(PendingIntent.getActivity(context, 0, sletteIntent, 0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setCategory(Notification.CATEGORY_ALARM);
        }


        Intent resultIntent = new Intent(context, Forside_akt.class);
        resultIntent.putExtra("overskrift", overskrift);
        resultIntent.putExtra("tekstId", id);
        resultIntent.putExtra("id_int", id_int);
        resultIntent.putExtra("fraAlarm", true);
        resultIntent.setAction(id); //-- lille hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(Forside.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification n = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            n = mBuilder
                    .build();
        }
        else return;

        //-- Hvis brugeren sletter notifikationen ved swipe eller tømmer alle notifikationer
        Intent sletteIntent = new Intent(context, SletNotifikation_Lytter.class);
        sletteIntent.putExtra("tekstId", id)
                .putExtra("id_int", id_int);
        sletteIntent.setAction(id);

        n.deleteIntent = PendingIntent.getBroadcast(context, 0, sletteIntent, 0);
        mNotificationManager.notify(id_int, n);
    }


    static void p (Object o){
        Util.p("AlarmLytter."+o);
    }

}

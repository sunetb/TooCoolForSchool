package dk.stbn.alarm.lyttere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;

import dk.stbn.alarm.data.AlarmLogik;
import dk.stbn.alarm.data.Tekstlogik;
import dk.stbn.alarm.data.Tilstand;
import dk.stbn.alarm.data.Util;

/**
 * Created by sune on 6/6/16.
 */
public class Alarm_Lytter extends BroadcastReceiver {


    AlarmLogik al;
    Tilstand tilstand;
    Tekstlogik t;

    @Override
    public void onReceive(Context context, Intent intent) {
        al = AlarmLogik.getInstance();
        t = Tekstlogik.getInstance(context);

        tilstand = Tilstand.getInstance(context);
        String besked = intent.getExtras().getString("tag", "Fejl");
        int id = intent.getExtras().getInt("id", 0);

        p("onRecieve() kaldt. "+ besked);
        Util.baglog("Alarm_Lytter onRecieve kaldt med besked: "+besked + " id: "+id + " tidspunkt: "+new DateTime(), context);

        p("Sætter alarm til nyt tjek imorgen");
        al.sætAlarm(context, tilstand.masterDato.plusDays(1).withTime(1,0,0,0), "loop igang");
        t.tjekForNoti(tilstand.masterDato);
    }


    void p(Object o) {
        String kl = this.getClass().getSimpleName() + ".";
        Util.p(kl + o);
    }

}

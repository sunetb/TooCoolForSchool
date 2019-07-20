package dk.stbn.alarm.lyttere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import dk.stbn.alarm.data.AlarmLogik;
import dk.stbn.alarm.data.Tekstlogik;
import dk.stbn.alarm.data.Tilstand;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.K;

/**
 * Created by sune on 6/6/16.
 * Alarmer slettes når telefonen slukkes. Derfor skal de sættes igen efter boot. Flaget boot sættes i tilstand, så appen ved at der ikke er nogen alarmer lagret
 */
public class Boot_Lytter extends BroadcastReceiver  {


       @Override
        public void onReceive(Context context, Intent intent) {



            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {
                Tilstand tilstand = Tilstand.getInstance(context);
                tilstand.boot = true;
                Tekstlogik t = Tekstlogik.getInstance(context);
                t.tjekForNoti(tilstand.masterDato);
                Util.baglog("Boot_Lytter.onRecieve(): Modenhed = "+ tilstand.modenhed, context);
            }
        }



    void p(Object o) {
        String kl = this.getClass().getSimpleName() + ".";
        Util.p(kl + o);
    }


}

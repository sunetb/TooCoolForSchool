package dk.stbn.alarm.lyttere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import dk.stbn.alarm.data.AlarmLogik;
import dk.stbn.alarm.data.Tilstand;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.K;

/**
 * Created by sune on 6/6/16.
 * Alarmer slettes når telefonen slukkes. Derfor skal de sættes flaget boot i tilstand, så appen ved at der ikke er nogen alarmer lagret
 */
public class Boot_Lytter extends BroadcastReceiver  {


       @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {
               //if (A.debugging) Toast.makeText(context, "hændelse boot modtaget af tooCoolToScool2", Toast.LENGTH_LONG).show();

                Tilstand tilstand = Tilstand.getInstance(context);
                tilstand.boot = true;


                int modenhed = tilstand.pref.getInt("modenhed", -1);

                if (modenhed == K.MODENHED_MODEN) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            //AlarmLogik.getInstance().opdaterKalender(context, "boot-lytter");
                        }
                    });
/*
*/
                }
               // Util.baglog("Boot_Lytter.onRecieve(): Modenhed = "+ pref.getInt("modenhed", -1), context);
            }
        }



    void p (Object o){
        Util.p("BootModtag."+o);
    }


}

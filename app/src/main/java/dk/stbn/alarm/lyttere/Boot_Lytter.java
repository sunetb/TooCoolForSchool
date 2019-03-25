package dk.stbn.alarm.lyttere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.K;

/**
 * Created by sune on 6/6/16.
 * Opdaterer alarmer. Alarmer slettes når telefonen slukkes. Derfor skal de sættes igen ved opstart
 */
public class Boot_Lytter extends BroadcastReceiver  {
        Context c;
        SharedPreferences pref;

       @Override
        public void onReceive(Context context, Intent intent) {

            c = context;
            pref = PreferenceManager.getDefaultSharedPreferences(c);

            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {


                //if (A.debugging) Toast.makeText(context, "hændelse boot modtaget af tooCoolToScool2", Toast.LENGTH_LONG).show();

                int modenhed = pref.getInt("modenhed", -1);

                if (modenhed == K.MODENHED_MODEN) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Util.opdaterKalender(c, "boot-lytter");
                        }
                    });
/*
                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {



                            return null;
                        }
                    }.execute();
*/
                }
                Util.baglog("Boot_Lytter.onRecieve(): Modenhed = "+ pref.getInt("modenhed", -1), context);
            }
        }



    void p (Object o){
        Util.p("BootModtag."+o);
    }


}

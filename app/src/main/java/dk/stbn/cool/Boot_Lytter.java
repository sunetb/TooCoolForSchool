package dk.stbn.cool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

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

                if (modenhed == 3) {
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

            }
        }



    void p (Object o){
        Util.p("BootModtag."+o);
    }


}

package com.example.cool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by sune on 6/6/16.
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


                Toast.makeText(context, "hændelse boot modtaget af tooCoolToScool2", Toast.LENGTH_LONG).show();
				new AsyncTask() {

                    ;
                    @Override
                    protected Object doInBackground(Object[] params) {
                        int modenhed = pref.getInt("modenhed", -1);

                        if (modenhed == 3) Util.opdaterKalender(c, "boot-lytter");

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {

                        super.onPostExecute(o);
                    }
                }.execute();



            }
        }



    void p (Object o){
        Util.p("BootModtag."+o);
    }


}

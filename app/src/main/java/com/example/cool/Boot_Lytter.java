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
				//TODO: find og sæt tidligere alarmer
				//context.startActivity(new Intent(context, MainActivity.class));
				//Notifikation.bygNotifikation(context,"fra boot","fra boot", "fra boot");
                new AsyncTask() {

                    ;
                    @Override
                    protected Object doInBackground(Object[] params) {
                        Util.opdaterKalender(c);


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

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


                Toast.makeText(context, "hændelse boot modtaget af notitest", Toast.LENGTH_LONG).show();
				//TODO: find og sæt tidligere alarmer
				//context.startActivity(new Intent(context, MainActivity.class));
				//Notifikation.bygNotifikation(context,"fra boot","fra boot", "fra boot");
                new AsyncTask() {

                    ;
                    @Override
                    protected Object doInBackground(Object[] params) {
                        opdaterkalenderBoot();


                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                    }
                }.execute();



            }
        }

    private void opdaterkalenderBoot(){
        p("opdeterKalenderBoot() kaldt");
        //Set<String> gamle = pref.getStringSet("gamle", new HashSet<String>());
        ArrayList<String> gamle = (ArrayList<String>) IO.læsObj("gamle", c);

        System.out.println("BootReciever tjek sættet:");
        for (String s : gamle) System.out.println(s);

        int iLængde = pref.getInt("teksterLængde", 0);
        int mLængde = pref.getInt("mteksterLængde", 0);

        //Evt if ilængde == 0...else
        for (int i = 0; i <iLængde; i++) {
            String id = pref.getString("i"+i, "fejl");
            if (id.equals("fejl")) p("Fejl ved indlæsning af itekst nr "+i);
            else if (!gamle.contains(id)) {
                Tekst t = (Tekst) IO.læsObj(id, c);
                p("boot kalender init "+id);
                Util.startAlarm(c,t);
            }
            else p("Noti "+id+" er allerede brugt");
        }

        //Evt if mlængde == 0... else
        for (int j = 0; j <mLængde; j++) {
            String id = pref.getString("m"+j, "fejl");

            if (id.equals("fejl")) p("Fejl ved indlæsning af mtekst nr "+j);

            if (!gamle.contains(id)) {
                Tekst t = (Tekst) IO.læsObj(id, c);
                Util.startAlarm(c,t);
            }
            else p("Noti "+id+" er allerede brugt");
        }
    }


    void p (Object o){
        Util.p("BoorModtag."+o);
    }


}

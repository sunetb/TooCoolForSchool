package dk.stbn.alarm.lyttere;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.joda.time.DateTime;

import dk.stbn.alarm.R;
import dk.stbn.alarm.aktivitetFragment.Forside_akt;
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

        //Kun til test:
       // bygNotifikation(context, besked, "Test", id);

        al.sætAlarm(context, tilstand.masterDato.plusDays(1).withTime(1,0,0,0), "loop igang");

        //Flyt til anden metode:
/*
        ArrayList<Integer> gamle = (ArrayList<Integer>) IO.læsObj("gamle", context);
        if (gamle != null && gamle.contains(id))
            p("Notifikation for "+id+" har allerede været vist");
        else
            bygNotifikation(context, "TEST", "Test", id_int);

        Util.baglog("Alarm_Lytter.onRecieve(): "+ id + " " +overskrift, context);
        //al.opdaterKalender(context, "Alarm_Lytter.onrecieve");
*/
    }



    /*
    void tjekNyTekst(Context c){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);

        ArrayList<Tekst> itekster = (ArrayList<Tekst>) IO.læsObj(K.ITEKSTER, c.getApplicationContext());
        ArrayList<Tekst> mtekster = (ArrayList<Tekst>) IO.læsObj(K.MTEKSTER, c.getApplicationContext());




        int modenhed = pref.getInt("modenhed", -1);

        ArrayList<Tekst> tempSynlige = new ArrayList<>();

        if (modenhed == K.SOMMERFERIE) {
            p("sommerferie!!!");

            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_1, c.getApplicationContext()));
            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_2, c.getApplicationContext()));
            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_3, c.getApplicationContext()));

        }else if (modenhed == K.MODENHED_TREDJE_DAG) {
            p("Dag 3 ");

            Tekst oTekst1 = (Tekst) IO.læsObj(K.OTEKST_1, getApplicationContext());
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, getApplicationContext());
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);

            ArrayList<Tekst> tekster = findItekster();
            if (tekster.size() > 0)
                tempSynlige.add(tekster.get(0));

        } else if (modenhed == K.MODENHED_FJERDE_DAG) {
            p("Dag 4 ");
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, getApplicationContext());


            tempSynlige.add(oTekst2);
            ArrayList<Tekst> tekster = findItekster();
            //Tag kun de første to I-tekster, så der vises tre i alt
            if (tekster.size() > 0) tempSynlige.add(tekster.get(0));
            if (tekster.size() > 1) tempSynlige.add(tekster.get(1));

            //sørger for at der altid er tre tekster, også lige efter sommerferien
            if (tempSynlige.size() == 2)
                tempSynlige.add(0, (Tekst)IO.læsObj(K.OTEKST_1, getApplicationContext()));

        } else if (modenhed == K.MODENHED_MODEN) {
            p("Dag 5: MODEN ");

            ArrayList<Tekst> itekster = findItekster();

            //Særtilfælde: er appen ung og har kun én eller to I-tekster?
            if (itekster.size() == 1) {
                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_1, getApplicationContext()));
                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_2, getApplicationContext()));
            }
            if (itekster.size() == 2) {

                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_2, getApplicationContext()));
            }
            tempSynlige.addAll(itekster);
            ArrayList<Tekst> mtekster = findMtekster();
            tempSynlige.addAll(mtekster);

        }

        p("Tjekker om de cachede tekster skal erstattes..");

        //Der skal skiftes hvis gemt liste og ny liste er forskellig længde
        boolean skift = synligeTekster.size() != tempSynlige.size();

        //Der skal skiftes hvis appen ikke er moden
        if (tilstand.modenhed != K.MODENHED_MODEN) skift = true;

        //Er appen moden og har listerne samme længde, må vi tjekke indholdet af listerne
        if (!skift) {
            boolean forskellige = false;

            for (int i = 0; i < synligeTekster.size(); i++) {
                Tekst synlig = synligeTekster.get(i);
                Tekst temp = tempSynlige.get(i);
                if (synlig.id_int != temp.id_int) {
                    forskellige = true;
                    break;
                }
            }


            skift = forskellige;
        }

        if (skift) {
            p("JA. Der er  et nyt udvalg af tekster");
            synligeTekster = tempSynlige;
            pref.edit().putInt("senesteposition", -1).commit(); //Sætter ViewPagerens position til nyeste element
            lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "udvælgTekster(), der var et nyt udvalg");
            gemSynligeTekster();
        } else
            p("NEJ. Vi bruger de cachede tekster");

        if (modenhed < K.MODENHED_TREDJE_DAG){
            //sørg for at der ikke vises notifikationer i starten
            for (Tekst t : synligeTekster)
                IO.føjTilGamle(t.id_int, getApplicationContext());
        }

        p("udvælgTekster() færdig");
    }
    */


    void startNotifikationskanal(){

    }


    //Kun static under test!!


    void p(Object o) {
        String kl = this.getClass().getSimpleName() + ".";
        Util.p(kl + o);
    }

}

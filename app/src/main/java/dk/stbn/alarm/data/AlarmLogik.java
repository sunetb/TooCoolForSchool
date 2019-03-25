package dk.stbn.alarm.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;

import java.util.ArrayList;

import dk.stbn.alarm.Tekst;
import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.Tid;
import dk.stbn.alarm.lyttere.Alarm_Lytter;
import dk.stbn.alarm.lyttere.Boot_Lytter;

public class AlarmLogik {

    private static AlarmLogik al;

    AlarmLogik(){

    }

    public static AlarmLogik getInstance(){

        if (al == null) al = new AlarmLogik();
        return al;
    }

    public void notiBrugt(Context c, Intent intent){
        Util.p("Util.notiBrugt kaldt");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

        //tjek om første opstart. Hmm ER det nu også nødvendigt?
        boolean førsteOpstart = sp.getBoolean("førstegang", true);

        if (førsteOpstart) IO.gemObj(new ArrayList<Integer>(), "gamle", c);

        sp.edit().putBoolean("førstegang", false).apply();
        String id = "";
        id = intent.getExtras().getString("tekstId");
        int id_int = intent.getExtras().getInt("id_int", 0);

        Util.p("Util.notiBrugt modtog: id: "+id + "id_int: "+id_int);

        IO.føjTilGamle(id_int, c);
        Util.p("Util.notiBrugt tjek sættet:");

        PendingIntent i = PendingIntent.getBroadcast(c, id_int, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alm = A.alm;
        if (alm == null) alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        else Util.p("Util.notiBrugt(): alarmManager eksisterer");
        i.cancel();
        alm.cancel(i);
    }

    void startAlarm (Context c, Tekst t) {
        Util.p("Util.startAlarm() modtog "+t.overskrift);

        ComponentName receiver = new ComponentName(c, Boot_Lytter.class);
        PackageManager pm = c.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        AlarmManager alarmMgr = A.alm;
        PendingIntent alarmIntent;

        if (alarmMgr == null)  alarmMgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        else Util.p("Util.startAlarm()  alarmManager eksisterer");
        Intent intent = new Intent(c, Alarm_Lytter.class);

        intent.putExtra("id_int", t.id_int);
        intent.putExtra("tekstId", t.id);
        intent.putExtra("overskrift", t.overskrift);
        String action = ""+t.id_int;
        if (t.kategori.equals("mgentag")) action+="gentag"; //--M-tekster har TO notifikationer: en syv dage før og en på dagen
        intent.setAction(action); //Fjollet hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig
        alarmIntent = PendingIntent.getBroadcast(c, t.id_int, intent,  PendingIntent.FLAG_CANCEL_CURRENT);

        Util.p("Util.startAlarm()      Dato: "+t.dato.toString());
       // p("Util.startAlarm() Dags Dato: "+A.tilstand.masterDato);

        alarmMgr.set(AlarmManager.RTC, t.dato.getMillis(), alarmIntent);
    }


    public void opdaterKalender(Context c, String kaldtfra){
        Util.p("opdaterKalender() kaldt fra "+kaldtfra);
        ArrayList<Integer> gamle = (ArrayList<Integer>) IO.læsObj("gamle", c);
        if (gamle == null) gamle  = new ArrayList<>();
        System.out.println("opdaterKalender() tjek sættet:");
        //for (Integer s : gamle) System.out.println(s);

        ArrayList<Integer> datoliste = (ArrayList<Integer>) IO.læsObj("datoliste", c);

        if (datoliste == null) return;
        //Hvis kaldet sker fra onrecieve i alarmlytteren, er det en ekstra gang for en sikkerheds skyld. Vi fjerner det nyeste element for at undgå loop
        boolean forskyd = kaldtfra.equals("Alarm_Lytter.onrecieve");
        if (forskyd) datoliste.remove(datoliste.size()-1);

        int antalAlarmerAffyret = 0;
        for (int i = 0 ; i < datoliste.size(); i++){
            if (antalAlarmerAffyret > 6) break;

            if (!gamle.contains((datoliste.get(i)))) {
                Tekst t = (Tekst) IO.læsObj(""+datoliste.get(i), c);
                if (t==null) {
                    Util.p("FEJL: Teksten var null, UTIL lin ca 118");
                    return;
                }
                if (t.id_int <300000000){  //Hvis I-tekst

                    if (Tid.fortid(t.dato))
                        gamle.add(t.id_int);

                    else{
                        startAlarm(c,t);
                        antalAlarmerAffyret++;
                    }
                }
                else{ //-- Hvis m-tekst
                    if(Tid.fortid(t.dato))
                        gamle.add(t.id_int);
                    else {
                        //-- M-tekster har både notifikation på dagen ...
                        startAlarm(c,t);
                        antalAlarmerAffyret++;

                        //-- ...og syv dage før
                        Tekst temp = t;
                        temp.dato = temp.dato.minusDays(7);
                        temp.kategori="mgentag";
                        startAlarm(c,temp);

                        antalAlarmerAffyret++;


                    }
                }

            }
            else Util.p("Noti "+datoliste.get(i)+" er allerede brugt");

        }

        IO.gemObj(gamle, "gamle", c);
    }

    void rensUdIAlarmer(final Context c){
        Util.p("Util.rensUdIAlarmer kaldt");
        final Context cx = c;

        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects) {
                ArrayList<Tekst> tempi = (ArrayList<Tekst>) IO.læsObj("itekster", c);
                ArrayList<Tekst> tempm = (ArrayList<Tekst>) IO.læsObj( "mtekster", c);

                for (Tekst t: tempi) sletAlarm(cx, t);
                for (Tekst t: tempm) sletAlarm(cx, t);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                opdaterKalender(c, "rensUdIAlarmer");
            }
        }.execute();






    }

    void sletAlarm (Context c, Tekst t){
        Util.p("Util.sletAlarm kaldt med tekst: "+t.toString(0));

        Intent intent = new Intent(c, Alarm_Lytter.class);

        intent.putExtra("id_int", t.id_int);
        intent.putExtra("tekstId", t.id);
        intent.putExtra("overskrift", t.overskrift);
        String action = ""+t.id_int;
        if (t.kategori.equals("mgentag")) action+="gentag"; //--M-tekster har TO notifikationer: en syv dage før og en på dagen
        intent.setAction(action); //Fjollet hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig

        PendingIntent i = PendingIntent.getBroadcast(c, t.id_int, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alm = A.alm;
        if (alm == null) alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        i.cancel();
        alm.cancel(i);

    }


    //Flyttes til tekstlogik???
    boolean visMtekst(DateTime mTid, DateTime masterDato){
        String logbesked = "Util.visMtekst() "+ mTid.getDayOfMonth()+ "/"+mTid.getMonthOfYear();
        //-- Eks: 11 september     ///Vises                    5, 6, 7, 8, 9, 10, 11
                                   ///Vises ikke: 1, 2, 3. 4.                         12, 13, 14, sept

        //-- Tjek om  m-dato er idag
        if (Tid.erSammeDato(mTid, masterDato)) return true;


        //-- Tjek om idag er 12 sept eller efter.
        if (Tid.før(mTid,masterDato)) return false;

        //-- Tjek om idag er 4. sept eller tidligere
        DateTime syvFør = mTid.minusDays(7);
        Util.p(logbesked + " dato var mindre end en uge gammel. Skal den vises? "+!Tid.efter(syvFør,masterDato));

        return !Tid.efter(syvFør,masterDato);
    }
}

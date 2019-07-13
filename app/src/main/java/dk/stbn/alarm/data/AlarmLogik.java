package dk.stbn.alarm.data;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.joda.time.DateTime;

import java.util.ArrayList;

import dk.stbn.alarm.R;
import dk.stbn.alarm.aktivitetFragment.Forside_akt;
import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.lyttere.Alarm_Lytter;
import dk.stbn.alarm.lyttere.Boot_Lytter;
import dk.stbn.alarm.lyttere.SletNotifikation_Lytter;

public class AlarmLogik {

    private static AlarmLogik al;
    private AlarmManager alm;
    final String NOTIFICATION_CHANNEL_ID = "4565";

    AlarmLogik(){

    }

    public static AlarmLogik getInstance(){

        if (al == null) al = new AlarmLogik();
        return al;
    }

    public void sætAlarm(Context c, DateTime tidspunkt, String evtBesked){
        p("sætAlarm() modtog dato: "+tidspunkt);
        p("nu er "+new DateTime());

        Util.baglog("sætAlarm kaldt med "+ evtBesked + " Nu: "+new DateTime(), c);
        Util.baglog("Alarmtid: "+tidspunkt, c);


        alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        ComponentName receiver = new ComponentName(c, Boot_Lytter.class);
        PackageManager pm = c.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        p("millis: "+ tidspunkt.getMillis());
        int id = lavIntId(tidspunkt.getMillis());
        p("id: "+id);
        String action = ""+tidspunkt.toLocalDate();
        p("sætAlarm() dato: "+action);

        Intent intentSomSendesMedPendingIntentet = new Intent(c, Alarm_Lytter.class);

        intentSomSendesMedPendingIntentet
                .putExtra("tag", evtBesked)
                .putExtra("id", id)
                .setAction(action); //Fjollet hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig

        PendingIntent alarmIntent = PendingIntent.getBroadcast(c, 0, intentSomSendesMedPendingIntentet,  PendingIntent.FLAG_CANCEL_CURRENT);
        alm.set(AlarmManager.RTC, tidspunkt.getMillis(), alarmIntent);
    }

    void startAlarmLoop(Context c) {
        p("startAlarmloop kaldt");
        Tilstand t = Tilstand.getInstance(c);

        if (t.femteDagFørsteGang || t.boot || t.modenhed == K.SOMMERFERIE) {

            //loopet skal kun startes én gang i sommerferien
            //TODO: OBS visker kun én gang!!! Fix det
            boolean alleredeStartet = t.pref.getBoolean("alarmloop allerede startet", false);
            t.pref.edit().putBoolean("alarmloop allerede startet", true);
            if (t.modenhed == K.SOMMERFERIE && alleredeStartet)
                return;

            p("startAlarmloop sætter alarm til: "+t.masterDato.plusDays(1).withTime(1,0,0,0));
            sætAlarm(c, t.masterDato.plusDays(1).withTime(1,0,0,0), "loop");

        }
    }

    public void notiBrugt(Context c, Intent intent){
        p("notiBrugt kaldt");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        //tjek om første opstart. Hmm ER det nu også nødvendigt?
        boolean førsteOpstart = sp.getBoolean("førstegang", true);

        if (førsteOpstart) IO.gemObj(new ArrayList<Integer>(), "gamle", c);

        sp.edit().putBoolean("førstegang", false).apply();
        String id = "";
        id = intent.getExtras().getString("tekstId");
        int id_int = intent.getExtras().getInt("id_int", 0);

        p("notiBrugt modtog: id: "+id + "id_int: "+id_int);

        IO.føjTilGamle(id_int, c);
        p("notiBrugt tjek sættet:");

        PendingIntent i = PendingIntent.getBroadcast(c, id_int, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        i.cancel();
        alm.cancel(i);
    }
/*
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

    void rensUdIAlarmer(Context c){
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

 */

    void sletAlarm (Context c, Tekst t){
        p("sletAlarm kaldt med tekst: "+t.toString(0));
        alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(c, Alarm_Lytter.class);

        intent.putExtra("id_int", t.id_int);
        intent.putExtra("tekstId", t.id);
        intent.putExtra("overskrift", t.overskrift);
        String action = ""+t.id_int;
        if (t.kategori.equals("mgentag")) action+="gentag"; //--M-tekster har TO notifikationer: en syv dage før og en på dagen
        intent.setAction(action); //Fjollet hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig

        PendingIntent i = PendingIntent.getBroadcast(c, t.id_int, intent, PendingIntent.FLAG_CANCEL_CURRENT);


        i.cancel();
        alm.cancel(i);

    }

    public void bygNotifikation (Context context, String overskrift, String id, int id_int) {
        //opdateret i henhold til https://stackoverflow.com/questions/44489657/android-o-reporting-notification-not-posted-to-channel-but-it-is


        p("bygnotifikation modtog: "+overskrift+ " IDStreng: "+id + " id_int: "+id_int);
        Util.baglog("Notifikation bygget: "+overskrift+ " IDStreng: "+id + " id_int: "+id_int, context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        mBuilder
                .setSmallIcon(R.drawable.cool_nobkgr_71x71)
                .setContentTitle("Too Cool for School")
                .setContentText(overskrift)
                .setAutoCancel(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true);
        //ingen effekt: .setDeleteIntent(PendingIntent.getActivity(context, 0, sletteIntent, 0))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setCategory(Notification.CATEGORY_ALARM);
        }


        Intent resultIntent = new Intent(context, Forside_akt.class);
        resultIntent.putExtra("overskrift", overskrift);
        resultIntent.putExtra("tekstId", id);
        resultIntent.putExtra("id_int", id_int);
        resultIntent.putExtra("fraAlarm", true);
        resultIntent.setAction(id); //-- lille hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(Forside.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        //NotificationManager mNotificationManager =
        //      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        Notification n = mBuilder.build();

        CharSequence name = context.getString(R.string.app_name);
        String description = "Too Cool for School";
        int importance = NotificationManager.IMPORTANCE_HIGH;///.IMPORTANCE_DEFAULT;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance));
        }

        //-- Hvis brugeren sletter notifikationen ved swipe eller tømmer alle notifikationer
        Intent sletteIntent = new Intent(context, SletNotifikation_Lytter.class);
        sletteIntent.putExtra("tekstId", id)
                .putExtra("id_int", id_int);
        sletteIntent.setAction(id);
        n.deleteIntent = PendingIntent.getBroadcast(context, 0, sletteIntent, 0);

        notificationManager.notify(id_int, n);
    }




    private int lavIntId (long l) {

        while(l >= Integer.MAX_VALUE) l= l/1000;

        return (int) l;

    }
    void p (Object o){
        Util.p("AlarmLogik."+o);
    }
}

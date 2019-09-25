package dk.stbn.alarm.data;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import dk.stbn.alarm.R;
import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.diverse.Tid;
import dk.stbn.alarm.lyttere.Lyttersystem;
import dk.stbn.alarm.lyttere.Observatør;
import io.fabric.sdk.android.Fabric;

//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sune on 5/31/16.
 */
public class A extends Application implements Observatør {


    public SharedPreferences pref;
    public Lyttersystem lytter;
    AlarmLogik alarmlogik;
    public Tekstlogik tekstlogik;
    public Tilstand tilstand;


////TEST / DEBUGGING////////////////////////////////////////////////

    public static String hoved = "<!DOCTYPE html ><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>Log</title></head><body style=\"color: white; background-color: black;\">";
    public static String debugmsg = hoved;
    public static String hale = "</body></html>";

    public String henteurltest = "http://www.lightspeople.net/sune/skole/tekstertest.xml";


//////////-------------------------//////////


    //DatabaseReference myRef;

    /*-----------------------------noter

    Tjekke om rækkefølgen har betydning
    Tjekke logning af notifikationer
    Firebase sceduler før testversion
    Obbbbbs på at de to mærkedages-noti er identiske (intents ens?)
    Test-regneark med 1-2 ekstra i startren af august
    Firebase log med log over notifikationer


    * NeedToHave:
     *
     * Væk appen i baggrunden hver dag og tjek om der er nye tekster.
     Hvis der er, så pop en noti. Tjek først for ny tekstversion i på nettet
    *
   *
    * Mulighed for at loade en test-tekstfil som skal testes, INDEN den udgives:
    Ny akt med langt klik plus kode
    Knap med Vis samtlige tekster fra disk.
    Knap med Hent test-udgave af tekster.


    *
    * FEJL Nogle gange forsvinder brødtekster. Problemet er noget med viewpagereren eller dens adapter der vist cacher et eller andet..
    * Teori: Noget med genbrug.
    * Har det noget med skærmvending at gøre?
    * FAKTA: Webview er nogle gange null i onSaveInstancestate
    * LOG FRAGMENTET
    *
    *
    *
    * NiceToHave


    *
    * MEn hvvordan gør den hvis appen allerede er åben og der alarmmodtageren kaldes?
    * Kan muligivs løses med lyttersystem..
    *
    Refactoring
    *
    *





    Vi kan ikke teste noti-loop i sommerferien fordi modehed går fra 0 til 6.
    Løsning:




    *
    * */


    @Override
    public void onCreate() {
        super.onCreate();
        p("%_%_%_%_%_%_%_%_%_%_%_% oncreate() kaldt  %_%_%_%_%_%_%_%_%_%_%_%");
        boolean EMULATOR = Build.PRODUCT.contains("sdk") || Build.MODEL.contains("Emulator");
        if (!EMULATOR) {
            Fabric.with(getApplicationContext(), new Crashlytics());
            //Util.baglog = true;
            p("Enhed: " + Build.MODEL + "  " + Build.PRODUCT);
            p("Androidversion: "+Build.VERSION.SDK_INT);
        }


//        FirebaseApp.initializeApp(ctx);
//        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Util.starttid = System.currentTimeMillis();

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        lytter = Lyttersystem.getInstance();
        lytter.lyt(this);

        //Nulstil alle data ved ny version

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String appVersion = pInfo.versionName;

        boolean erNulstillet = pref.getBoolean(appVersion,false);

        if( ! erNulstillet ){
            int tempModenhed = tilstand.modenhed;
            sletAlt();

            pref.edit()
                    .putInt("modenhed", tempModenhed)
                    .putBoolean(appVersion, true).commit();

            tilstand.gemModenhed(tempModenhed);
            p(" Der er installeret en ny version af appen og data blev nulstillet");

        }

        else {

            tilstand = Tilstand.getInstance(getApplicationContext());
            alarmlogik = AlarmLogik.getInstance();
            tekstlogik = Tekstlogik.getInstance(getApplicationContext());
            init();
        }

        p("oncreate() færdig. tilstand.modenhed: (0=frisk, 1=første, 2=anden...) " + tilstand.modenhed);
        p("Gemt modenhed: " + K.modenhed(pref.getInt("modenhed", -1)));
    }

    void init(){

        tekstlogik.tjekSprog();

        if (tilstand.modenhed == K.MODENHED_HELT_FRISK)
            tekstlogik.udvælgTekster();
        else {
            tekstlogik.tjekTekstversion("init()"); //Fyrer event og A.opdater() kaldes hvis der er nye tekster på nettet.
            tekstlogik.visCachedeTekster();
            tekstlogik.indlæsHtekster();

            alarmlogik.startAlarmLoop(this);
        }



    }



    //Observer-callback
    @Override
    public void opdater(int hændelse) {

        if (hændelse == K.NYE_TEKSTER_ONLINE || hændelse == K.SPROG_ÆNDRET)
            tekstlogik.opdaterTekstbasen();
        else if (hændelse == K.INGEN_NYE_TEKSTER_ONLINE || hændelse == K.TEKSTBASEN_OPDATERET)
            tekstlogik.udvælgTekster();
        else if (hændelse == K.HTEKSTER_OPDATERET)
            tilstand.hteksterKlar = true;


    }






    /**
     * debugging: Tving ny app-dato. GAMMEL
     *
     */
    /*public void rul(int antaldage) {
        p("rul() kaldt");

        tilstand.masterDato = tilstand.masterDato.plusDays(antaldage);
        if (testtilstand_2) tilstand.masterDato = new DateTime();
//        t("Idag er "+ masterDato.getDayOfMonth() + " / " + masterDato.getMonthOfYear() + " - " + masterDato.getYear());

        synligeTekster = new ArrayList();  //brugeas af pageradapteren
        htekster = new ArrayList();
        synligeDatoer = null;
        hteksterOverskrifter = new ArrayList();
        tilstand.sidstKendteVindueshøjde = 0;

        tilstand.modenhed = K.MODENHED_HELT_FRISK;
        tilstand.femteDagFørsteGang = false;

        if (alm == null) alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        else p("alarmManager eksisterer");


        udvælgTekster();
        p("rul() sover 1½ sek");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        p("rul() starter ny aktivitet");

        Intent i = new Intent(this, Forside_akt.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "rul, synlige, UI-tråd? : " + Thread.currentThread().getName());
            }
        }, 10);
    }
*/




    //-- Kaldes når appen har kørt alle tekster igennnem og skal starte forfra med Otekst1
    void sletAlt() {
        p("sletAlt kaldt");
        sletDiskData();
        pref.edit().putInt("modenhed", K.MODENHED_FØRSTE_DAG).commit();
        tilstand = null;
        tilstand = Tilstand.getInstance(getApplicationContext());
        alarmlogik = null;
        alarmlogik = AlarmLogik.getInstance();
        tekstlogik = null;
        tekstlogik = Tekstlogik.getInstance(getApplicationContext());


       // tekstlogik.synligeTekster.clear();
        //tekstlogik.synligeTekster.add((Tekst) IO.læsObj(K.OTEKST_1, getApplicationContext()));
        //Lyttersystem.getInstance().givBesked(K.NYE_TEKSTER_ONLINE, "nulstillet");
        tekstlogik.allerFørsteGang(); //her sættes pref modenhed til 1 = FØRSTE DAG
    }

    void sletDiskData() {
        p("sletDiskData() blev kaldt");
        pref.edit().clear().commit();

        ArrayList tomTekst = new ArrayList<Tekst>();
        IO.gemObj(tomTekst, K.TEMP_SYNLIGETEKSTER, getApplicationContext());
        IO.gemObj(tomTekst, K.HTEKSTER, getApplicationContext());
        IO.gemObj(tomTekst, K.ITEKSTER, getApplicationContext());
        IO.gemObj(tomTekst, K.MTEKSTER, getApplicationContext());
        ArrayList<Integer> tomTal = new ArrayList<>();
        IO.gemObj(tomTal, K.SYNLIGEDATOER, getApplicationContext());
        IO.gemObj(tomTal, K.GAMLE, getApplicationContext());

    }




    void t(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    void p(Object o) {
        String kl = this.getClass().getSimpleName() + ".";
        Util.p(kl + o);
    }



}

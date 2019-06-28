package dk.stbn.alarm.data;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.appspector.sdk.AppSpector;
import com.crashlytics.android.Crashlytics;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.lyttere.Lyttersystem;
import dk.stbn.alarm.lyttere.Observatør;
import io.fabric.sdk.android.Fabric;

/**
 * Created by sune on 5/31/16.
 */
public class A extends Application implements Observatør {

    public static A a;
    public SharedPreferences pref;
    public Lyttersystem lytter;
    static AlarmManager alm; //TVM
    AlarmLogik alarmlogik;
    Tekstlogik tekstlogik;

//////////---------- TEKSTFRAGMENT/AKTIVITET DATA ----------//////////

    public ArrayList<Tekst> synligeTekster = new ArrayList();  //bruges af pageradapteren //TVM
    public ArrayList<Tekst> htekster = new ArrayList();  //TVM
    public ArrayList<String> hteksterOverskrifter = new ArrayList(); //TVM


//////////-------------------------//////////


//////////---------- APP TILSTAND ----------//////////

    public Tilstand tilstand;

//////////-------------------------//////////


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
        }
        AppSpector
                .build(getApplicationContext())
                .withDefaultMonitors()
                .run("android_ZDdiOWY3YWQtZGVjNy00ZWNiLThkMTAtYTI4YmI2OWIzNDEy");
//        FirebaseApp.initializeApp(ctx);
//        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Util.starttid = System.currentTimeMillis();

        a = this;
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        lytter = Lyttersystem.getInstance();
        lytter.lyt(this);
        tilstand = Tilstand.getInstance(getApplicationContext());
        alarmlogik = AlarmLogik.getInstance();
        tekstlogik = Tekstlogik.getInstance();

        init();

        p("oncreate() færdig. tilstand.modenhed: (0=frisk, 1=første, 2=anden...) " + tilstand.modenhed);
        p("Gemt modenhed: " + pref.getInt("modenhed", -1));
    }

    void init(){
        if (alm == null) alm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        tjekSprog();


        if (tilstand.modenhed == K.MODENHED_HELT_FRISK)
            tekstlogik.udvælgTekster(getApplicationContext());
        else {
            tjekTekstversion("init()"); //Fyrer event og A.opdater() kaldes hvis der er nye tekster på nettet.
            indlæsHtekster();
            visCachedeTekster();
        }
        alarmlogik.startAlarmLoop(this);


    }

    //Observer-callback
    @Override
    public void opdater(int hændelse) {

        if (hændelse == K.NYE_TEKSTER_ONLINE)
            opdaterTekstbasen();
        else if (hændelse == K.INGEN_NYE_TEKSTER_ONLINE || hændelse == K.TEKSTBASEN_OPDATERET)
            tekstlogik.udvælgTekster(getApplicationContext());
        else if (hændelse == K.HTEKSTER_OPDATERET)
            tilstand.hteksterKlar = true;
        else if (hændelse == K.SPROG_ÆNDRET)
            opdaterTekstbasen();

    }


    private void tjekSprog() {
        String sprog = Locale.getDefault().getLanguage();
        String gemtSprog = pref.getString("sprog", "ikke sat");
        //TODO:
        pref.edit().putString("sprog", sprog).commit();
        p("SPROG " + sprog);
    }


    /**
     * Henter gemte synlige tekster fra sidst
     */
    void visCachedeTekster() {

        synligeTekster = (ArrayList<Tekst>) IO.læsObj(K.SYNLIGETEKSTER, getApplicationContext());

        if (synligeTekster != null) {
            lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "visCachedeTekster()");
            p("visCachedeTekster() synligeTekster længde: "+synligeTekster.size());
        }
        else p("Fejl: gemte synlige tekster fandes ikke");
    }




    /**
     * Særlig fordi vi ved præcis hvilken tekst som skal vises når appen er nyinstalleret og alt andet kan køres i baggrunden.
     * Plus at diverse filer oprettes på disken ved første kørsel
     */
    void allerFørsteGang() {

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                p("allerFørsteGang() henter alle tekster..");
                ArrayList[] alleTekster = hentTeksterOnline("allerFørsteGang()");
                p("Data længde: "+ alleTekster.length + " | o: "+alleTekster[0].size() + " | i: "+alleTekster[1].size() + " | m: "+alleTekster[2].size() + " | h: "+alleTekster[3].size());

                //-- Denne kode burde egentlig stå i tilstand.opdaterModenhed() men er flyttet hertil så appen kun kan modnes hvis den får data første gang.
                int idag = Util.lavDato(tilstand.masterDato);

                pref.edit()
                        .putInt("modenhed", K.MODENHED_FØRSTE_DAG)
                        .putInt("installationsdato", idag)
                        .commit();
                tilstand.modenhed = K.MODENHED_FØRSTE_DAG;
                //-- hertil

                p("Finder O-tekst1...");
                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                o1.formater();
                synligeTekster.add(o1);
                p("Så er der O-tekst i array!");

                p("Event til aktiviteten om at synlige tekster er klar");

                if (tilstand.aktivitetenVises) {
                    publishProgress(1);
                }
                else {
                    p("Aktiviteten blev klar EFTER at data blev klar");
                }

                IO.gemObj(o1, K.OTEKST_1, getApplicationContext());

                p("Formaterer H-tekster...");
                htekster = alleTekster[3];
                for (Tekst t : htekster)
                    t.formater();

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift.toUpperCase());

                p("Event til aktiviteten om at H-tekster er klar");

                publishProgress(2);
                IO.gemObj(htekster, K.HTEKSTER, getApplicationContext());

                //-- Gemmer O-tekst nr 2 og 3 til næste gang

                Tekst o2 = otekster.get(1);
                o2.formater();
                IO.gemObj(o2, K.OTEKST_2, getApplicationContext());

                Tekst o3 = otekster.get(2);
                o3.formater();
                IO.gemObj(o3, K.OTEKST_3, getApplicationContext());


                p("Formaterer resten af listerne..");
                ArrayList<Tekst> itekster = alleTekster[1];
                p("Itekster længde når den hentes i allerførstegang(): "+itekster.size());

                for (Tekst t : itekster) t.formater();
                itekster = Util.sorterStigende(itekster);

                ArrayList<Tekst> mtekster = alleTekster[2];
                p("Mtekster længde når den hentes i allerførstegang(): "+mtekster.size());

                for (Tekst t : mtekster) t.formater();
                mtekster = Util.sorterStigende(mtekster);


                IO.gemObj(new ArrayList<Integer>(), K.GAMLE, getApplicationContext());
                IO.gemObj(new ArrayList<Integer>(), K.DATOLISTE, getApplicationContext());
                IO.gemObj(new ArrayList<Integer>(), K.SYNLIGEDATOER, getApplicationContext());

                if (tilstand.aktivitetenVises)
                    publishProgress(1);
                else
                    p("Aktiviteten stadig ikke klar selvom data blev klar");

                IO.gemObj(itekster, K.ITEKSTER, getApplicationContext());
                p("Itekster længde når den gemmes i allerførstegang(): "+itekster.size());
                IO.gemObj(mtekster, K.MTEKSTER, getApplicationContext());
                p("Mtekster længde når den gemmes i allerførstegang(): "+mtekster.size());

                gemEnkelteTeksterTilDisk(itekster);
                gemEnkelteTeksterTilDisk(mtekster);


                return null;
            }


            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int i = (int) values[0];
                if (i == 1)
                    lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "initallerførste Otekst klar, UI-tråd: ");
                else if (i == 2)
                    lytter.givBesked(K.HTEKSTER_OPDATERET, "initAllerførste_2 htekst, forgrund: " );

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if (synligeTekster.size() == 0) {
                    prøvIgen();
                }
                else{
                    gemSynligeTekster();

                    tjekTekstversion("allerFørsteGang()"); //kaldes for at lagre versionsnummeret for tekster på nettet

                }


            }


        }.execute();
    }

    private void prøvIgen() {

        t("Fejl ved hentning af data. Prøver igen...\nTjek evt. om der er netforbindelse");

        new Handler().postDelayed(() -> {
            p("Prøvigen() kaldt ");
            allerFørsteGang();

        }, 5000);

    }


    /**Som sikkerhed skal vi kunne hente en enkelt M- eller I-tekst fra disken på dens ID
     *
     * @param tekster
     */
    private void gemEnkelteTeksterTilDisk(ArrayList<Tekst> tekster) {

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    for (Tekst tekst : tekster)
                        IO.gemObj(tekst, "" + tekst.id_int, getApplicationContext());
                    return null;
                }
            }.execute();
    }



    public void gemSynligeTekster() {
        IO.gemObj(synligeTekster, K.SYNLIGETEKSTER, getApplicationContext());
    }

    private void opdaterTekstbasen(){
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                p("opdaterTekstbasen() henter alle tekster..");
                ArrayList[] alleTekster = hentTeksterOnline("opdaterTekstbasen()");

                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                o1.formater();
                IO.gemObj(o1, K.OTEKST_1, getApplicationContext());
                otekster = alleTekster[0];
                Tekst o2 = otekster.get(1);
                o2.formater();
                IO.gemObj(o2, K.OTEKST_2, getApplicationContext());
                Tekst o3 = otekster.get(2);
                o3.formater();
                IO.gemObj(o3, K.OTEKST_3, getApplicationContext());

                ArrayList<Tekst> itekster = alleTekster[1];
                for (Tekst t : itekster) t.formater();
                itekster = Util.sorterStigende(itekster);
                IO.gemObj(itekster, K.ITEKSTER, getApplicationContext());


                ArrayList<Tekst> mtekster = alleTekster[2];

                for (Tekst t : mtekster) t.formater();
                mtekster = Util.sorterStigende(mtekster);
                IO.gemObj(mtekster, K.MTEKSTER, getApplicationContext());
                publishProgress(1);

                ArrayList<Tekst> htekster = alleTekster[3];
                for (Tekst t : htekster)
                    t.formater();

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift.toUpperCase());
                p("Event til aktiviteten om at H-tekster er klar");

                publishProgress(2);
                IO.gemObj(htekster, K.HTEKSTER, getApplicationContext());

                gemEnkelteTeksterTilDisk(itekster);
                gemEnkelteTeksterTilDisk(mtekster);

                return null;
            }


            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int i = (int) values[0];
                if (i == 1)
                    lytter.givBesked(K.TEKSTBASEN_OPDATERET, "opdaterTekstbasen()");
                if (i == 2)
                    lytter.givBesked(K.HTEKSTER_OPDATERET, "opdaterTekstbasen()");

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);



            }


        }.execute();


    }

    /**
     * Henter tekster på nettet. Må KUN kaldes fra baggrundtråd
     * @param kaldtFra
     * @return
     */
    private ArrayList[] hentTeksterOnline(String kaldtFra) {
        p("hentTeksterOnline() kaldt fra " + kaldtFra);
        String input = "";
        try {
            //Tjekker sprog:
            String sprog = Locale.getDefault().getLanguage();
            pref.edit().putString("sprog", sprog).commit();
            p("henter nye tekster på sprog: " + sprog);
            URL u = new URL(K.henteurlDK);
            if (sprog.equalsIgnoreCase("de")) u = new URL(K.henteurlDE);
            InputStream is = u.openStream();
            is = new BufferedInputStream(is);
            is.mark(1);
            int read;
            read = is.read();
            if (read == 0xef) is.read();
            else is.reset();

            input = Util.inputStreamSomStreng(is);

            is.close();
        } catch (UnknownHostException uhex) {
            uhex.printStackTrace();
            Lyttersystem.getInstance().givBesked(K.OFFLINE, "hentTeksterOnline()");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Util.parseXML(input, "hentTeksterOnline");

    }

    /**
     * Fyrer en event hvis der er ny version
     *
     * @param kaldtFra
     */
    private void tjekTekstversion(String kaldtFra) {
        p("tjekTekstversion() kaldt fra " + kaldtFra);

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object... tekst) {

                String versionStreng = "";

                try {
                    URL url = new URL(K.versionUrl);

                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                    String str;
                    while ((str = in.readLine()) != null) {
                        versionStreng += str;
                    }
                    in.close();

                } catch (MalformedURLException me) {
                    me.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return versionStreng;
            }


            @Override
            protected void onPostExecute(Object tekst) {
                super.onPostExecute(tekst);
                String versionstekst = (String) tekst;
                int netversion = -1;

                if (!"".equals(versionstekst) && (versionstekst != null))
                    netversion = Util.tryParseInt(versionstekst);
                else
                    p("Fejl: Hentet tekstversion null eller tom");

                p("netversion: " + netversion);
                int gemtTekstversion = pref.getInt(K.TEKSTVERSION, 0);
                p("gemt tekstversion: " + gemtTekstversion);

                if (gemtTekstversion < netversion) {
                    //Måske giver det ikke rigtig mening med event længere efter Den Store Revidering
                    if (tilstand.modenhed > K.MODENHED_HELT_FRISK) //vi skal ikke fyre event første dag, da dse nyeste data allerede er hentet
                        lytter.givBesked(K.NYE_TEKSTER_ONLINE, "tjektekstversion, nye online");
                    pref.edit().putInt(K.TEKSTVERSION, netversion).commit();
                }
                else {
                    lytter.givBesked(K.INGEN_NYE_TEKSTER_ONLINE, "tjektekstversion, ingen nye online");
                }

            }
        }.execute();

    }


    /**
     * debugging: Tving ny app-dato. GAMMEL
     *
     * @param antaldage
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

    /**
     * Henter gemte Htekster og genererer en liste med overskrifterne
     */
    void indlæsHtekster() {

        lytter.givBesked(K.NYE_HTEKSTER_PÅ_VEJ, "indlæsHtekster()"); //Så brugeren ikke trykker netop mens den opdateres

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                p("indlæsHtekster() start");

                ArrayList<Tekst> gemteHtekster = (ArrayList<Tekst>) IO.læsObj(K.HTEKSTER, getApplicationContext());
                if (gemteHtekster == null) {
                    p("Fejl! ingen h-tekster på disk");
                    return false;
                }
                ArrayList<String> temp = new ArrayList<>();
                for (Tekst t : gemteHtekster) temp.add(t.overskrift.toUpperCase());
                hteksterOverskrifter = temp;
                htekster = gemteHtekster;
                p("indlæsHtekster() slut");

                return true;
            }

            @Override
            protected void onPostExecute(Object gikOK) {
                super.onPostExecute(gikOK);
                if ((boolean) gikOK) lytter.givBesked(K.HTEKSTER_OPDATERET, "indlæsHtekster()");

            }
        }.execute();

    }



    //-- Kaldes når appen har kørt alle tekster igennnem og skal starte forfra med Otekst1
    void sletAlt() {
        p("sletAlt kaldt");
        p("er tilstand null? "+ (tilstand == null));
        if (tilstand == null) tilstand = Tilstand.getInstance(getApplicationContext());
        else {
            Tilstand.getInstance(getApplicationContext()).nulstil();
            tilstand = Tilstand.getInstance(getApplicationContext());
        }
        sletDiskData();
        pref.edit().putInt("modenhed", K.MODENHED_FØRSTE_DAG).commit();


        synligeTekster.clear();
        synligeTekster.add((Tekst) IO.læsObj(K.OTEKST_1, getApplicationContext()));
        Lyttersystem.getInstance().givBesked(K.NYE_TEKSTER_ONLINE, "nulstillet");
        allerFørsteGang(); //her sættes pref modenhed til 1 = FØRSTE DAG
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


    int findTekstnr(int id) {

        for (int i = 0; i < synligeTekster.size(); i++)
            if (id == synligeTekster.get(i).id_int) return i;

        return -1;
    }

    //-- Alle Htekster har samme id_int
    public int findTekstnr(String overskrift) {
        for (int i = 0; i < synligeTekster.size(); i++)
            if (overskrift.equals(synligeTekster.get(i).overskrift)) return i;

        return -1;

    }


    void t(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    void p(Object o) {
        String kl = "A.";
        Util.p(kl + o);
    }



}

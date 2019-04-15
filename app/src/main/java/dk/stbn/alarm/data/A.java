package dk.stbn.alarm.data;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
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

import org.joda.time.DateTime;

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

import dk.stbn.alarm.aktivitetFragment.Forside_akt;
import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.diverse.Tid;
import dk.stbn.alarm.lyttere.Lyttersystem;
import dk.stbn.alarm.lyttere.Observatør;
import io.fabric.sdk.android.Fabric;

/**
 * Created by sune on 5/31/16.
 */
public class A extends Application implements Observatør {

    public static A a;
    public SharedPreferences pref;
    public Context ctx;
    public Lyttersystem lytter;
    static AlarmManager alm; //TVM
    AlarmLogik alarmlogik;

//////////---------- TEKSTFRAGMENT/AKTIVITET DATA ----------//////////

    public ArrayList<Tekst> synligeTekster = new ArrayList();  //bruges af pageradapteren //TVM
   // public ArrayList<Tekst> htekster = new ArrayList();  //TVM
    public ArrayList<String> hteksterOverskrifter = new ArrayList(); //TVM


//////////-------------------------//////////


//////////---------- APP TILSTAND ----------//////////

    public Tilstand tilstand;

//////////-------------------------//////////


//////////---------- MIDLERTIDIGE DATA ----------//////////

   // private ArrayList[] alleTekster;
    //private ArrayList<Tekst> itekster;
    //private ArrayList<Tekst> mtekster;
    //ArrayList<Integer> synligeDatoer;

//////////-------------------------//////////


////TEST / DEBUGGING////////////////////////////////////////////////

    public static String hoved = "<!DOCTYPE html ><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>Log</title></head><body style=\"color: white; background-color: black;\">";
    public static String debugmsg = hoved;
    public static String hale = "</body></html>";

    public static boolean debugging = true;
    public static boolean testtilstand = false;
    public static boolean testtilstand_2 = false;
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
    *Filnavne i KONSTANTER
    *
    *
    * */


    @Override
    public void onCreate() {
        super.onCreate();
        p("oncreate() kaldt");
        boolean EMULATOR = Build.PRODUCT.contains("sdk") || Build.MODEL.contains("Emulator");
        if (!EMULATOR) {
            Fabric.with(this, new Crashlytics());
            Util.baglog = true;
            p("Enhed: " + Build.MODEL + "  " + Build.PRODUCT);
        }
        AppSpector
                .build(this)
                .withDefaultMonitors()
                .run("android_ZDdiOWY3YWQtZGVjNy00ZWNiLThkMTAtYTI4YmI2OWIzNDEy");
//        FirebaseApp.initializeApp(ctx);
//        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Util.starttid = System.currentTimeMillis();

        a = this;
        ctx = this;
        lytter = Lyttersystem.getInstance();
        lytter.nulstil();
        lytter.lyt(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        tilstand = Tilstand.getInstance(pref);
        alarmlogik = AlarmLogik.getInstance();



        if (alm == null) alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);


        String sprog = Locale.getDefault().getLanguage();
        String gemtSprog = pref.getString("sprog", "ikke sat");
        pref.edit().putString("sprog", sprog).commit();
        p("SPROG " + sprog);

        //Init
        tjekTekstversion("A.onCreate()");
        indlæsHtekster((ArrayList<Tekst>) IO.læsObj());
        visCachedeTekster();
        udvælgTekster();

        p("oncreate() færdig. tilstand.modenhed: (0=frisk, 1=første, 2=anden...) " + tilstand.modenhed);
        p("Gemt modenhed: " + pref.getInt("modenhed", -1));
    }

    /**
     * Henter gemte synlige tekster fra sidst
     */
    void visCachedeTekster() {
        synligeTekster = (ArrayList<Tekst>) IO.læsObj("synligeTekster", this);
        lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "visCachedeTekster()");
    }

    /**
     * Udvælger tekster på baggrund af modenhed og om der er ny version på nettet
     */
    private void udvælgTekster() {
        int modenhed = tilstand.modenhed;

        ArrayList<Tekst> tempSynlige = new ArrayList<>();

        if (modenhed == K.SOMMERFERIE) {
            p("sommerferie!!!");

            tempSynlige.add((Tekst) IO.læsObj("otekst1", this));
            tempSynlige.add((Tekst) IO.læsObj("otekst2", this));
            tempSynlige.add((Tekst) IO.læsObj("otekst3", this));

        } else if (modenhed == K.MODENHED_HELT_FRISK) {
            p("udvælgTekster() Modenhed: Helt frisk");
            allerFørsteGang();
            IO.gemObj(new DateTime(), "masterdato", this);

        } else if (modenhed == K.MODENHED_FØRSTE_DAG) {
            p("Dag 1, ikke første gang");

            tempSynlige.add((Tekst) IO.læsObj("otekst1", this));

        } else if (modenhed == K.MODENHED_ANDEN_DAG) {
            p("Dag 2 ");

            Tekst oTekst1 = (Tekst) IO.læsObj("otekst1", ctx);
            Tekst oTekst2 = (Tekst) IO.læsObj("otekst2", ctx);
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);

        } else if (modenhed == K.MODENHED_TREDJE_DAG) {
            p("Dag 3 ");

            Tekst oTekst1 = (Tekst) IO.læsObj("otekst1", ctx);
            Tekst oTekst2 = (Tekst) IO.læsObj("otekst2", ctx);
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);

            ArrayList<Tekst> tekster = findItekster();
            if (tekster.size() > 0)
                tempSynlige.add(tekster.get(0));

        } else if (modenhed == K.MODENHED_FJERDE_DAG) {
            p("Dag 4 ");
            Tekst oTekst2 = (Tekst) IO.læsObj("otekst2", ctx);
            tempSynlige.add(oTekst2);
            ArrayList<Tekst> tekster = findItekster();
            if (tekster.size() > 0) tempSynlige.add(tekster.get(0));
            if (tekster.size() > 1) tempSynlige.add(tekster.get(1));

        } else if (modenhed == K.MODENHED_MODEN) {
            p("Dag 5: MODEN ");

            ArrayList<Tekst> itekster = findItekster();
            tempSynlige.addAll(itekster);
            ArrayList<Tekst> mtekster = findMtekster();
            tempSynlige.addAll(mtekster);

        }

        p("Tjekker om vi kan beholde de cachede tekster..");

        boolean skift = false;

        skift = synligeTekster.size() != tempSynlige.size();


        if (!skift) {
            boolean forskellige = false;

            for (int i = 0; i < synligeTekster.size(); i++){
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
            p("Nyt udvalg af tekster");
            synligeTekster = tempSynlige;
            pref.edit().putInt("senesteposition", -1).commit(); //Sætter ViewPagerens position til nyeste element
            lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "udvælgTekster(), der var et nyt udvalg");
        }
        else
            p("Vi bruger de cachede tekster");

        if (modenhed < K.MODENHED_TREDJE_DAG){
            //sørg for at der ikke vises notifikationer i starten
            for (Tekst t : synligeTekster)
                IO.føjTilGamle(t.id_int, ctx);
        }

        gemSynligeTekster();
        p("udvælgTekster() færdig");
    }


    private ArrayList<Tekst> findMtekster() {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> mtekster = (ArrayList<Tekst>) IO.læsObj("mtekster", this);


        Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", tilstand.masterDato);
        dummyMTekst.lavId();
        p("Tjek M dummytekst id: " + dummyMTekst.id_int);

        boolean mFundet = false;

       p("Mtekster længde: " + mtekster.size());

        for (int i = 0; i < mtekster.size(); i++) {
            Tekst mtekst = mtekster.get(i);
            p("tjek mtekster: " + mtekst.id_int);
            p("IdTekst: " + mtekst.id);
            //datoliste.add(mtekst.id_int);
            //IO.gemObj(mtekst, "" + mtekst.id_int, ctx);
            if (mtekst.id_int >= dummyMTekst.id_int) {

                if (!mFundet) {

                    if (mtekst.id_int == dummyMTekst.id_int) {
                        p("Eksakt match Mtekst");
                        r.add(mtekst);

                    } else if (alarmlogik.visMtekst(mtekst.dato, tilstand.masterDato)) {
                        r.add(mtekst);
                        p("Mtekst ineksakt match --");
                    }
                    mFundet = true;
                }
            }
        }
        return r;

    }

    private ArrayList<Tekst> findItekster() {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> itekster = (ArrayList<Tekst>) IO.læsObj("itekster", this);

        Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", tilstand.masterDato);
        dummyITekst.lavId();

        p("Tjek dummytekst id: " + dummyITekst.id_int);
        p("itekster længde: " + itekster.size());

        boolean iFundet = false;
        //            fejlen med visning af tekster er her omkring
        for (int i = 0; i < itekster.size(); i++) {
            Tekst itekst = itekster.get(i);
            int tekstid = itekst.id_int;
            p("Tjek Itekster: " + tekstid);
            p("IdTekst: " + itekst.id);

            //datoliste.add(itekst.id_int);
            //IO.gemObj(itekst, "" + tekstid, ctx);

            //Tjek om teksten skal vises
            if (!iFundet && tekstid >= dummyITekst.id_int) {

                if (tekstid == dummyITekst.id_int) {
                    p("Itekst eksakt match");
                    iFundet = true;

                    if (i > 1) r.add(itekster.get(i - 2));
                    if (i > 0) r.add(itekster.get(i - 1));
                    r.add(itekster.get(i));


                } else {
                    p("I ineksakt match");
                    iFundet = true;
                    if (i > 2) r.add(itekster.get(i - 3));
                    if (i > 1) r.add(itekster.get(i - 2));
                    if (i > 0) r.add(itekster.get(i - 1));
                    else r.add(itekster.get(i));
                }
            }
        }
        return r;


    }

    /**
     * Særlig fordi vi ved præcis hvilken tekst som skal vises og alt andet kan køres i baggrunden.
     * Plus at diverse filer oprettes på disken ved første kørsel
     */
    void allerFørsteGang() {

        tjekTekstversion("allerFørsteGang"); //køres for at få gemt versionsnummer i prefs første gang

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                p("allerFørsteGang() henter alle tekster..");
                ArrayList[] alleTekster = hentTeksterOnline("allerFørsteGang()");

                //-- Denne kode burde egentlig stå i tilstand.opdaterModenhed() men er flyttet hertil så appen kun kan modnes hvis den får data første gang.
                int idag = Util.lavDato(tilstand.masterDato);

                pref.edit()
                        .putInt("modenhed", K.MODENHED_FØRSTE_DAG)
                        .putInt("installationsdato", idag)
                        .commit();
                //-- hertil

                p("Finder O-tekst1...");
                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                o1.formaterTekst();
                synligeTekster.add(o1);
                p("Så er der O-tekst i array!");

                p("Event til aktiviteten om at synlige tekster er klar");
                boolean aktivitetKlar = false;

                if (tilstand.aktivitetenVises) {
                    publishProgress(1);
                    aktivitetKlar = true;
                } else {
                    p("Aktiviteten blev klar EFTER at data blev klar");
                }

                IO.gemObj(o1, "otekst1", ctx);



                p("Formaterer H-tekster...");
                ArrayList<Tekst> htekster = alleTekster[3];
                for (Tekst t : htekster)
                    t.formaterTekst();

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift.toUpperCase());

                p("Event til aktiviteten om at H-tekster er klar");

                publishProgress(2);
                IO.gemObj(htekster, K.HTEKSTER, ctx);

                //-- Gemmer O-tekst nr 2 og 3 til næste gang
                otekster = alleTekster[0];
                Tekst o2 = otekster.get(1);
                o2.formaterTekst();
                IO.gemObj(o2, "otekst2", ctx);

                Tekst o3 = otekster.get(2);
                o3.formaterTekst();
                IO.gemObj(o3, "otekst3", ctx);

                ArrayList<Tekst> itekster = alleTekster[1];

                p("Formaterer resten af listerne..");
                for (Tekst t : itekster) t.formaterTekst();
                Util.sorterStigende(itekster);

                ArrayList<Tekst> mtekster = alleTekster[2];

                for (Tekst t : mtekster) t.formaterTekst();
                Util.sorterStigende(mtekster);


                IO.gemObj(new ArrayList<Integer>(), "gamle", ctx);
                IO.gemObj(new ArrayList<Integer>(), "datoliste", ctx);
                IO.gemObj(new ArrayList<Integer>(), "synligeDatoer", ctx);

                if (!aktivitetKlar && tilstand.aktivitetenVises)
                    publishProgress(1);
                else
                    p("Aktiviteten STADIG ikke klar selvom data blev klar");


                return null;
            }


            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int i = (int) values[0];
                if (i == 1)
                    lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "initallerførste Otekst klar, UI-tråd: " + Thread.currentThread().getName());
                else if (i == 2)
                    lytter.givBesked(K.HTEKSTER_OPDATERET, "initAllerførste_2 htekst, forgrund: " + Thread.currentThread().getName());

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if (synligeTekster.size() == 0) {
                    prøvIgen();
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


    int hentNyeTeksterTæller = 1;

    public void hentOgGemNyeTekster() {
        p("hentOgGemNyeTekster() kaldt. Gang nr " + hentNyeTeksterTæller);
        hentNyeTeksterTæller++;

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                p("...ny tekstversion tilgængelig, kalder hentTeksterOnline()");

                ArrayList[] alleTekster = hentTeksterOnline("hentOgGemNyeTekster");

                //nyt versionsnr tjekkes og gemmes ikker her men i tjektekstversion()
                itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));
                return null;
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int i = (int) values[0];
                if (i == 1)
                    lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "initallerførste Otekst klar, UI-tråd: " + Thread.currentThread().getName());
                else if (i == 2)
                    lytter.givBesked(K.HTEKSTER_OPDATERET, "initAllerførste_2 htekst, forgrund: " + Thread.currentThread().getName());

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                udvælgTekster();

                formaterLister(); //Kæde: metoden gemAlle..() kører i baggrunden
            }
        }.execute();

    }

      //Burde være overflødig efter Den Store Tur
    private void gemAlleTeksterTilDisk() {
        if (itekster.size() > 0)
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    IO.gemObj(itekster, "itekster", A.a);
                    IO.gemObj(mtekster, "mtekster", A.a);
                    IO.gemObj(htekster, "htekster", A.a);

                    //itekster = null;
                    //mtekster = null;

                    return null;
                }
            }.execute();
    }
    //Burde være overflødig efter Den Store Tur
    public boolean skalTekstlistenOpdateres(String kaldtfra) {
        p("skalTekstlistenOpdateres(" + kaldtfra + ") start________");

        if (tilstand.modenhed == K.SOMMERFERIE) return false;

        new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                //-- nulstiller returværdien
                pref.edit().putBoolean("nyTekst", false).commit();
            }

            @Override
            protected Object doInBackground(Object... o) {

                //-- datoliste er listen med alle teksters  id'er

                ArrayList<Integer> datoliste = (ArrayList<Integer>) IO.læsObj("datoliste", ctx); //hvis denne gøres global, kan den initalisteres når som helst - dvs igså tidligere.

                //-- Hvis datolisten er null, er appen helt frisk
                if (datoliste == null) {
                    return false;
                }
                //-- Hvis datolisten er tom, er det fordi vi er nået til slutningen af skoleåret og der er ikke flere nye tekster
                else if (datoliste.size() == 0) {
                    p("Datolisten er tom!!!");
                    return false;
                }

                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", tilstand.masterDato);
                dummyITekst.lavId();
                int idag = dummyITekst.id_int;
                p("Tjek I dummytekst id: " + dummyITekst.id_int);

                boolean iFundet = false;

                Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", tilstand.masterDato);
                dummyMTekst.lavId();
                int mIdag = dummyMTekst.id_int;
                p("Tjek M dummytekst id: " + dummyMTekst.id_int);


                ArrayList<Integer> slettes = new ArrayList<>();


                //TODO: Optimeres senere

                synligeDatoer.clear(); //todo: ikke nødvendigt at gemme den på disk. Ryd op i det
                for (int i = 0; i < datoliste.size(); i++) {
                    int tekstid = datoliste.get(i);

                    if (tekstid < 300000000) {
                        p("Tjek datoliste skalTOpdateres? I: " + tekstid);

                        if (tekstid >= idag) {


                            if (!iFundet && tekstid == idag) {
                                p("Itekst eksakt match: " + tekstid);
                                iFundet = true;
                                if (i > 1) synligeDatoer.add(datoliste.get(i - 2));
                                if (i > 0) synligeDatoer.add(datoliste.get(i - 1));
                                synligeDatoer.add(datoliste.get(i));
                            } else if (!iFundet) {
                                p("I ineksakt match: dummy: " + dummyITekst.id_int + " | tekst: " + tekstid);
                                iFundet = true;
                                if (i > 2) synligeDatoer.add(datoliste.get(i - 3));
                                if (i > 1) synligeDatoer.add(datoliste.get(i - 2));
                                if (i > 0) synligeDatoer.add(datoliste.get(i - 1));
                                else synligeDatoer.add(datoliste.get(i));
                            }

                        }
                        //else slettes.add(tekstid);
                    } else { //if tekstid > 300000000
                        p("Tjek datoliste skalTOpdateres? M: " + tekstid);

                        //Todo: bør skrives om til at bruge tekst id i stedet for at hente alle tekster
                        Tekst t = (Tekst) IO.læsObj("" + datoliste.get(i), ctx);

                        if (alarmlogik.visMtekst(t.dato, tilstand.masterDato)) {
                            synligeDatoer.add(datoliste.get(i));
                            break; //Tillader ikke to m-tekster. KAN konflikte med notifikationer!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        } else if (Tid.fortid(t.dato))
                            slettes.add(datoliste.get(i));

                    }
                }

                //-- Renser ud i gamle tekster
                if (synligeDatoer.size() > 0) {
                    int ældsteI = synligeDatoer.get(0);
                    for (Integer i : datoliste) if (i < ældsteI) slettes.add(i);

                    for (Integer j : slettes) IO.føjTilGamle(j, ctx);
                } else //hvis listen er tom, er det fordi appen er et år gammel og der skal nulstilles
                    //sletAlt(); //Ikke længere relevant. Håndteres nu i erDerGået5DageOg...

                    for (Integer i : slettes) datoliste.remove(i);

                IO.gemObj(datoliste, "datoliste", ctx);


                p("skalTekstlistenOpdateres() async slut");

                return true;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                boolean ny = false;


                if (synligeDatoer.size() != synligeTekster.size()) {
                    ny = true;
                    p("Listerne var forskellig længde: SynligeDatoer: " + synligeDatoer.size() + " SynligeTekster: " + synligeTekster.size());
                } else {
                    p("Listerne var samme længde: SynligeDatoer: " + synligeDatoer.size() + " SynligeTekster: " + synligeTekster.size());

                    for (int i = 0; i < synligeTekster.size(); i++) {

                        int a = synligeDatoer.get(i);
                        int b = synligeTekster.get(i).id_int;
                        if (a != b) {
                            ny = true;
                            break;
                        }
                    }
                }

                if (ny) {
                    synligeTekster.clear();
                    p("skalTekstlistenOpdateres synligetekster er NY");

                    for (Integer i : synligeDatoer) {
                        p("dato: " + i);
                        synligeTekster.add((Tekst) IO.læsObj("" + i, ctx));

                    }
                    lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "skaltekstlistenopdaetere() synlige UI-tråd: " + Thread.currentThread().getName());
                    gemSynligeTekster();
                    IO.gemObj(synligeDatoer, "synligeDatoer", ctx);

                    alarmlogik.rensUdIAlarmer(ctx);
                } else p("skalTekstlistenOpdateres Ingen ny synlige");
                p("skalTekstlistenOpdateres() slut");

                //-- (lidt sløv) måde at overføre returværdi. 'o' er falsk hvis datolisten er tom og der ikke er flere tekster at vise
                if ((boolean) o) pref.edit().putBoolean("nyTekst", ny).commit();
                else pref.edit().putBoolean("nyTekst", false).commit();


            }

        }.execute();

        return pref.getBoolean("nyTekst", false);
    }

    public void gemSynligeTekster() {
        //new async ?
        IO.gemObj(synligeTekster, "synligeTekster", this);
    }

    /**
     * Kaldes KUN fra baggrundtråd
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
                int gemtTekstversion = pref.getInt("tekstversion", 0);
                p("gemt tekstversion: " + gemtTekstversion);

                if (gemtTekstversion < netversion) {
                    //Måske giver det ikke rigtig mening med event længere efter Den Store Revidering
                    if (tilstand.modenhed > K.MODENHED_HELT_FRISK)
                        lytter.givBesked(K.NYE_TEKSTER_ONLINE, "tjektekstversion, nye online");
                    pref.edit().putInt("tekstversion", netversion).commit();
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
    void indlæsHtekster(final ArrayList<Tekst> hTekster) {
        lytter.givBesked(K.NYE_HTEKSTER_PÅ_VEJ, "indlæsHtekster()"); //Så brugeren ikke trykker netop mens den opdateres

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                ArrayList<String> temp = new ArrayList<>();
                for (Tekst t : hTekster) temp.add(t.overskrift.toUpperCase());
                hteksterOverskrifter = temp;
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                lytter.givBesked(K.HTEKSTER_OPDATERET, "indlæsHtekster()");
            }
        }.execute();

    }



    //-- Kaldes når appen har kørt alle tekster igennnem og skal starte forfra med tekst1
    static void sletAlt() {
        tilstand.nulstil();
        p("sletAlt kaldt");
        sletDiskData();
        synligeTekster.clear();
        synligeTekster.add((Tekst) IO.læsObj("otekst1", this));
        Lyttersystem.getInstance().givBesked(K.NYE_TEKSTER_ONLINE, "nulstillet");
        tilstand.gemModenhed(K.MODENHED_HELT_FRISK);
        allerFørsteGang(); //her sættes pref modenhed til 1 = FØRSTE DAG
    }

    void sletDiskData() {
        p("sletDiskData() blev kaldt");
        pref.edit().clear().commit();

        ArrayList tomTekst = new ArrayList<Tekst>();
        IO.gemObj(tomTekst, "tempsynligeTekster", this);
        IO.gemObj(tomTekst, "htekster", this);
        ArrayList<Integer> tomTal = new ArrayList<>();
        IO.gemObj(tomTal, "synligeDatoer", this);
        IO.gemObj(tomTal, "gamle", this);

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
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    void p(Object o) {
        String kl = "A.";
        Util.p(kl + o);
    }

    @Override
    public void opdater(int hændelse) {

        if (hændelse == K.NYE_TEKSTER_ONLINE) {
            //if (tilstand.modenhed == K.MODENHED_MODEN || tilstand.modenhed == K.SOMMERFERIE)
            hentOgGemNyeTekster();
        } else if (hændelse == K.HTEKSTER_OPDATERET)
            tilstand.hteksterKlar = true;


    }


}

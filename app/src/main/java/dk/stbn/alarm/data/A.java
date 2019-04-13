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

import dk.stbn.alarm.Tekst;
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
    public ArrayList<Tekst> htekster = new ArrayList();  //TVM
    public ArrayList<String> hteksterOverskrifter = new ArrayList(); //TVM


//////////-------------------------//////////


//////////---------- APP TILSTAND ----------//////////

    public Tilstand tilstand;

//////////-------------------------//////////
	
	

//////////---------- MIDLERTIDIGE DATA ----------//////////

	private ArrayList[] alleTekster;
	private ArrayList<Tekst> itekster;
    private ArrayList<Tekst> mtekster;
    ArrayList<Integer> synligeDatoer;

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
     * Hvis der går lang tid mellem at appen er åben, når appen forbi de noti som er 'bestilt'
    * Lazy loading !!!
    *
    * Lad O-tekster blive indtil de "skubbes ud"
    *
    * Mulighed for at loade en test-tekstfil som skal testes, INDEN den udgives
    *
    * FEJL Nogle gange forsvinder brødtekster.
    * Teori: Noget med genbrug.
    * Men: det har vist noget med skærmvending at gøre..
    * FAKTA: Webview er nogle gange null i onSaveInstancestate
    * LOG FRAGMENTET
    *
    *
    *
    * NiceToHave
    *
    * Bedre struktur
    * en metode svarende til gemAlleNyeTekster i Util. Kaldes i service/baggundstråd  når alarmMODTAGEREN kaldes.
    * Gemmer en ny synligeTekster Arraylist under samme navn, så den altid henter en rigtig liste ved opstart
    * Smart fordi det er det tidligste tidspunkt vi kan vide det.
    * MEn hvvordan gør den hvis appen allerede er åben og der alarmmodtageren kaldes?
    * Kan muligivs løses med lyttersystem..
    *
    *
    *
    *
    * Let anync
    AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    * */
	

    @Override
    public void onCreate() {
        super.onCreate();
        p("oncreate() kaldt");
        boolean EMULATOR = Build.PRODUCT.contains("sdk") || Build.MODEL.contains("Emulator");
        if (!EMULATOR) {
            Fabric.with(this, new Crashlytics());
            Util.baglog = true;
            p("Enhed: " + Build.MODEL + "  "+Build.PRODUCT);
        }
        AppSpector
                .build(this)
                .withDefaultMonitors()
                .run("android_ZDdiOWY3YWQtZGVjNy00ZWNiLThkMTAtYTI4YmI2OWIzNDEy");
//        FirebaseApp.initializeApp(ctx);
//        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Util.starttid = System.currentTimeMillis();

        a= this;
        ctx=this;
        lytter = Lyttersystem.getInstance();
        tilstand = Tilstand.getInstance(this, a);
        alarmlogik = AlarmLogik.getInstance();
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        lytter.nulstil();
        lytter.lyt(this);

        if (alm == null)  alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);



        String sprog = Locale.getDefault().getLanguage();
        String gemtSprog = pref.getString("sprog", "ikke sat");
        pref.edit().putString("sprog", sprog).commit();
        p("SPROG "+ sprog);

        //Init
        tjekTekstversion("A.onCreate()");
        indlæsHtekster();
        udvælgTekster();

        p("oncreate() færdig. tilstand.modenhed: (0=frisk, 1=første, 2=anden...) "+ tilstand.modenhed);
        p("Gemt modenhed: "+ pref.getInt("modenhed", -1));

    }//Oncreate færdig

    void visCachedeTekster(){
        synligeTekster = (ArrayList<Tekst>) IO.læsObj("synligeTekster", this);
    }

    /**
     * Udvælger tekster på baggrund af modenhed
     */
    private void udvælgTekster() {
        int modenhed = tilstand.modenhed;

        if (modenhed > K.MODENHED_HELT_FRISK)
            // ikke testet: visCachedeTekster();

        if (modenhed == K.SOMMERFERIE) {
            p("sommerferie!!!");
            synligeTekster.clear();
            synligeTekster.add((Tekst) IO.læsObj("otekst1", this));
            synligeTekster.add((Tekst) IO.læsObj("otekst2", this));
            synligeTekster.add((Tekst) IO.læsObj("otekst3", this));

            //der skal være noget i synligeDatoer, ellers kaldes sletAlt()
            synligeDatoer = new ArrayList<>();
            synligeDatoer.add(1000);

        }
        else if (modenhed == K.MODENHED_HELT_FRISK){
            p("udvælgTekster() Modenhed: Helt frisk");
            allerFørsteGang();
            IO.gemObj(new DateTime(), "masterdato", this);
        }

        else if (modenhed == K.MODENHED_FØRSTE_DAG) {
            synligeTekster.clear();
            synligeTekster.add((Tekst) IO.læsObj("otekst1", this));
            gemSynligeTekster();
            p("Dag 1, ikke første gang");
            return;
        }

        else if (modenhed == K.MODENHED_ANDEN_DAG) {
            p("Dag 2 ");
            synligeTekster.clear();
            Tekst oTekst1 = (Tekst) IO.læsObj("otekst1", ctx);
            Tekst oTekst2 = (Tekst) IO.læsObj("otekst2", ctx);
            synligeTekster.add(oTekst1);
            synligeTekster.add(oTekst2);
            gemSynligeTekster();
            pref.edit().putInt("senesteposition", -1).commit();

            }
        else if (modenhed == K.MODENHED_TREDJE_DAG) {
            p("Dag 3 ");
            synligeTekster.clear();
            Tekst oTekst1 = (Tekst) IO.læsObj("otekst1", ctx);
            Tekst oTekst2 = (Tekst) IO.læsObj("otekst2", ctx);
            synligeTekster.add(oTekst1);
            synligeTekster.add(oTekst2);

            ArrayList<Tekst> tekster = findItekster();
            synligeTekster.add(tekster.get(0));

            gemSynligeTekster();
            pref.edit().putInt("senesteposition", -1).commit();

        }


        else if (modenhed == K.MODENHED_FJERDE_DAG) {
            p("Dag 4 ");

            synligeTekster.clear();
            Tekst oTekst2 = (Tekst) IO.læsObj("otekst2", ctx);
            synligeTekster.add(oTekst2);

            ArrayList<Tekst> tekster = findItekster();
            synligeTekster.add(tekster.get(0));
            synligeTekster.add(tekster.get(1));

            gemSynligeTekster();
            pref.edit().putInt("senesteposition", -1).commit();

        }


        else if (modenhed == K.MODENHED_MODEN) {

            synligeTekster.clear();
            ArrayList<Tekst> itekster = findItekster();
            synligeTekster.addAll(itekster);
            ArrayList<Tekst> mtekster = findMtekster();
            synligeTekster.addAll(mtekster);

            gemSynligeTekster();
            pref.edit().putInt("senesteposition", -1).commit();


        if (skalTekstlistenOpdateres("a")) {
            pref.edit().putInt("senesteposition", -1).commit(); //Sætter ViewPagerens position til nyeste element
        }
        if (modenhed == K.MODENHED_MODEN) {


            if (skalTekstlistenOpdateres("a")) {
                pref.edit().putInt("senesteposition", -1).commit(); //Sætter ViewPagerens position til nyeste element
            }



        }


        //if (modenhed > K.MODENHED_HELT_FRISK) {


            /*
            if (tilstand.femteDagFørsteGang){
                p("femte dag første gang!! ");
                //-- Viewpageren nulstilles (og viser sidste element i listen når det starter)
                pref.edit().putInt("senesteposition", -1).commit();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        synligeTekster = (ArrayList<Tekst>) IO.læsObj("tempsynligeTekster", ctx);

                        //-- Sørger for at der ikke vises notifikationer for helt nye tekster
                        for (Tekst t : synligeTekster)
                            IO.føjTilGamle(t.id_int, ctx);
HER FRA
                        alarmlogik.opdaterKalender(ctx, "Application singleton");  //-- Kaldes ellers kun fra BootLytter
                        gemSynligeTekster();
                    }
                });

                tilstand.femteDagFørsteGang = false;
            }
            else {
                synligeTekster = hentsynligeTekster();

            }

            if (synligeTekster == null) synligeTekster = new ArrayList(); //har oplevet at den der blev hentet på disk var null i forbindelse med opdatering
            else {


            }
            synligeDatoer = (ArrayList<Integer>) IO.læsObj("synligeDatoer", ctx);
            */
            indlæsHtekster();

        }



        p("udvælgTekster() færdig");
    }

    private ArrayList<Tekst> findMtekster() {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> mtekster = (ArrayList<Tekst>) IO.læsObj("mtekster", this);


        Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", tilstand.masterDato);
        dummyMTekst.lavId();
        p("Tjek M dummytekst id: "+dummyMTekst.id_int);

        boolean mFundet = false;

        //ArrayList<Tekst> mtekster = Util.sorterStigende(alleTekster[2]);

        p("Mtekster længde: "+mtekster.size());

        for (int i = 0; i < mtekster.size(); i++) {
            Tekst mtekst = mtekster.get(i);
            p("tjek mtekster: " + mtekst.id_int);
            p("IdTekst: "+mtekst.id);
            //datoliste.add(mtekst.id_int);
            //IO.gemObj(mtekst, "" + mtekst.id_int, ctx);
            if (mtekst.id_int >= dummyMTekst.id_int) {

                if (!mFundet) {

                    if (mtekst.id_int == dummyMTekst.id_int) {
                        p("Eksakt match Mtekst");
                        r.add(mtekst);

                    } else if (alarmlogik.visMtekst(mtekst.dato, tilstand.masterDato) ){
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

        p("Tjek dummytekst id: "+dummyITekst.id_int);
        p("itekster længde: "+ itekster.size());

        boolean iFundet = false;
        //            fejlen med visning af tekster er her omkring
        for (int i = 0; i < itekster.size(); i++) {
            Tekst itekst = itekster.get(i);
            int tekstid = itekst.id_int;
            p("Tjek Itekster: " + tekstid);
            p("IdTekst: "+itekst.id);

            //datoliste.add(itekst.id_int);
            //IO.gemObj(itekst, "" + tekstid, ctx);

            //Tjek om teksten skal vises
            if (!iFundet && tekstid >= dummyITekst.id_int) {

                if (tekstid == dummyITekst.id_int) {
                    p("Itekst eksakt match");
                    iFundet = true;

                    if (i>1)r.add(itekster.get(i - 2));
                    if (i>0) r.add(itekster.get(i - 1));
                    r.add(itekster.get(i));


                } else {
                    p("I ineksakt match");
                    iFundet = true;
                    if (i>2) r.add(itekster.get(i - 3));
                    if (i>1)r.add(itekster.get(i - 2));
                    if (i>0) r.add(itekster.get(i - 1));
                    else r.add(itekster.get(i));
                }
            }
        }
        return r;


    }


    void allerFørsteGang(){

        tjekTekstversion("allerFørsteGang"); //køres for at få gemt versionsnummer i prefs første gang
        // må ikke kaldes fra baggrundstråd?

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                alleTekster = hentTeksterOnline("allerFørsteGang()");

                //-- Denne kode burde egentlig stå i tjekModenhed() men er flyttet hertil så appen kun kan modnes hvis den får data første gang.
                int idag = Util.lavDato(tilstand.masterDato);

                pref.edit()
                        .putInt("modenhed", K.MODENHED_FØRSTE_DAG)
                        .putInt("installationsdato", idag)
                        .commit();
                //-- hertil

                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                String nyBrødtekst = o1.brødtekst.replaceAll("\n", " ");
                o1.brødtekst = nyBrødtekst;
                synligeTekster.add(o1);
                p("Så er der O-tekst i array!");

                if (tilstand.aktivitetenVises)
                    publishProgress(1);
                else
                    p("Aktiviteten blev klar EFTER at data blev klar");

                IO.gemObj(o1, "otekst1", ctx);
                htekster = Util.erstatAfsnit(alleTekster[3]);

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift.toUpperCase());
                publishProgress(2);
                IO.gemObj(htekster,"htekster",ctx);

                //-- Gemmer O-tekst nr 2 til næste gang

                otekster = alleTekster[0];
                Tekst o2 = otekster.get(1);
                String nyBrødtekst2 = o2.brødtekst.replaceAll("\n", " ");

                o2.brødtekst = nyBrødtekst2;

                IO.gemObj(o2, "otekst2", ctx);

                Tekst o3 = otekster.get(2);
                String nyBrødtekst3 = o3.brødtekst.replaceAll("\n", " ");

                o3.brødtekst = nyBrødtekst3;
                p("otekst3 lige hentet: "+o3);
                IO.gemObj(o3, "otekst3", ctx);

                //-- Gemmer resten
                itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));
                IO.gemObj(new ArrayList<Integer>(), "gamle", ctx);
                IO.gemObj(new ArrayList<Integer>(), "datoliste", ctx);
                IO.gemObj(new ArrayList<Integer>(), "synligeDatoer", ctx);




                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if (alleTekster[0].size() == 0) {
                    prøvIgen();
                    return;
                }

            }

            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int i = (int) values[0];
                if (i==1) lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "initallerførste Otekst klar, UI-tråd: "+Thread.currentThread().getName());
                else if (i == 2)  lytter.givBesked(K.HTEKSTER_OPDATERET, "initAllerførste_2 htekst, forgrund: "+Thread.currentThread().getName());

            }
        }.execute();
    }

    private void prøvIgen(){

        t("Fejl ved hentning af data. Prøver igen...\nTjek evt. om der er netforbindelse");

        new Handler().postDelayed(() -> {
            p("Prøvigen() kaldt "+Thread.currentThread().getName());
            allerFørsteGang();

        }, 5000);

    }

    private void allerførsteGangInitHtekster() {

        p("allerførsteGangInitHtekster() kaldt");

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {


                //-- gemmer h-tekster




                return null;
            }



            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                gemAlleNyeTekster(); // async-kæde: ting der også kan gøres i baggrunden, men som er afhængige af værdier fra denne metode
				
            }
        }.execute();
    }

    int hentNyeTeksterTæller = 1;

    public void hentNyeTekster() {
        p("hentNyeTekster() kaldt. Gang nr "+hentNyeTeksterTæller);
        hentNyeTeksterTæller++;

            new AsyncTask() {

                @Override
                protected Object doInBackground(Object[] params) {
                    p("...ny tekstversion tilgængelig, kalder hentTeksterOnline()");

                    alleTekster = hentTeksterOnline("hentNyeTekster");

                    //nyt versionsnr gemmes i tjektekstversion

                    return null;
                }

                @Override
                protected void onPostExecute (Object o){
                    super.onPostExecute(o);

                    formaterLister(); //Kæde: metoden gemAlle..() kører i baggrunden
                }
            }.execute();

    }

    void formaterLister () {
        p("formaterLister () start");
        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] params) {

                itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                p("formaterLister () tekstlister fik erstattet afsnit");


                gemAlleNyeTekster();
            }
        }.execute();
    }


    private void gemAlleNyeTekster() {
        p("gemAlleNyeTekster() start");
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {



                //Gem o-tekster enkeltvis

                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                String nyBrødtekst = o1.brødtekst.replaceAll("\n", " ");
                o1.brødtekst = nyBrødtekst;
                IO.gemObj(o1, "otekst1", ctx);

                Tekst o2 = otekster.get(1);
                String nyBrødtekst2 = o2.brødtekst.replaceAll("\n", " ");

                o2.brødtekst = nyBrødtekst2;

                IO.gemObj(o2, "otekst2", ctx);

                Tekst o3 = otekster.get(2);
                String nyBrødtekst3 = o3.brødtekst.replaceAll("\n", " ");

                o3.brødtekst = nyBrødtekst3;

                IO.gemObj(o3, "otekst3", ctx);



                ArrayList<Integer> datoliste = new ArrayList();
                for (Tekst t: itekster)
                    datoliste.add(t.id_int);
                IO.gemObj(datoliste, "datoliste", ctx);



               // IO.gemObj(tempSynlige,"tempsynligeTekster", ctx);

              //  if(modenhed == MODENHED_MODEN) {  /// Kun når appen er moden og der derfor allerede er indlæst et sæt tekster.

                ArrayList<Tekst> tempHTekster = Util.erstatAfsnit(alleTekster[3]);
                ArrayList<String> tempHOverskrifter = new ArrayList<String>();
                for (Tekst t : tempHTekster)
                    tempHOverskrifter.add(t.overskrift.toUpperCase());


                htekster.clear();
                htekster = tempHTekster;
                hteksterOverskrifter.clear();
                hteksterOverskrifter = tempHOverskrifter;

                //-- Fyrer argument til event
                publishProgress(K.HTEKSTER_OPDATERET);

                synligeTekster.clear();
                if (tilstand.modenhed == K.MODENHED_HELT_FRISK || tilstand.modenhed == K.MODENHED_FØRSTE_DAG) synligeTekster.add(tempSynlige.get(0));
                else if (tilstand.modenhed == K.MODENHED_ANDEN_DAG) {
                        synligeTekster.add(tempSynlige.get(0));
                        synligeTekster.add(tempSynlige.get(1));
                    }
                else if (tilstand.modenhed == K.MODENHED_TREDJE_DAG) {
                    synligeTekster.add(tempSynlige.get(0));
                    synligeTekster.add(tempSynlige.get(1));
                    synligeTekster.add(tempSynlige.get(2));
                }
                else
                synligeTekster = tempSynlige;

                //-- Fyrer argument til event
                publishProgress(K.SYNLIGETEKSTER_OPDATERET);
                p("tjek synligetekster efter init: længde: "+synligeTekster.size());
                for (Tekst t : synligeTekster) p(t.toString());


                gemSynligeTekster();

                return null;
            }

            @Override //-- Modtager argument til event og fyrer det af
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int hændelse = (int) values[0];
                p("gemAlleNye..().Async.onprogress..()  modtog "+hændelse + " = "+ K.hændelsestekst(hændelse));

                lytter.givBesked(hændelse, "gemallenye, Htekster OG Synlige");
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (tilstand.modenhed == K.MODENHED_MODEN )// TODO: plus || HELT_FRISK ??
                    skalTekstlistenOpdateres("a.gemAlleNyeTekster()onPost"); ///KÆDE
                    gemAlleTeksterTilDisk();
                    p("gemAlleNyeTekster() slut");

            }
        }.execute();
    }

    private void gemAlleTeksterTilDisk() {
        if (itekster.size() >0)
        new AsyncTask(){
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

    public boolean skalTekstlistenOpdateres(String kaldtfra) {
        p("skalTekstlistenOpdateres("+kaldtfra+") start________");

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
                if (datoliste == null){
                    return false;
                }
                //-- Hvis datolisten er tom, er det fordi vi er nået til slutningen af skoleåret og der er ikke flere nye tekster
                else if (datoliste.size() == 0 ) {
					p("Datolisten er tom!!!");
                    return false;
                }

                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", tilstand.masterDato);
                dummyITekst.lavId();
                int idag = dummyITekst.id_int;
                p("Tjek I dummytekst id: "+dummyITekst.id_int);

				boolean iFundet = false;

                Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", tilstand.masterDato);
                dummyMTekst.lavId();
                int mIdag = dummyMTekst.id_int;
                p("Tjek M dummytekst id: "+dummyMTekst.id_int);


                ArrayList<Integer> slettes = new ArrayList<>();

                
               //TODO: Optimeres senere

                synligeDatoer.clear(); //todo: ikke nødvendigt at gemme den på disk. Ryd op i det
                for (int i = 0; i < datoliste.size(); i++) {
                    int tekstid = datoliste.get(i);

					if(tekstid < 300000000){
                        p("Tjek datoliste skalTOpdateres? I: " + tekstid);

                        if (tekstid >= idag) {


                                if (!iFundet && tekstid == idag) {
                                     p("Itekst eksakt match: "+tekstid);
                                    iFundet = true;
                                    if (i>1)synligeDatoer.add(datoliste.get(i - 2));
                                    if (i>0) synligeDatoer.add(datoliste.get(i - 1));
                                    synligeDatoer.add(datoliste.get(i));
                                }
                                else if (!iFundet) {
                                    p("I ineksakt match: dummy: "+dummyITekst.id_int+" | tekst: "+tekstid);
                                    iFundet = true;
                                    if (i>2) synligeDatoer.add(datoliste.get(i - 3));
                                    if (i>1) synligeDatoer.add(datoliste.get(i - 2));
                                    if (i>0) synligeDatoer.add(datoliste.get(i - 1));
                                    else synligeDatoer.add(datoliste.get(i));
                                }

                            }
                         //else slettes.add(tekstid);
                        }

					else { //if tekstid > 300000000
                        p("Tjek datoliste skalTOpdateres? M: " + tekstid);

                        //Todo: bør skrives om til at bruge tekst id i stedet for at hente alle tekster
                        Tekst t = (Tekst) IO.læsObj(""+datoliste.get(i), ctx);

                        if (alarmlogik.visMtekst(t.dato, tilstand.masterDato)) {
                            synligeDatoer.add(datoliste.get(i));
                            break; //Tillader ikke to m-tekster. KAN konflikte med notifikationer!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    	}
                        else if (Tid.fortid(t.dato))
                            slettes.add(datoliste.get(i));

					}
				}

                //-- Renser ud i gamle tekster
                if (synligeDatoer.size() >0) {
                    int ældsteI = synligeDatoer.get(0);
                    for (Integer i : datoliste) if (i < ældsteI) slettes.add(i);

                    for (Integer j : slettes) IO.føjTilGamle(j, ctx);
                }
                else //hvis listen er tom, er det fordi appen er et år gammel og der skal nulstilles
                        //sletAlt(); //Ikke længere relevant. Håndteres nu i erDerGået5DageOg...

              	for (Integer i : slettes) datoliste.remove(i);

                IO.gemObj( datoliste, "datoliste", ctx);



                p("skalTekstlistenOpdateres() async slut");

                return true;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

				boolean ny = false;


                if (synligeDatoer.size() != synligeTekster.size()) {
                    ny = true;
                    p("Listerne var forskellig længde: SynligeDatoer: "+synligeDatoer.size() + " SynligeTekster: "+synligeTekster.size());
                }
                else {
                    p("Listerne var samme længde: SynligeDatoer: "+synligeDatoer.size() + " SynligeTekster: "+synligeTekster.size());

                    for (int i = 0 ; i < synligeTekster.size(); i++){

                        int a = synligeDatoer.get(i);
                        int b  = synligeTekster.get(i).id_int;
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
                        p("dato: "+i);
                        synligeTekster.add( (Tekst) IO.læsObj(""+i,ctx));

                    }
                    lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "skaltekstlistenopdaetere() synlige UI-tråd: "+Thread.currentThread().getName());
                    gemSynligeTekster();
                    IO.gemObj(synligeDatoer, "synligeDatoer", ctx);

                    alarmlogik.rensUdIAlarmer(ctx);
                }
                else p("skalTekstlistenOpdateres Ingen ny synlige");
                p("skalTekstlistenOpdateres() slut");

            //-- (lidt sløv) måde at overføre returværdi. 'o' er falsk hvis datolisten er tom og der ikke er flere tekster at vise
                if ((boolean)o) pref.edit().putBoolean("nyTekst", ny).commit();
                else pref.edit().putBoolean("nyTekst", false).commit();


            }

        }.execute();

        return pref.getBoolean("nyTekst", false);
    }


    private ArrayList<Tekst> hentsynligeTekster(){
		//new Asynctask
		return (ArrayList<Tekst>) IO.læsObj("synligeTekster",this);
		
	}

	public void gemSynligeTekster(){
		//new async ?
        IO.gemObj(synligeTekster, "synligeTekster", this);
	}
	
	//kaldes kun fra baggrundstråd
    private ArrayList[]  hentTeksterOnline(String kaldtFra) {
        p("hentTeksterOnline() kaldt fra "+kaldtFra);
        String input = "";
        try {
            //Tjekker sprog:
            String sprog = Locale.getDefault().getLanguage();
            pref.edit().putString("sprog", sprog).commit();
            p("henter nye tekster på sprog: "+ sprog);
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
            ex.printStackTrace(); }

		return Util.parseXML(input, "hentTeksterOnline");

    }

    /**
     * Tjekker om der er ny version af tekster på nettet og fyrer en event hvis der er ny version
     * @param kaldtFra
     */
    private void tjekTekstversion(String kaldtFra) {
        p("tjekTekstversion() kaldt fra "+ kaldtFra);

         new AsyncTask() {

            int version = -1;


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

                if(!"".equals(versionstekst) && (versionstekst!=null))
                    version=Util.tryParseInt(versionstekst);
                else
                    p("Fejl: Hentet tekstversion null eller tom");

                p("version hentet på nettet: "+version);
                int gemtTekstversion = pref.getInt("tekstversion", 0);
                p("gemt tekstversion: "+gemtTekstversion);

                if (gemtTekstversion<version){
                    lytter.givBesked(K.NYE_TEKSTER_ONLINE, "tjektekstversion, nye online");
                    pref.edit().putInt("tekstversion", version).commit();
                }
            }
        }.execute();

    }



    /**
     * debugging: Tving ny app-dato
     * @param antaldage
     */
    public void rul(int antaldage) {
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

        if (alm == null)  alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
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
                lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "rul, synlige, UI-tråd? : "+Thread.currentThread().getName());
            }
        }, 10);
    }

    //-- 100% baggrund
    void indlæsHtekster(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                htekster = (ArrayList<Tekst>) IO.læsObj("htekster", ctx);
                for (Tekst t : htekster) hteksterOverskrifter.add(t.overskrift.toUpperCase());

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                lytter.givBesked(K.HTEKSTER_OPDATERET, "udvælgTekster, htekster, UI-tråd: "+Thread.currentThread().getName());
            }
        }.execute();

    }

    int findTekstnr (int id) {

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



    void t(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    void p(Object o){
        String kl = "A.";
        Util.p(kl+o);
    }

    @Override
    public void opdater(int hændelse) {

        if (hændelse == K.NYE_TEKSTER_ONLINE) {
            //if (tilstand.modenhed == K.MODENHED_MODEN || tilstand.modenhed == K.SOMMERFERIE)
                hentNyeTekster();
        }
        else if (hændelse == K.HTEKSTER_OPDATERET)
            tilstand.hteksterKlar= true;


    }








}

package dk.stbn.cool;

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

import com.crashlytics.android.Crashlytics;

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

import io.fabric.sdk.android.Fabric;

/**
 * Created by sune on 5/31/16.
 */
public class A extends Application implements Observatør{

    static A a;
	SharedPreferences pref;
    Context ctx;
    static AlarmManager alm;

//////////---------- TEKSTFRAGMENT/AKTIVITET DATA ----------//////////

    ArrayList<Tekst> synligeTekster = new ArrayList();  //bruges af pageradapteren

    ArrayList<Tekst> htekster = new ArrayList();
    ArrayList<String> hteksterOverskrifter = new ArrayList();

    public String henteurl = "http://www.lightspeople.net/sune/skole/tekster.xml";
    public String versionUrl = "http://www.lightspeople.net/sune/skole/version.txt";

    int sidstKendteVindueshøjde = 0;

//////////-------------------------//////////
	

//////////---------- UI TILSTAND / Lytterstystem ----------//////////
    static boolean hteksterKlar = false;
    boolean aktivitetenVises = false; //tjekker om aktiviteten vises før der er data at vise

//////////-------------------------//////////


	
//////////---------- APP TILSTAND ----------//////////

    public static DateTime masterDato;
    int modenhed = 0;
    final int MODENHED_HELT_FRISK = 0;
    final int MODENHED_FØRSTE_DAG = 1;
    final int MODENHED_ANDEN_DAG = 2;
    final int MODENHED_MODEN = 3;
    boolean tredjeDagFørsteGang = false;

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

    static boolean debugging = true;
    static boolean testtilstand = false;
    static boolean testtilstand_2 = false;
    public String henteurltest = "http://www.lightspeople.net/sune/skole/tekstertest.xml";
    int hændelsesId = 0;
    int skærmVendt = 0;
    int nyPageradapter = 0;


//////////-------------------------//////////

    /*-----------------------------noter

    * NeedToHave:
     *
     * Hvis der går lang tid mellem at appen er åben, når appen forbi de noti som er 'bestilt'
    * Lazy loading måske?
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
    * NiceToHave
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
        Fabric.with(this, new Crashlytics());

        Util.starttid = System.currentTimeMillis();
        p("oncreate() kaldt: UI-tråd: "+Thread.currentThread().getName());
        a= this;
        ctx=this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
       //til test
        //pref.edit().putInt("tekstversion",1).commit();
        //hertil
        Lyttersystem.nulstil();
        Lyttersystem.lyt(this);
        if (alm == null)  alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        else p("alarmManager eksisterer");

        masterDato = new DateTime();

        modenhed = tjekModenhed();
        tjekOpstart();
        p("oncreate() færdig. Modenhed: (0=frisk, 1=første, 2=anden, 3=moden) "+ modenhed);

    }//Oncreate færdig



    private void tjekOpstart() {

        if (modenhed > MODENHED_HELT_FRISK) {

            if (tredjeDagFørsteGang){
                p("tredje dag første gang!! ");
                //-- Viewpageren nulstilles (og viser sidste element i listen når det starter)
                pref.edit().putInt("seneste position", -1).commit();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        synligeTekster = (ArrayList<Tekst>) IO.læsObj("tempsynligeTekster", ctx);

                        //-- Sørger for at der ikke vises notifikationer for helt nye tekster
                        for (Tekst t : synligeTekster)
                            IO.føjTilGamle(t.id_int, ctx);

                        Util.opdaterKalender(ctx, "Application singleton");  //-- Kaldes ellers kun fra BootLytter
                        gemSynligeTekster();
                    }
                });

                tredjeDagFørsteGang = false;
            }
            else {
                synligeTekster = hentsynligeTekster();

            }
            tjekVisOtekst();

            //-- Hvis nu nogle h-tekster skulle være gemt
            int før = synligeTekster.size();
            for (Tekst t : synligeTekster) if (t.kategori.equals("h")) synligeTekster.remove(t);
            int efter = synligeTekster.size();
            if (før != efter) pref.edit().putInt("seneste position", -1).commit();
            synligeDatoer = (ArrayList<Integer>) IO.læsObj("synligeDatoer", ctx);
            indlæsHtekster();

        }

        if (modenhed == MODENHED_MODEN) {


            if (skalTekstlistenOpdateres()) {
                pref.edit().putInt("seneste position", -1).commit(); //Sætter ViewPagerens position til nyeste element
            }

            //-- Tjek om der er opdateringer til tekstene
            tjekTekstversion("tjekOpstart()");

        }

        else if (modenhed == MODENHED_HELT_FRISK) {
            p("oncreate() Modenhed: Helt frisk");
            initAllerFørsteGang();
            IO.gemObj(new DateTime(), "masterdato", this);
        }

        else if (modenhed == MODENHED_FØRSTE_DAG) {
            p("Dag 1, ikke første gang");
        }

        else if (modenhed == MODENHED_ANDEN_DAG) {
            p("Dag 2 ");
            if (pref.getBoolean("andenDagFørsteGang", true)) {
                p("Dag 2 første gang");
                Tekst t = (Tekst) IO.læsObj("otekst2", ctx);
                synligeTekster.add(t);
                gemSynligeTekster();
                pref.edit().putInt("seneste position", -1).commit();
                pref.edit().putBoolean("andenDagFørsteGang", false).commit();
            }
        }
        p("tjekOpstart() færdig");

        //singletonKlar = true;
    }

    private void initAllerFørsteGang(){

        tjekTekstversion("initAllerFørsteGang"); //køres for at få gemt versionsnummer i prefs første gang
        // må ikke kaldes fra baggrundstråd?

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                alleTekster = hentTeksterOnline("initAllerFørsteGang()");

                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if (alleTekster[0].size() == 0) prøvIgen();
                else {

                    //-- Denne kode burde egentlig stå i tjekModenhed() men er flyttet hertil så appen kun kan modnes hvis den får data første gang.
                        int idag = Util.lavDato(masterDato);

                        pref.edit()
                                .putInt("modenhed", MODENHED_FØRSTE_DAG)
                                .putInt("installationsdato", idag)
                                .commit();
                    //-- hertil

                        ArrayList<Tekst> otekster = alleTekster[0];
                        Tekst o1 = otekster.get(0);
                        String nyBrødtekst = o1.brødtekst.replaceAll("\n", " ");
                        o1.brødtekst = nyBrødtekst;
                        synligeTekster.add(o1);
                        p("Så er der O-tekst i array!");

                        if (aktivitetenVises)
                            Lyttersystem.givBesked(Lyttersystem.SYNLIGETEKSTER_OPDATERET, "initallerførste Otekst klar, UI-tråd: "+Thread.currentThread().getName()+ "id = ", hændelsesId++);
                        else
                            p("Aktiviteten blev klar EFTER at data blev klar");

                        IO.gemObj(o1, "otekst1", ctx);

                        initAllerFørsteGang_2();// async-kæde: ting der også kan gøres i baggrunden, men som er afhængige af værdier herfra
                    }
            }
        }.execute();
    }

    private void prøvIgen(){

        t("Fejl ved hentning af data. Prøver igen...\nTjek evt. om der er netforbindelse");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                p("Prøvigen() kaldt "+Thread.currentThread().getName());
                initAllerFørsteGang();

            }
        }, 5000);

    }

    private void initAllerFørsteGang_2() {

        p("initAllerFørsteGang_2() kaldt");

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {


                //-- gemmer h-tekster

                htekster = Util.erstatAfsnit(alleTekster[3]);

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift);

                publishProgress(1);

                IO.gemObj(htekster,"htekster",ctx);

                //-- Gemmer O-tekst nr 2 til næste gang

                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o2 = otekster.get(1);
                String nyBrødtekst2 = o2.brødtekst.replaceAll("\n", " ");

                o2.brødtekst = nyBrødtekst2;

                IO.gemObj(o2, "otekst2", ctx);

                //-- Gemmer resten
                itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));
                IO.gemObj(new ArrayList<Integer>(), "gamle", ctx);
                IO.gemObj(new ArrayList<Integer>(), "datoliste", ctx);
                IO.gemObj(new ArrayList<Integer>(), "synligeDatoer", ctx);



                return null;
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                p("Lyttersystem kaldes fra onProgress i initallerførstegang");
                Lyttersystem.givBesked(Lyttersystem.HTEKSTER_OPDATERET, "initAllerførste_2 htekst, forgrund: "+Thread.currentThread().getName()+ "id = ", hændelsesId++);

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                gemAlleNyeTekster(); // async-kæde: ting der også kan gøres i baggrunden, men som er afhængige af værdier fra denne metode
				
            }
        }.execute();
    }

    int hentNyeTeksterTæller = 1;
    void hentNyeTekster () {
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

                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", masterDato);
                dummyITekst.lavId();
                ArrayList<Integer> datoliste = new ArrayList();

                p("Tjek dummytekst id: "+dummyITekst.id_int);

                //itekster = Util.sorterStigende(alleTekster[1]);
                p("itekster længde: "+ itekster.size());

                ArrayList<Tekst> tempSynlige = new ArrayList<>();
                if (modenhed != MODENHED_MODEN){
                    tempSynlige.add((Tekst) IO.læsObj("otekst1", ctx));
                    tempSynlige.add((Tekst) IO.læsObj("otekst2", ctx));
                }

                boolean iFundet = false;

                for (int i = 0; i < itekster.size(); i++) {
                    Tekst itekst = itekster.get(i);
                    int tekstid = itekst.id_int;
                    p("Tjek Itekster: " + tekstid);
                    p("IdTekst: "+itekst.id);
                    datoliste.add(itekst.id_int);
                    IO.gemObj(itekst, "" + tekstid, ctx);
                    if (tekstid >= dummyITekst.id_int) {

                        if (!iFundet && tekstid == dummyITekst.id_int) {
                            p("Itekst eksakt match");
                            iFundet = true;

                            if (i>1)tempSynlige.add(itekster.get(i - 2));
                            if (i>0) tempSynlige.add(itekster.get(i - 1));
                            tempSynlige.add(itekster.get(i));


                        } else if (!iFundet) {
                            p("I ineksakt match");
                            iFundet = true;
                            if (i>2) tempSynlige.add(itekster.get(i - 3));
                            if (i>1)tempSynlige.add(itekster.get(i - 2));
                            if (i>0) tempSynlige.add(itekster.get(i - 1));
                            else tempSynlige.add(itekster.get(i));
                        }
                    }
                }

                Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", masterDato);
                dummyMTekst.lavId();
                p("Tjek M dummytekst id: "+dummyMTekst.id_int);


                boolean mFundet = false;

                //ArrayList<Tekst> mtekster = Util.sorterStigende(alleTekster[2]);

                p("Mtekster længde: "+mtekster.size());

                for (int i = 0; i < mtekster.size(); i++) {
                    Tekst mtekst = mtekster.get(i);
                    p("tjek mtekster: " + mtekst.id_int);
                    p("IdTekst: "+mtekst.id);
                    datoliste.add(mtekst.id_int);
                    IO.gemObj(mtekst, "" + mtekst.id_int, ctx);
                    if (mtekst.id_int >= dummyMTekst.id_int) {

                        if (!mFundet) {

                            if (mtekst.id_int == dummyMTekst.id_int) {
                                p("Eksakt match Mtekst");
                                tempSynlige.add(mtekst);

                            } else if (Util.visMtekst(mtekst.dato) ){
                                tempSynlige.add(mtekst);
                                p("Mtekst ineksakt match --");
                            }
                            mFundet = true;
                        }
                    }
                }

                IO.gemObj(datoliste, "datoliste", ctx);

                IO.gemObj(tempSynlige,"tempsynligeTekster", ctx);

                if(modenhed == MODENHED_MODEN) {  /// Kun når appen er moden og der derfor allerede er indlæst et sæt tekster.

                    ArrayList<Tekst> tempHTekster = Util.erstatAfsnit(alleTekster[3]);
                    ArrayList<String> TempHOverskrifter = new ArrayList<String>();
                    for (Tekst t : tempHTekster)
                        TempHOverskrifter.add(t.overskrift);


                    htekster.clear();
                    htekster = tempHTekster;
                    hteksterOverskrifter.clear();
                    hteksterOverskrifter = TempHOverskrifter;

                    //-- Fyrer argument til event
                    publishProgress(Lyttersystem.HTEKSTER_OPDATERET);

                    synligeTekster.clear();
                    synligeTekster = tempSynlige;

                    //-- Fyrer argument til event
                    publishProgress(Lyttersystem.SYNLIGETEKSTER_OPDATERET);
                    p("tjek synligetekster efter init:");
                    for (Tekst t : synligeTekster) p(t.toString());
                    gemSynligeTekster();

                }
                else IO.gemObj(tempSynlige,"tempsynligeTekster", ctx);

                gemSynligeTekster();
                p("gemAlleNyeTekster() slut");

                return null;
            }

            @Override //-- Modtager argument til event og fyrer det af
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                int hændelse = (int) values[0];
                p("gemAlleNye..().Async.onprogress..()  modtog "+hændelse + " = "+ Lyttersystem.hændelsestekst(hændelse));

                Lyttersystem.givBesked(hændelse, "gemallenye, Htekster OG Synlige, forgrund: "+Thread.currentThread().getName()+ "id = ", hændelsesId++);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (modenhed == MODENHED_MODEN)
                    skalTekstlistenOpdateres(); ///KÆDE
            }
        }.execute();
    }

    boolean skalTekstlistenOpdateres() {
        p("skalTekstlistenOpdateres() start________");

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

                //-- Hvis datolisten er tom, er det fordi vi er nået til slutningen af skoleåret og der er ikke flere nye tekster
                if (datoliste.size() == 0 || datoliste == null) {

                    return false;
                }

                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", masterDato);
                dummyITekst.lavId();
                int idag = dummyITekst.id_int;
                p("Tjek I dummytekst id: "+dummyITekst.id_int);

				boolean iFundet = false;

                Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", masterDato);
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

                        if (Util.visMtekst(t.dato)) {
                            synligeDatoer.add(datoliste.get(i));
                            break; //Tillader ikke to m-tekster. KAN konflikte med notifikationer!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    	}
                        else if (t.dato.isBeforeNow())
                            slettes.add(datoliste.get(i));

					}
				}

                //-- Renser ud i gamle tekster
                int ældsteI = synligeDatoer.get(0);
                for (Integer i : datoliste) if (i<ældsteI) slettes.add(i);

                for (Integer j : slettes) IO.føjTilGamle(j, ctx);


/* Igang: slet intents for gamle notifikationer)
                if (alm == null) alm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

                for (int i = 0 ; i < gamle.size() ; i++){
                    PendingIntent pi = PendingIntent.getBroadcast(ctx, gamle.get(i), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    pi.cancel();
                    alm.cancel(pi);
                }
*/
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
                    Lyttersystem.givBesked(Lyttersystem.SYNLIGETEKSTER_OPDATERET, "skaltekstlistenopdaetere() synlige UI-tråd: "+Thread.currentThread().getName()+ "id = ", hændelsesId++);
                    gemSynligeTekster();
                    IO.gemObj(synligeDatoer, "synligeDatoer", ctx);

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
            InputStream is = new URL(henteurl).openStream();
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

    private int tjekModenhed() {

        int moden = pref.getInt("modenhed", MODENHED_HELT_FRISK);

		if (moden == MODENHED_MODEN) return MODENHED_MODEN;
		
		int idag = Util.lavDato(masterDato);

        if (moden == MODENHED_HELT_FRISK) {
           //koden herfra er flyttet til initAllerFørsteGang() for at den ikke bliver kørt med mindre appen får hentet sine data

            return MODENHED_HELT_FRISK;
        }
        else if (moden == MODENHED_FØRSTE_DAG){
			int instDato  = pref.getInt("installationsdato", 0);
			if (idag == instDato) return MODENHED_FØRSTE_DAG;
			else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
                    pref.edit()
					.putInt("modenhed", MODENHED_ANDEN_DAG)
					.putInt("installationsdato2", idag)
					.apply();

                else  pref.edit()
                        .putInt("modenhed", MODENHED_ANDEN_DAG)
                        .putInt("installationsdato2", idag)
                        .commit();
				return MODENHED_ANDEN_DAG;
			}
		}
        else if (moden == MODENHED_ANDEN_DAG){
            int instDatoPlusEn = pref.getInt("installationsdato2", 0);
            if (idag == instDatoPlusEn) return MODENHED_ANDEN_DAG;
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
                    pref.edit().putInt("modenhed", MODENHED_MODEN).apply();
                else
                    pref.edit().putInt("modenhed", MODENHED_MODEN).commit();
                tredjeDagFørsteGang = true;
                p("tjekModenhed() Tredje dag første gang sat til true");
            }
            //return MODENHED_MODEN;
        }
        p("tjekModenhed() slut ");
        return MODENHED_MODEN;
    }

    private void tjekTekstversion(String kaldtFra) {
        p("tjekTekstversion() kaldt fra "+ kaldtFra);

         new AsyncTask() {

            int version = -1;


            @Override
            protected Object doInBackground(Object... tekst) {

                String versionStreng = "";

                try {
                    URL url = new URL(versionUrl);

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


                if(!"".equals(tekst) && (tekst!=null))
                    version=Util.tryParseInt((String) tekst);
                else
                    p("Fejl: Hentet tekstversion null eller tom");

                p("version hentet på nettet: "+version);
                int gemtTekstversion = pref.getInt("tekstversion", 0);
                p("gemt tekstversion: "+gemtTekstversion);

                if (modenhed == MODENHED_MODEN && gemtTekstversion<version) {
                    Lyttersystem.givBesked(Lyttersystem.NYE_TEKSTER_ONLINE, "tjektekstverion, nye online, UI-tråd: "+Thread.currentThread().getName()+ "id = ", hændelsesId++);
                    pref.edit().putInt("tekstversion", version).commit();
                }
            }
        }.execute();

    }

    void tjekVisOtekst(){ //TODO ikke færdig
        int synligeLængde = synligeTekster.size();

        if (synligeLængde == 3) return;
        else if (synligeLængde == 2){
            //tjek om der er en m-tekst
        }

        //todo
    }

    // - - Til test
    void rul (int antaldage) {
        p("rul() kaldt");

        masterDato = masterDato.plusDays(antaldage);
        if (testtilstand_2) masterDato = new DateTime();
        t("Idag er "+ masterDato.getDayOfMonth() + " / " + masterDato.getMonthOfYear() + " - " + masterDato.getYear());

        synligeTekster = new ArrayList();  //brugeas af pageradapteren
        htekster = new ArrayList();
        synligeDatoer = null;
        hteksterOverskrifter = new ArrayList();
        sidstKendteVindueshøjde = 0;


        modenhed = 0;
        tredjeDagFørsteGang = false;

        if (alm == null)  alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        else p("alarmManager eksisterer");

        //singletonKlar = false;

        modenhed = tjekModenhed();
        tjekOpstart();
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
                Lyttersystem.givBesked(Lyttersystem.SYNLIGETEKSTER_OPDATERET, "rul, synlige, UI-tråd? : "+Thread.currentThread().getName()+ "id = ", hændelsesId++);
            }
        }, 10);
    }

    void indlæsHtekster(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                htekster = (ArrayList<Tekst>) IO.læsObj("htekster", ctx);
                for (Tekst t : htekster) hteksterOverskrifter.add(t.overskrift);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                Lyttersystem.givBesked(Lyttersystem.HTEKSTER_OPDATERET, "tjekOpstart, htekster, UI-tråd: "+Thread.currentThread().getName()+ "id = ", hændelsesId++);
            }
        }.execute();

    }

    int findTekstnr (int id) {

        for (int i = 0; i < synligeTekster.size(); i++)
            if (id == synligeTekster.get(i).id_int) return i;

        return -1;
    }

    //-- Htekster har samme id_int
    int findTekstnr (String overskrift) {
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

        if (hændelse == Lyttersystem.NYE_TEKSTER_ONLINE) {
            if (modenhed == MODENHED_MODEN) hentNyeTekster();
        }

    }
}

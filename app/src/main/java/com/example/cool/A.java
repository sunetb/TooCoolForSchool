package com.example.cool;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.joda.time.DateTime;

/**
 * Created by sune on 5/31/16.
 */
public class A extends Application {

    static A a;
	SharedPreferences pref;
    Context ctx;
    static AlarmManager alm;

//////////---------- TEKSTFRAGMENT/AKTIVITET DATA ----------//////////


    ArrayList<Tekst> synligeTekster = new ArrayList();  //brugeas af pageradapteren
    ArrayList<Tekst> htekster = new ArrayList();
    ArrayList<Integer> synligeDatoer;
    ArrayList<String> hteksterOverskrifter = new ArrayList();


    public String henteurl = "http://www.lightspeople.net/sune/skole/tekster.xml";
    public String versionUrl = "http://www.lightspeople.net/sune/skole/version.txt";

    int sidstKendteVindueshøjde = 0;

//////////-------------------------//////////
	

//////////---------- UI TILSTAND / Lytterstystem ----------//////////
	ArrayList<Observatør> observatører = new ArrayList<>();
    void lyt(Observatør o) {observatører.add(o);}
    void afregistrer(Observatør o) {observatører.remove(o);}
    void givBesked () {for (Observatør o: observatører) o.opdater();}

    boolean aktivitetenVises = false; //tjekker om aktiviteten vises før der er data at vise

//////////-------------------------//////////


	
//////////---------- APP TILSTAND ----------//////////
	
    boolean findesNyTekst = false;
    int modenhed = 0;
    final int MODENHED_HELT_FRISK = 0;
    final int MODENHED_FØRSTE_DAG = 1;
    final int MODENHED_ANDEN_DAG = 2;
    final int MODENHED_MODEN = 3;

    boolean tredjeDagFørsteGang = false;

    static boolean singletonKlar = false;
//////////-------------------------//////////
	
	

//////////---------- MIDLERTIDIGE DATA ----------//////////
	

	private ArrayList[] alleTekster;
	private ArrayList<Tekst> itekster;
    private ArrayList<Tekst> mtekster;

//////////-------------------------//////////
	
	
////TEST / DEBUGGING////////////////////////////////////////////////
    //String htmlStreng =  "<!DOCTYPE html ><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>o17092016.txt</title></head><body><h2 style=\"text-align: center;\">Overskift</h2>Prødtekst bla bla bla<br>loårwij vfvåoijdsd åvoij såoigjb åfsoji gvåoifjob idfj obåijfd boåijfdobigj fdoibjåfdoijgb åfdoij båojfdpi gbåjopidf åboji dfåobji dfåojibåfdjop båpdfjo hbåpjiodf <span style=\"font-weight: bold;\">fed tekst inde i teksten</span>oæbhijd fåpjoib fdoji bådfoipj båpdoijo bhåpjoi dfåppjoihb åpdjfoihbåijpod fåbboijp dfåojhip båpdfjio cgåpoijb ådfpojg cbpojv dfåpgojbojfd gcb<br><br>&nbsp;vpfdoijb åpfjdoi bpåfodj ågbpoijfd ågpoj båfdopijgbbåpjo fpågojbpofjd gbvjpodfø gopbjk fodjx bpöjkf gxobkj vfokgxbo fkxgpobk pokgxxbpok fpovkb ogdfk gpbök fdpogkvcb pofdkg bcpok dfogxkb dofkg bc<br><br style=\"font-weight: bold;\"><div style=\"text-align: center;\"><span style=\"font-weight: bold;\">denne line skal være fed og centreret</span><br></div><br>OG her kommer mere almindelig tekst doijf våorwij fvåojiper pbogvijerrpoijvbgv åjeroip bgåiopjet gvåoipje tpbvogj etpogbkj etåpojgb petojgboijet gboijet gboåij tepogbj etåoibjt <br><br><div style=\"text-align: center; font-weight: bold;\">Og her fler lininer som er fed og cwentreret <br>denne er med <br>denne ogr også med<br></div><br>OG mere helt almindelig tekst åoij åoijgb dåfjiop bpoj ghbojgdj&#7719;pokg<br>\"gdøopbkjk pokjbh prgonh pfoknh rgonkn hrf<br><br></body></html>";

    public static String debugmsg = "<!DOCTYPE html ><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>o17092016.txt</title></head><body  style=\"color: white; background-color: black;\">";
    public static String hale = "</body></html>";



  //  boolean hurtigModning = false; // til test på enhed
    DateTime masterDato;

    static boolean debugging = true; //-- omdefiner knapperne "Del" og "Kontakt" i hovedaktiviteteten

//////////-------------------------//////////

    /*-----------------------------noter

    * NeedToHave:
     * tjek i aktiviteten om den er åbnet nra noti. vis tilvvarende tekst på skærmen
    *
    * 
    * 
    *
    * 
	
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
    *
    *

    *    int modenhed = 0;
    final int MODENHED_HELT_FRISK = 0;
    final int MODENHED_FØRSTE_DAG = 1;
    final int MODENHED_ANDEN_DAG = 2;
    final int MODENHED_MODEN = 3;
    * */
	

    @Override
    public void onCreate() {
        super.onCreate();
        Util.starttid = System.currentTimeMillis();
        p("oncreate() kaldt");
        a= this;
        ctx=this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (alm == null)  alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        else p("alarmManager eksisterer");

        masterDato = new DateTime();
        //if (hurtigModning) masterDato = new DateTime(2015, 10, 1, 0,0);

        modenhed = tjekModenhed();
        tjekOpstart();
        p("Modenhed: (0=frisk, 1=første, 2=anden, 3=moden) "+ modenhed);

        if (debugging) pref.edit().putBoolean("vistestdialog", true).commit();

    }//Oncreate færdig

    private void tjekOpstart() {


        if (modenhed > MODENHED_HELT_FRISK) {

            if (tredjeDagFørsteGang){
                p("tredje dag første gang!! ");
                synligeTekster = (ArrayList<Tekst>) IO.læsObj("tempsynligeTekster", this);


                //--hvis nu nogle h-tekster skulle være gemt
                //    for (Tekst t : synligeTekster) if (t.kategori.equals("h")) synligeTekster.remove(t);

                p("Synligetekster længde: "+ synligeTekster.size());
                pref.edit().putInt("seneste position", -1).commit();
                //test
                //Tekst testtt = new Tekst("test", "test", "i", new DateTime().plusSeconds(60));
                //Util.startAlarm(this,testtt);
                //evt i async:
                Util.opdaterKalender(ctx, "Application singleton");
                gemSynligeTekster();
                //hertil

                tredjeDagFørsteGang = false;
            }
            else {
                synligeTekster = hentsynligeTekster();

            }

            //--hvis nu nogle h-tekster skulle være gemt
            int før = synligeTekster.size();
            for (Tekst t : synligeTekster) if (t.kategori.equals("h")) synligeTekster.remove(t);
            int efter = synligeTekster.size();
            if (før != efter) pref.edit().putInt("seneste position", -1).commit();

            synligeDatoer = (ArrayList<Integer>) IO.læsObj("synligeDatoer", ctx);

            //-- Tjek om der er opdateringer til tekstene
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    htekster = (ArrayList<Tekst>) IO.læsObj("htekster", ctx);
                    for (Tekst t : htekster) hteksterOverskrifter.add(t.overskrift);
                    findesNyTekst = tjekTekstversion();
                    p("Ny tekstversion? : " + findesNyTekst);
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);

                    //TODO: if (findesNyTekst) {
                    // hentonline
                    // gemallenye
                    // opdater adapter (lyttersystem)
                    // }
                    ///for (Tekst t : htekster) p("Tjekker Htekster: " + t.toString(1));
                }
            }.execute();

        }

        if (modenhed == MODENHED_MODEN) {


            if (skalTekstlistenOpdateres())  {
                pref.edit().putInt("seneste position", -1).commit();
            }



            new AsyncTask() {

                @Override
                protected Object doInBackground(Object[] params) {
                    findesNyTekst = tjekTekstversion();
                    p("Findes ny tekstversion? "+findesNyTekst);
                    if (findesNyTekst) {
                        alleTekster = hentTeksterOnline();
                        //TODO: erstat synlige. giv adapter besked. gem synlige.
                        //nyt versionsnr gemmes i tjektekstversion

                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    p("Ny tekstversion? : "+findesNyTekst);
                }
            }.execute();

        }

        else if (modenhed == MODENHED_HELT_FRISK) {
            p("oncreate() Modenhed: Helt frisk");
            initAllerFørsteGang();
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
        p("onCreate færdig");
        singletonKlar = true;



    }

    private void initAllerFørsteGang(){

        IO.gemObj(new ArrayList<Integer>(), "gamle", this);
        IO.gemObj(new ArrayList<Integer>(), "datoliste", this);
        IO.gemObj(new ArrayList<Integer>(), "synligeDatoer", this);


        new AsyncTask() {


            @Override
            protected Object doInBackground(Object[] params) {
                alleTekster = hentTeksterOnline();
                findesNyTekst = tjekTekstversion(); //køres for at få gemt versionsnummer i prefs første gang
                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                    ArrayList<Tekst> otekster = alleTekster[0];
                    Tekst o1 = otekster.get(0);
                    String nyBrødtekst = o1.brødtekst.replaceAll("\n", " ");
                    o1.brødtekst = nyBrødtekst;
                    synligeTekster.add(o1);
                    p("Så er der O-tekst i array!");

                    if(aktivitetenVises) givBesked();
                    IO.gemObj(o1, "otekst1", ctx);

                    //Gemmer O-tekst nr 2 til næste gang
                    Tekst o2 = otekster.get(1);
                    String nyBrødtekst2 = o2.brødtekst.replaceAll("\n", " ");

                    o1.brødtekst = nyBrødtekst2;

                    IO.gemObj(o2, "otekst2", ctx);

                fortsæt();// async-kæde: ting der også kan gøres i baggrunden, men som er afhængige af værdier fra denne metode
            }
        }.execute();
    }

    private void fortsæt() {

        p("fortsæt() kaldt");

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                //gemSynligeTekster();
                itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));
                htekster = alleTekster[3];

                for (Tekst t : htekster) hteksterOverskrifter.add(t.overskrift);

                //gemmer h-tekster
                IO.gemObj(Util.erstatAfsnit(alleTekster[3]),"htekster",ctx);



                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                gemAlleNyeTekster(); // async-kæde: ting der også kan gøres i baggrunden, men som er afhængige af værdier fra denne metode
				
            }
        }.execute();
    }

    private void gemAlleNyeTekster() {
        p("gemAlleNyeTekster() start");
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                //ArrayList<Tekst> itekster;

                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", masterDato);
                dummyITekst.lavId();
                ArrayList<Integer> datoliste = new ArrayList();

                p("Tjek dummytekst id: "+dummyITekst.id_int);

                //itekster = Util.sorterStigende(alleTekster[1]);
                p("itekster længde: "+ itekster.size());

                ArrayList<Tekst> tempSynlige = new ArrayList<>();

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

                if(modenhed == MODENHED_MODEN) {
                    synligeTekster.clear();
                    synligeTekster = tempSynlige;
                    //tempSynlige = null;
                    p("tjek synligetekster efter init:");
                    for (Tekst t : synligeTekster) p(t.toString());

                }
                else gemSynligeTekster();
                p("gemAlleNyeTekster() slut");

                return null;
            }

        }.execute();
    }

    boolean skalTekstlistenOpdateres() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
				
                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", masterDato);
                dummyITekst.lavId();

                ArrayList<Integer> datoliste = (ArrayList<Integer>) IO.læsObj("datoliste", ctx); //hvis denne gøres global, kan den initalisteres når som helst - dvs igså tidligere.

                int idag = dummyITekst.id_int;
				
				Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", masterDato);
                dummyMTekst.lavId();
				
				int mIdag = dummyMTekst.id_int;
                p("Tjek I dummytekst id: "+dummyITekst.id_int);
                p("Tjek M dummytekst id: "+dummyMTekst.id_int);

                boolean mFundet = false;
				boolean iFundet = false;
				
               // ArrayList<Integer> tempSynlige = new ArrayList<>();

                ArrayList<Integer> slettes = new ArrayList<>();

                
               //TODO: Optimeres senere
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

              	for (Integer i : slettes) datoliste.remove(i);

                IO.gemObj( datoliste, "datoliste", ctx);

                p("skalTekstlistenOpdateres() async slut");

                return null;
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
                    for (int i = 0 ; i < synligeTekster.size(); i++){

                        int a = synligeDatoer.get(i);
                        int b  = synligeTekster.get(i).id_int;
                        if (a != b) {
                            p("Listerne var samme længde: SynligeDatoer: "+synligeDatoer.size() + " SynligeTekster: "+synligeTekster.size());
                            ny = true;
                            p("tjekker listerne: ............................................");
                            for (Integer q : synligeDatoer) p("synligeDatoer: "+q);
                            for (Tekst t : synligeTekster) p("synligeTekster: "+t.id_int);

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
                    givBesked();
                    gemSynligeTekster();

                }
                else p("skalTekstlistenOpdateres Ingen ny synlige");
                p("skalTekstlistenOpdateres() slut");

            //Gider ikke parametrisere
                pref.edit().putBoolean("nyTekst", ny).commit();

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
    private ArrayList[]  hentTeksterOnline() {
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
		
		int idag = Util.lavDato(new Date());

        if (moden == MODENHED_HELT_FRISK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                pref.edit()
                    .putInt("modenhed", MODENHED_FØRSTE_DAG)
                    .putInt("installationsdato", idag)
                    .apply();
            }
            else {
                pref.edit()
                        .putInt("modenhed", MODENHED_FØRSTE_DAG)
                        .putInt("installationsdato", idag)
                        .commit();
            }

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
                else pref.edit().putInt("modenhed", MODENHED_MODEN).commit();
                tredjeDagFørsteGang = true;
                p("tjekModenhed() Tredje dag første gang sat til true");
            }
            //return MODENHED_MODEN;
        }
        p("tjekModenhed() slut ");
        return MODENHED_MODEN;
    }

    private boolean tjekTekstversion() {

        int gemtTekstversion = pref.getInt("tekstversion", 0);

        int version = -1;
        String versionStreng = "";

        try {

            URL url = new URL(versionUrl);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String str;
            while ((str = in.readLine()) != null) {
                versionStreng+= str;
            }

            p("Version hentet fra nettet: "+versionStreng);
            in.close();

        } catch (MalformedURLException me) {
            me.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!"".equals(versionStreng) && (versionStreng != null))
            version = Util.tryParseInt(versionStreng);
        else p("Fejl: Hentet teksteversion null eller tom");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            pref.edit().putInt("tekstversion", version).apply();
        else pref.edit().putInt("tekstversion", version).commit();
        return (gemtTekstversion < version);
    }

    void nulstil () {

        synligeTekster = new ArrayList();  //brugeas af pageradapteren
        htekster = new ArrayList();
        synligeDatoer = null;
        hteksterOverskrifter = new ArrayList();
        sidstKendteVindueshøjde = 0;

        aktivitetenVises = false; //tjekker om aktiviteten vises før der er data at vise

        findesNyTekst = false;
        modenhed = 0;
        tredjeDagFørsteGang = false;

        if (alm == null)  alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        else p("alarmManager eksisterer");

        singletonKlar = false;

        masterDato = new DateTime();
        modenhed = tjekModenhed();
        tjekOpstart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                givBesked();
            }
        }, 100);



    }

    int findTekstnr (int id) {

        for (int i = 0; i < synligeTekster.size(); i++)
            if (id == synligeTekster.get(i).id_int) return i;

        return -1;
    }

    void t(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    void p(Object o){
        String kl = "A.";
        Util.p(kl+o);
    }

}

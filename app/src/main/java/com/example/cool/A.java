package com.example.cool;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

import android.support.v4.app.Fragment;

import org.joda.time.DateTime;

/**
 * Created by sune on 5/31/16.
 */
public class A extends Application {

    static A a;
	SharedPreferences pref;
	Context ctx;

//////////---------- TEKSTFRAGMENT DATA ----------//////////


    ArrayList<Tekst> synligeTekster = new ArrayList();
    ArrayList<Tekst> htekster = new ArrayList();

    public String henteurl = "http://www.lightspeople.net/sune/skole/teksterny.xml";
    public String versionUrl = "http://www.lightspeople.net/sune/skole/version.txt";

//////////-------------------------//////////
	

//////////---------- VISES NU ----------//////////
	

	//todo  scroll, zoom, 
//////////-------------------------//////////
	
//////////---------- TILSTAND ----------//////////
	
    boolean findesNyTekst = false;
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

//////////-------------------------//////////
	
	
////TEST / DEBUGGING////////////////////////////////////////////////
    //String htmlStreng =  "<!DOCTYPE html ><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>o17092016.txt</title></head><body><h2 style=\"text-align: center;\">Overskift</h2>Prødtekst bla bla bla<br>loårwij vfvåoijdsd åvoij såoigjb åfsoji gvåoifjob idfj obåijfd boåijfdobigj fdoibjåfdoijgb åfdoij båojfdpi gbåjopidf åboji dfåobji dfåojibåfdjop båpdfjo hbåpjiodf <span style=\"font-weight: bold;\">fed tekst inde i teksten</span>oæbhijd fåpjoib fdoji bådfoipj båpdoijo bhåpjoi dfåppjoihb åpdjfoihbåijpod fåbboijp dfåojhip båpdfjio cgåpoijb ådfpojg cbpojv dfåpgojbojfd gcb<br><br>&nbsp;vpfdoijb åpfjdoi bpåfodj ågbpoijfd ågpoj båfdopijgbbåpjo fpågojbpofjd gbvjpodfø gopbjk fodjx bpöjkf gxobkj vfokgxbo fkxgpobk pokgxxbpok fpovkb ogdfk gpbök fdpogkvcb pofdkg bcpok dfogxkb dofkg bc<br><br style=\"font-weight: bold;\"><div style=\"text-align: center;\"><span style=\"font-weight: bold;\">denne line skal være fed og centreret</span><br></div><br>OG her kommer mere almindelig tekst doijf våorwij fvåojiper pbogvijerrpoijvbgv åjeroip bgåiopjet gvåoipje tpbvogj etpogbkj etåpojgb petojgboijet gboijet gboåij tepogbj etåoibjt <br><br><div style=\"text-align: center; font-weight: bold;\">Og her fler lininer som er fed og cwentreret <br>denne er med <br>denne ogr også med<br></div><br>OG mere helt almindelig tekst åoij åoijgb dåfjiop bpoj ghbojgdj&#7719;pokg<br>\"gdøopbkjk pokjbh prgonh pfoknh rgonkn hrf<br><br></body></html>";

    public static String debugmsg = "<!DOCTYPE html ><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>o17092016.txt</title></head><body  style=\"color: white; background-color: black;\">";
    public static String hale = "</body></html>";
    DateTime okt15 = new DateTime(2015, 10, 2, 1,0);


    boolean bypassOtekster = false;

//////////-------------------------//////////
	

    @Override
    public void onCreate() {
        super.onCreate();
        Util.starttid = System.currentTimeMillis();
        p("A.oncreate() kaldt");
        a= this;
        ctx=this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (bypassOtekster) modenhed = bypassOpstart(); //test
        else
        modenhed = tjekModenhed();

        p("Modenhed: (0=frisk, 1=første, 2=anden, 3=moden) "+ modenhed);


        if (modenhed > MODENHED_HELT_FRISK) {
            if (bypassOtekster) {

            }
            else if (tredjeDagFørsteGang){
                 p("tredje dag første gang!! ");
                synligeTekster = Util.hentTekstliste("tempsynligeTekster", this);
                p("Synligetekster længde: "+ synligeTekster.size());
                pref.edit().putInt("seneste position", synligeTekster.size()-1).apply();


                gemSynligeTekster();
                tredjeDagFørsteGang = false;

            }
            else {
                ArrayList<Tekst> temp = hentsynligeTekster();
                if (temp != null) {
                    synligeTekster = temp;
                    //temp=null;

                }
                else p("gemt synligetekster var null");
            }

            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    htekster = Util.hentTekstliste("htekster", ctx);
                    findesNyTekst = tjekTekstversion();
                    p("Ny tekstversion? : " + findesNyTekst);
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);

                    ///for (Tekst t : htekster) p("Tjekker Htekster: " + t.toString(1));
                }
            }.execute();

        }

        if (modenhed == MODENHED_MODEN) {

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
                    Util.gemTekst(o1, "otekst1", ctx);


                    //Gemmer O-tekst nr 2 til næste gang
                    Tekst o2 = otekster.get(1);
                    String nyBrødtekst2 = o2.brødtekst.replaceAll("\n", " ");

                    o1.brødtekst = nyBrødtekst2;


                    Util.gemTekst(o2,"otekst2",ctx);

                    fortsæt();

                }

				

            }.execute();


        }

        else if (modenhed == MODENHED_FØRSTE_DAG) {
            p("Dag 1, anden gang");
        }

        else if (modenhed == MODENHED_ANDEN_DAG) {
            p("Dag 2 ");
            if (pref.getBoolean("andenDagFørsteGang", true)) {
                p("Dag 2 første gang");
                Tekst t = Util.hentTekst("otekst2", ctx);
                synligeTekster.add(t);
                gemSynligeTekster();
                pref.edit().putBoolean("andenDagFørsteGang", false).apply();
            }
        }

    }



    private void fortsæt() {

        p("fortsæt() kaldt");

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                //gemSynligeTekster();
                itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));


                //gemmer h-tekster
                Util.gemTekstliste(Util.erstatAfsnit(alleTekster[3]),"htekster",ctx);

                 return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                gemAlleNyeTekster();
            }
        }.execute();
    }



    private void gemAlleNyeTekster() {
        p("gemAlleNyeTekster() start");
        new AsyncTask() {



            @Override
            protected Object doInBackground(Object[] params) {

                //ArrayList<Tekst> itekster;

                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", new DateTime());
                dummyITekst.lavId();
                p("Tjek dummytekst id: "+dummyITekst.tekstid);

                //itekster = Util.sorterStigende(alleTekster[1]);
                p("itekster længde: "+ itekster.size());

                Set<String> alleteksterSæt = new HashSet<String>();

                ArrayList<Tekst> tempSynlige = new ArrayList<>();

                boolean iFundet = false;

                for (int i = 0; i < itekster.size(); i++) {
                    Tekst itekst = itekster.get(i);
                    int tekstid = itekst.tekstid;
                    p("Tjek Itekster: " + tekstid);
                    p("IdTekst: "+itekst.idTekst);

                    if (tekstid >= dummyITekst.tekstid) {
                        Util.gemTekst(itekst, "" + tekstid, ctx);
                        alleteksterSæt.add("" + tekstid);



                        if (!iFundet && tekstid == dummyITekst.tekstid) {
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

                Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", new DateTime());
                dummyITekst.lavId();
                p("Tjek dummytekst id: "+dummyITekst.tekstid);


                boolean mFundet = false;

                //ArrayList<Tekst> mtekster = Util.sorterStigende(alleTekster[2]);

                p("Mtekster længde: "+mtekster.size());

                for (int i = 0; i < mtekster.size(); i++) {
                    Tekst mtekst = mtekster.get(i);
                    p("tjek mtekster: " + mtekst.tekstid);
                    p("IdTekst: "+mtekst.idTekst);

                    if (mtekst.tekstid >= dummyMTekst.tekstid) {
                        Util.gemTekst(mtekst, "" + mtekst.tekstid, ctx);
                        alleteksterSæt.add("" + mtekst.tekstid);

                        if (!mFundet) {

                            if (mtekst.tekstid == dummyMTekst.tekstid) {
                                p("Eksakt match Mtekst");
                                tempSynlige.add(mtekst);


                            } else {
                                tempSynlige.add(mtekst);
                                p("ineksakt match mtekst");
                            }
                            mFundet = true;
                        }

                    }
                }

                pref.edit().putStringSet("alletekster", alleteksterSæt).apply();
                Util.gemTekstliste(tempSynlige,"tempsynligeTekster",ctx );

                if(modenhed == MODENHED_MODEN) {
                    synligeTekster.clear();
                    synligeTekster = tempSynlige;
                    tempSynlige = null;
                    p("tjek synligetekster efer init:");
                    //for (Tekst t : synligeTekster) p(t.toString());
                }
                p("gemAlleNyeTekster() slut");

                return null;
            }
        }.execute();
    }

    private boolean skalTekstlistenOpdateres() {


        return false;
    }

    private ArrayList<Tekst> hentsynligeTekster(){
		//new Asynctask
		return Util.hentTekstliste("synligeTekster",this);
		
	}
	
	public void gemSynligeTekster(){
		//new async ?
		Util.gemTekstliste(synligeTekster, "synligeTekster", this);
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
		
		//test: bypass
		//if (true) return 0;
		
//IKKE TESTET
        int moden = pref.getInt("modenhed", MODENHED_HELT_FRISK);

		if (moden == MODENHED_MODEN) return MODENHED_MODEN;
		
		int idag = Util.lavDato(new Date());
		
        if (moden == MODENHED_HELT_FRISK) {
			pref.edit()
				.putInt("modenhed", MODENHED_FØRSTE_DAG)
				.putInt("installationsdato", idag)
				.apply();
			
            return 0;
        }
        else if (moden == MODENHED_FØRSTE_DAG){
			int instDato  = pref.getInt("installationsdato", 0);
			if (idag == instDato) return MODENHED_FØRSTE_DAG;
			else {
				pref.edit()
					.putInt("modenhed", MODENHED_ANDEN_DAG)
					.putInt("installationsdato2", idag)
					.apply();
				return 2;
			}
		}
        else if (moden == MODENHED_ANDEN_DAG){
            int instDatoPlusEn = pref.getInt("installationsdato2", 0);
            if (idag == instDatoPlusEn) return MODENHED_ANDEN_DAG;
            else {
                pref.edit().putInt("modenhed", MODENHED_MODEN).apply();
                tredjeDagFørsteGang = true;
            }
            return MODENHED_MODEN;
        }

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
        else t("Fejl: Hentet teksteversion null eller tom");

        pref.edit().putInt("tekstversion", version).apply();

        return (gemtTekstversion < version);
    }

    private int bypassOpstart() {



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




                new AsyncTask() {

                    @Override
                    protected Object doInBackground(Object[] params) {
                        //gemSynligeTekster();
                        itekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[1]));
                        mtekster = Util.sorterStigende(Util.erstatAfsnit(alleTekster[2]));


                        //gemmer h-tekster
                        Util.gemTekstliste(Util.erstatAfsnit(alleTekster[3]),"htekster",ctx);

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        p("gemAlleNyeTekster() start");
                        new AsyncTask() {



                            @Override
                            protected Object doInBackground(Object[] params) {

                                //ArrayList<Tekst> itekster;

                                Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", okt15);
                                dummyITekst.lavId();
                                p("Tjek dummytekst id: "+dummyITekst.tekstid);

                                //itekster = Util.sorterStigende(alleTekster[1]);
                                p("itekster længde: "+ itekster.size());

                                Set<String> alleteksterSæt = new HashSet<String>();

                                ArrayList<Tekst> tempSynlige = new ArrayList<>();

                                boolean iFundet = false;

                                for (int i = 0; i < itekster.size(); i++) {
                                    Tekst itekst = itekster.get(i);
                                    int tekstid = itekst.tekstid;
                                    p("Tjek Itekster: " + tekstid);
                                    p("IdTekst: "+itekst.idTekst);

                                    if (tekstid >= dummyITekst.tekstid) {
                                        Util.gemTekst(itekst, "" + tekstid, ctx);
                                        alleteksterSæt.add("" + tekstid);



                                        if (!iFundet && tekstid == dummyITekst.tekstid) {
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

                                Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", okt15);
                                dummyITekst.lavId();
                                p("Tjek dummytekst id: "+dummyITekst.tekstid);


                                boolean mFundet = false;

                                //ArrayList<Tekst> mtekster = Util.sorterStigende(alleTekster[2]);

                                p("Mtekster længde: "+mtekster.size());

                                for (int i = 0; i < mtekster.size(); i++) {
                                    Tekst mtekst = mtekster.get(i);
                                    p("tjek mtekster: " + mtekst.tekstid);
                                    p("IdTekst: "+mtekst.idTekst);

                                    if (mtekst.tekstid >= dummyMTekst.tekstid) {
                                        Util.gemTekst(mtekst, "" + mtekst.tekstid, ctx);
                                        alleteksterSæt.add("" + mtekst.tekstid);

                                        if (!mFundet) {

                                            if (mtekst.tekstid == dummyMTekst.tekstid) {
                                                p("Eksakt match Mtekst");
                                                tempSynlige.add(mtekst);


                                            } else {
                                                tempSynlige.add(mtekst);
                                                p("ineksakt match mtekst");
                                            }
                                            mFundet = true;
                                        }

                                    }
                                }

                                pref.edit().putStringSet("alletekster", alleteksterSæt).apply();
                                Util.gemTekstliste(tempSynlige,"synligeTekster",ctx );

                                if(modenhed == MODENHED_MODEN) {
                                    synligeTekster.clear();
                                    synligeTekster = tempSynlige;
                                    //tempSynlige = null;
                                    p("tjek synligetekster efer init:");
                                    //for (Tekst t : synligeTekster) p(t.toString());
                                }
                                p("gemAlleNyeTekster() slut");

                                return null;
                            }
                        }.execute();


                    }
                }.execute();



            }



        }.execute();



        return MODENHED_MODEN;
    }

    void t(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    void p(Object o){
        String kl = "A.";
        kl += o +"   #t:" + Util.tid();
        System.out.println(kl);
        A.debugmsg += kl +"<br>";
    }

}

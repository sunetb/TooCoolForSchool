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
	
	int antalTekster = 0;
	
    ArrayList<String> tekster = new ArrayList<>();
    ArrayList<String> overskrifter = new ArrayList<>();
    ArrayList<String> links = new ArrayList<>();

    ArrayList<Tekst> synligeTekster = new ArrayList();
    ArrayList<Tekst> htekster = new ArrayList();


    public String henteurl = "http://www.lightspeople.net/sune/skole/teksterny.xml";
    public String versionUrl = "http://www.lightspeople.net/sune/skole/version.txt";

	
    
//////////-------------------------//////////
	

//////////---------- VISES NU ----------//////////
	
	String overskrift="";
	String brødtekst ="";
	int side = -1;
	static WebView wa;

	//todo  scroll, zoom, 
//////////-------------------------//////////
	
//////////---------- TILSTAND ----------//////////
	
    boolean findesNyTekst = false;
    final int MODENHED_HELT_FRISK = 0;
    final int MODENHED_FØRSTE_DAG = 1;
    final int MODENHED_ANDEN_DAG = 2;
    final int MODENHED_MODEN = 3;
//////////-------------------------//////////
	
	

//////////---------- MIDLERTIDIGE DATA ----------//////////
	

	ArrayList[] alleTekster;
	

//////////-------------------------//////////
	
	
////TEST////////////////////////////////////////////////
    String htmlStreng =  "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>o17092016.txt</title></head><body><h2 style=\"text-align: center;\">Overskift</h2>Prødtekst bla bla bla<br>loårwij vfvåoijdsd åvoij såoigjb åfsoji gvåoifjob idfj obåijfd boåijfdobigj fdoibjåfdoijgb åfdoij båojfdpi gbåjopidf åboji dfåobji dfåojibåfdjop båpdfjo hbåpjiodf <span style=\"font-weight: bold;\">fed tekst inde i teksten</span>oæbhijd fåpjoib fdoji bådfoipj båpdoijo bhåpjoi dfåppjoihb åpdjfoihbåijpod fåbboijp dfåojhip båpdfjio cgåpoijb ådfpojg cbpojv dfåpgojbojfd gcb<br><br>&nbsp;vpfdoijb åpfjdoi bpåfodj ågbpoijfd ågpoj båfdopijgbbåpjo fpågojbpofjd gbvjpodfø gopbjk fodjx bpöjkf gxobkj vfokgxbo fkxgpobk pokgxxbpok fpovkb ogdfk gpbök fdpogkvcb pofdkg bcpok dfogxkb dofkg bc<br><br style=\"font-weight: bold;\"><div style=\"text-align: center;\"><span style=\"font-weight: bold;\">denne line skal være fed og centreret</span><br></div><br>OG her kommer mere almindelig tekst doijf våorwij fvåojiper pbogvijerrpoijvbgv åjeroip bgåiopjet gvåoipje tpbvogj etpogbkj etåpojgb petojgboijet gboijet gboåij tepogbj etåoibjt <br><br><div style=\"text-align: center; font-weight: bold;\">Og her fler lininer som er fed og cwentreret <br>denne er med <br>denne ogr også med<br></div><br>OG mere helt almindelig tekst åoij åoijgb dåfjiop bpoj ghbojgdj&#7719;pokg<br>\"gdøopbkjk pokjbh prgonh pfoknh rgonkn hrf<br><br></body></html>";

    

    public Fragment fragSomVises;
//////////-------------------------//////////
	

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("A.oncreate() kaldt");
        a= this;
        ctx=this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        int modenhed = tjekModenhed();
        p("Modenhed: (0=frisk, 1=første, 2=anden, 3=moden) "+ modenhed);

        if (modenhed == MODENHED_MODEN) {

            skalTekstlistenOpdateres();

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
        else if (modenhed > MODENHED_HELT_FRISK) {
//Ryk evt til splash
			ArrayList<Tekst> temp = hentsynligeTekster();
			if (temp !=null){
				synligeTekster = temp; 
				//temp=null;
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        htekster = Util.hentTekstliste("htekster", ctx);
                        gemAlleNyeTekster();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);

                        ///for (Tekst t : htekster) p("Tjekker Htekster: " + t.toString(1));
                    }
                }.execute();
			}
		}



        else if (modenhed == MODENHED_HELT_FRISK) {
			
            new AsyncTask() {


                @Override
                protected Object doInBackground(Object[] params) {
                    alleTekster = hentTeksterOnline();

                    return null;
                }
				@Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    p("Ny tekstversion? : "+findesNyTekst);
					ArrayList<Tekst> otekster = alleTekster[0];
					Tekst t = otekster.get(0);
					synligeTekster.add(t);
                    gemSynligeTekster();

                    Tekst nr2 = otekster.get(1);
                    Util.gemTekst(nr2, "otekst2", ctx);
					ArrayList<Tekst> htekster = alleTekster[3];
                    Util.gemTekstliste(htekster, "htekster", ctx);

                }
				

            }.execute();


        }

        else if (modenhed == MODENHED_FØRSTE_DAG) {
            //hhmm
        }

        else if (modenhed == MODENHED_ANDEN_DAG) {

            if (pref.getBoolean("andenDagFørsteGang", true)) {
                Tekst t = Util.hentTekst("otekst2", ctx);
                synligeTekster.add(t);
                gemSynligeTekster();
                pref.edit().putBoolean("andenDagFørsteGang", false).apply();
            }
        }

    }

    private void gemAlleNyeTekster() {
        Tekst dummyTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "t", new DateTime());
        dummyTekst.lavId();

        ArrayList<Tekst> itekster = Util.sorterStigende(alleTekster[1]);

        Set <String> alleteksterSæt = pref.getStringSet("alletekster", new HashSet<String>());


        for (int i = 0; i < itekster.size(); i++) {
            Tekst itekst = itekster.get(i);

            if (itekst.tekstid >= dummyTekst.tekstid) {
                Util.gemTekst(itekst,""+itekst.tekstid,ctx);
                alleteksterSæt.add(""+itekst.tekstid);
                //Der er lige noget med at de to ældre tekster
            }
        }
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
            else pref.edit().putInt("modenhed", MODENHED_MODEN);
            return MODENHED_MODEN;
        }

        return MODENHED_MODEN;
    }

    /*
    *  1102016
    * 11012016
    *
    * 20161001
    * 20160111
    * */


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

    boolean tjekTekstRul () {

        return true;
    }

    private void indlæsTekster()  {

        //TODO
    }

    private void dummyInit() {

        //bruges under parsing
        htmlStreng = htmlStreng.replaceFirst("<title", "<!--title");
        htmlStreng = htmlStreng.replaceFirst("</title>", "title-->");
        htmlStreng = htmlStreng.replaceFirst("<body", "<body style=\"color: white; background-color: black;\"");

        tekster.add("Tekst1 overskrift");
        tekster.add("Tekst2 overskrift");//"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head><meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\"><title>o17092016.txt</title></head><body><h2 style=\"text-align: center;\">Overskift</h2>Prødtekst bla bla bla<br>loårwij vfvåoijdsd åvoij såoigjb åfsoji gvåoifjob idfj obåijfd boåijfdobigj fdoibjåfdoijgb åfdoij båojfdpi gbåjopidf åboji dfåobji dfåojibåfdjop båpdfjo hbåpjiodf <span style=\"font-weight: bold;\">fed tekst inde i teksten</span>oæbhijd fåpjoib fdoji bådfoipj båpdoijo bhåpjoi dfåppjoihb åpdjfoihbåijpod fåbboijp dfåojhip båpdfjio cgåpoijb ådfpojg cbpojv dfåpgojbojfd gcb<br><br>&nbsp;vpfdoijb åpfjdoi bpåfodj ågbpoijfd ågpoj båfdopijgbbåpjo fpågojbpofjd gbvjpodfø gopbjk fodjx bpöjkf gxobkj vfokgxbo fkxgpobk pokgxxbpok fpovkb ogdfk gpbök fdpogkvcb pofdkg bcpok dfogxkb dofkg bc<br><br style=\"font-weight: bold;\"><div style=\"text-align: center;\"><span style=\"font-weight: bold;\">denne line skal være fed og centreret</span><br></div><br>OG her kommer mere almindelig tekst doijf våorwij fvåojiper pbogvijerrpoijvbgv åjeroip bgåiopjet gvåoipje tpbvogj etpogbkj etåpojgb petojgboijet gboijet gboåij tepogbj etåoibjt <br><br><div style=\"text-align: center; font-weight: bold;\">Og her fler lininer som er fed og cwentreret <br>denne er med <br>denne ogr også med<br></div><br>OG mere helt almindelig tekst åoij åoijgb dåfjiop bpoj ghbojgdj&#7719;pokg<br>\"gdøopbkjk pokjbh prgonh pfoknh rgonkn hrf<br><br></body></html>");
        tekster.add("Tekst3 overskrift");
        tekster.add("Tekst4 overskrift");
        tekster.add("Tekst5 overskrift");

        overskrifter.add("1 En meget lang overskrift der nok er for bred til skærmen");
        overskrifter.add("2 En kortere overskrift ");
        overskrifter.add("3 En meget lang overskrift der nok er for bred til skærmen");
        overskrifter.add("4 En overskrift");
        overskrifter.add("5 En meget lang overskrift der nok er for bred til skærmen");



    }

    void t(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    void p(Object o){
        String kl = "A.";
        kl += o ;//+ "   #t:" + String.format("%.3f", sec);
        System.out.println(kl);
        //this.debugmsg += kl +"\n";
    }

}

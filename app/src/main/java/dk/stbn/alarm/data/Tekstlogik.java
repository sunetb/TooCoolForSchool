package dk.stbn.alarm.data;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

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

import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.diverse.Tid;
import dk.stbn.alarm.lyttere.Lyttersystem;

public class Tekstlogik {



    private static Tekstlogik tl;
    public ArrayList<Tekst> synligeTekster;
    public ArrayList<String> hteksterOverskrifter;
    public ArrayList<Tekst> htekster;




    Lyttersystem lytter;
    Tilstand t;
    private Context c;



    private Tekstlogik(Context c){
        this.c = c;
        lytter = Lyttersystem.getInstance();
        t = Tilstand.getInstance(c);

    }

    public static Tekstlogik getInstance(Context c){
        if (tl == null) tl = new Tekstlogik(c);
        return tl;
    }



    /**
     * Udvælger tekster på baggrund af modenhed og om der er ny version på nettet
     */
    public void udvælgTekster() {
        int modenhed = Tilstand.getInstance(c).modenhed;

        ArrayList<Tekst> tempSynlige = new ArrayList<>();

        if (modenhed == K.SOMMERFERIE) {
            p("sommerferie!!!");

            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_1, c));
            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_2, c));
            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_3, c));
//GEM DETTE
        } else if (modenhed == K.MODENHED_HELT_FRISK) {
            p("udvælgTekster() Modenhed: Helt frisk");
            allerFørsteGang();
            IO.gemObj(new DateTime(), K.MASTERDATO, c);

        } else if (modenhed == K.MODENHED_FØRSTE_DAG) {
            p("Dag 1, ikke første gang");

            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_1, c));

        } else if (modenhed == K.MODENHED_ANDEN_DAG) {
            p("Dag 2 ");

            Tekst oTekst1 = (Tekst) IO.læsObj(K.OTEKST_1, c);
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, c);
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);
//hertil
        } else if (modenhed == K.MODENHED_TREDJE_DAG) {
            p("Dag 3 ");

            Tekst oTekst1 = (Tekst) IO.læsObj(K.OTEKST_1, c);
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, c);
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);

            ArrayList<Tekst> tekster = findItekster();
            if (tekster.size() > 0)
                tempSynlige.add(tekster.get(0));

        } else if (modenhed == K.MODENHED_FJERDE_DAG) {
            p("Dag 4 ");
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, c);


            tempSynlige.add(oTekst2);
            ArrayList<Tekst> tekster = findItekster();
            //Tag kun de første to I-tekster, så der vises tre i alt
            if (tekster.size() > 0) tempSynlige.add(tekster.get(0));
            if (tekster.size() > 1) tempSynlige.add(tekster.get(1));

            //sørger for at der altid er tre tekster, også lige efter sommerferien
            if (tempSynlige.size() == 2)
                tempSynlige.add(0, (Tekst)IO.læsObj(K.OTEKST_1, c));

        } else if (modenhed == K.MODENHED_MODEN) {
            p("Dag 5: MODEN ");

            ArrayList<Tekst> itekster = findItekster();

            //Særtilfælde: er appen ung og har kun én eller to I-tekster?
            if (itekster.size() == 1) {
                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_1, c));
                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_2, c));
            }
            if (itekster.size() == 2) {

                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_2, c));
            }
            tempSynlige.addAll(itekster);
            ArrayList<Tekst> mtekster = findMtekster();
            tempSynlige.addAll(mtekster);

        }

        p("Tjekker om de cachede tekster skal erstattes..");

        //Der skal skiftes hvis gemt liste og ny liste er forskellig længde
        boolean skift = synligeTekster.size() != tempSynlige.size();

        //Der skal skiftes hvis appen ikke er moden
        if (modenhed != K.MODENHED_MODEN) skift = true;

        //Er appen moden og har listerne samme længde, må vi tjekke indholdet af listerne
        if (!skift) {
            boolean forskellige = false;

            for (int i = 0; i < synligeTekster.size(); i++) {
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
            p("JA. Der er  et nyt udvalg af tekster");
            synligeTekster = tempSynlige;
            Tilstand.getInstance(c).pref.edit().putInt("senesteposition", -1).commit(); //Sætter ViewPagerens position til nyeste element
            lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "udvælgTekster(), der var et nyt udvalg");
            gemSynligeTekster();
        } else
            p("NEJ. Vi bruger de cachede tekster");

        if (modenhed < K.MODENHED_TREDJE_DAG){
            //sørg for at der ikke vises notifikationer i starten
            for (Tekst t : synligeTekster)
                IO.føjTilGamle(t.id_int, c);
        }

        p("udvælgTekster() færdig");
    }
    /**
     * Finder de I-tekster som skal vises idag
     * @return array med tekster
     *
     */
    ArrayList<Tekst> findItekster() {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> itekster = (ArrayList<Tekst>) IO.læsObj(K.ITEKSTER, c);

        Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", t.masterDato);
        dummyITekst.lavId();

        p("Tjek dummytekst id: " + dummyITekst.id_int);
        p("itekster længde: " + itekster.size());

        boolean iFundet = false;
        for (int i = 0; i < itekster.size(); i++) {
            Tekst itekst = itekster.get(i);
            int tekstid = itekst.id_int;
            p("Tjek Itekster: " + tekstid);
            p("IdTekst: " + itekst.id);

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
        if (!iFundet){
            p("Appen er løbet tør for tekster (Snart sommerferie)");
            int længde = itekster.size();
            r.add(itekster.get(længde-3));
            r.add(itekster.get(længde-2));
            r.add(itekster.get(længde-1));

        }
        return r;


    }

    /**
     * Henter gemte synlige tekster fra sidst
     */
    void visCachedeTekster() {

        synligeTekster = (ArrayList<Tekst>) IO.læsObj(K.SYNLIGETEKSTER, c);

        if (synligeTekster != null) {
            lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "visCachedeTekster()");
            p("visCachedeTekster() synligeTekster længde: "+synligeTekster.size());
        }
        else p("Fejl: gemte synlige tekster fandes ikke");
    }


    /**
     * Finder de M-tekster som skal vises idag
     * @return
     */
    private ArrayList<Tekst> findMtekster() {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> mtekster = (ArrayList<Tekst>) IO.læsObj(K.MTEKSTER, c);

        Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", Tilstand.getInstance(c).masterDato);
        dummyMTekst.lavId();
        p("Tjek M dummytekst id: " + dummyMTekst.id_int);

        boolean mFundet = false;

        p("Mtekster længde: " + mtekster.size());

        for (int i = 0; i < mtekster.size(); i++) {
            Tekst mtekst = mtekster.get(i);
            p("tjek mtekster: " + mtekst.id_int);
            p("IdTekst: " + mtekst.id);

            if (mtekst.id_int >= dummyMTekst.id_int) {

                if (!mFundet) {

                    if (mtekst.id_int == dummyMTekst.id_int) {
                        p("Eksakt match Mtekst");
                        r.add(mtekst);

                    } else if (visMtekst(mtekst.dato, Tilstand.getInstance(c).masterDato)) {
                        r.add(mtekst);
                        p("Mtekst ineksakt match --");
                    }
                    mFundet = true;
                }
            }
        }
        return r;

    }

    /**
     * Henter gemte Htekster og genererer en liste med overskrifterne
     */
    void indlæsHtekster() {

        lytter.givBesked(K.NYE_HTEKSTER_PÅ_VEJ, "indlæsHtekster()"); //Så brugeren ikke trykker netop mens den opdateres

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {

                p("indlæsHtekster() start");

                ArrayList<Tekst> gemteHtekster = (ArrayList<Tekst>) IO.læsObj(K.HTEKSTER, c);
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

    /**
     * Fyrer en event hvis der er ny version
     *
     * @param kaldtFra
     */
    public void tjekTekstversion(String kaldtFra) {
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
                int gemtTekstversion = t.pref.getInt(K.TEKSTVERSION, 0);
                p("gemt tekstversion: " + gemtTekstversion);

                if (gemtTekstversion < netversion) {
                    //Måske giver det ikke rigtig mening med event længere efter Den Store Revidering
                    if (t.modenhed > K.MODENHED_HELT_FRISK) //vi skal ikke fyre event første dag, da dse nyeste data allerede er hentet
                        lytter.givBesked(K.NYE_TEKSTER_ONLINE, "tjektekstversion, nye online");
                    t.pref.edit().putInt(K.TEKSTVERSION, netversion).commit();
                }
                else {
                    lytter.givBesked(K.INGEN_NYE_TEKSTER_ONLINE, "tjektekstversion, ingen nye online");
                }

            }
        }.execute();

    }

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

    /**Som sikkerhed skal vi kunne hente en enkelt M- eller I-tekst fra disken på dens ID
     *
     * @param tekster
     */
    private void gemEnkelteTeksterTilDisk(ArrayList<Tekst> tekster) {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                for (Tekst tekst : tekster)
                    IO.gemObj(tekst, "" + tekst.id_int, c);
                return null;
            }
        }.execute();
    }



    public void gemSynligeTekster() {
        IO.gemObj(synligeTekster, K.SYNLIGETEKSTER, c);
    }

    public void opdaterTekstbasen(){
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                p("opdaterTekstbasen() henter alle tekster..");
                ArrayList[] alleTekster = hentTeksterOnline("opdaterTekstbasen()");

                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                o1.formater();
                IO.gemObj(o1, K.OTEKST_1, c);
                otekster = alleTekster[0];
                Tekst o2 = otekster.get(1);
                o2.formater();
                IO.gemObj(o2, K.OTEKST_2, c);
                Tekst o3 = otekster.get(2);
                o3.formater();
                IO.gemObj(o3, K.OTEKST_3, c);

                ArrayList<Tekst> itekster = alleTekster[1];
                for (Tekst t : itekster) t.formater();
                itekster = Util.sorterStigende(itekster);
                IO.gemObj(itekster, K.ITEKSTER, c);


                ArrayList<Tekst> mtekster = alleTekster[2];

                for (Tekst t : mtekster) t.formater();
                mtekster = Util.sorterStigende(mtekster);
                IO.gemObj(mtekster, K.MTEKSTER, c);
                publishProgress(1);

                ArrayList<Tekst> htekster = alleTekster[3];
                for (Tekst t : htekster)
                    t.formater();

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift.toUpperCase());
                p("Event til aktiviteten om at H-tekster er klar");

                publishProgress(2);
                IO.gemObj(htekster, K.HTEKSTER, c);

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
            t.pref.edit().putString("sprog", sprog).commit();
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



    public void tjekSprog() {
        String sprog = Locale.getDefault().getLanguage();
        String gemtSprog = t.pref.getString("sprog", "ikke sat");
        //TODO:
        t.pref.edit().putString("sprog", sprog).commit();
        p("SPROG " + sprog);
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
                int idag = Util.lavDato(t.masterDato);

                t.pref.edit()
                        .putInt("modenhed", K.MODENHED_FØRSTE_DAG)
                        .putInt("installationsdato", idag)
                        .commit();
                t.modenhed = K.MODENHED_FØRSTE_DAG;
                //-- hertil

                p("Finder O-tekst1...");
                ArrayList<Tekst> otekster = alleTekster[0];
                Tekst o1 = otekster.get(0);
                o1.formater();
                synligeTekster.add(o1);
                p("Så er der O-tekst i array!");

                p("Event til aktiviteten om at synlige tekster er klar");

                if (t.aktivitetenVises) {
                    publishProgress(1);
                }
                else {
                    p("Aktiviteten blev klar EFTER at data blev klar");
                }

                IO.gemObj(o1, K.OTEKST_1, c);

                p("Formaterer H-tekster...");
                htekster = alleTekster[3];
                for (Tekst t : htekster)
                    t.formater();

                for (Tekst t : htekster)
                    hteksterOverskrifter.add(t.overskrift.toUpperCase());

                p("Event til aktiviteten om at H-tekster er klar");

                publishProgress(2);
                IO.gemObj(htekster, K.HTEKSTER, c);

                //-- Gemmer O-tekst nr 2 og 3 til næste gang

                Tekst o2 = otekster.get(1);
                o2.formater();
                IO.gemObj(o2, K.OTEKST_2, c);

                Tekst o3 = otekster.get(2);
                o3.formater();
                IO.gemObj(o3, K.OTEKST_3, c);


                p("Formaterer resten af listerne..");
                ArrayList<Tekst> itekster = alleTekster[1];
                p("Itekster længde når den hentes i allerførstegang(): "+itekster.size());

                for (Tekst t : itekster) t.formater();
                itekster = Util.sorterStigende(itekster);

                ArrayList<Tekst> mtekster = alleTekster[2];
                p("Mtekster længde når den hentes i allerførstegang(): "+mtekster.size());

                for (Tekst t : mtekster) t.formater();
                mtekster = Util.sorterStigende(mtekster);


                IO.gemObj(new ArrayList<Integer>(), K.GAMLE, c);
                IO.gemObj(new ArrayList<Integer>(), K.DATOLISTE, c);
                IO.gemObj(new ArrayList<Integer>(), K.SYNLIGEDATOER, c);

                if (t.aktivitetenVises)
                    publishProgress(1);
                else
                    p("Aktiviteten stadig ikke klar selvom data blev klar");

                IO.gemObj(itekster, K.ITEKSTER, c);
                p("Itekster længde når den gemmes i allerførstegang(): "+itekster.size());
                IO.gemObj(mtekster, K.MTEKSTER, c);
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
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    void p(Object o) {
        String kl = "Tekstlogik.";
        Util.p(kl + o);
    }

}

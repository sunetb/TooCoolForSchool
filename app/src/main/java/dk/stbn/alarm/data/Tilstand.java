package dk.stbn.alarm.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import org.joda.time.DateTime;
import java.util.ArrayList;
import dk.stbn.alarm.Tekst;
import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.diverse.Tid;
import dk.stbn.alarm.lyttere.Lyttersystem;

public class Tilstand {


//TODO Skal den laves om til ViewModel?

    public DateTime masterDato;
    public int modenhed;
    boolean femteDagFørsteGang;
    public int skærmVendt;
    public boolean hteksterKlar = false;
    public boolean aktivitetenVises = false; //tjekker om aktiviteten vises før der er data at vise
    public int sidstKendteVindueshøjde = 0;

    SharedPreferences pref;
    A a;


    private final String MODENHED = "modenhed";

    private static Tilstand instans;

    public static Tilstand getInstance(Context c, A a){
        if (instans == null)
            instans = new Tilstand(c, a);
        return instans;
    }


    Tilstand (Context c, A a) {
        this.a = a;
        masterDato = new DateTime();
        pref = PreferenceManager.getDefaultSharedPreferences(c);
        modenhed = opdaterModenhed(c);
        p("Global modenhed efter opdaterModenhed: "+modenhed);
        p("Modenhed i prefs: " + pref.getInt(MODENHED, -1));
    }


    private int opdaterModenhed(Context c) {

        int tempModenhed = pref.getInt(MODENHED, K.MODENHED_HELT_FRISK);
        p("Gemt modenhed er: "+tempModenhed);

        //Har vi netop passeret sommerferien? Så nulstil appens data
        boolean harPasseretSommerferie = Tid.efter(masterDato, K.SOMMERFERIE_SLUT) && tempModenhed == K.SOMMERFERIE;
        if (harPasseretSommerferie) {
            sletAlt(c);//nulstiller bla. prefs og derfor også harPasseretSommer..
            tempModenhed = 0;
            gemModenhed(K.MODENHED_FØRSTE_DAG);
        }

        else{//Er det sommerferie?
            boolean sommerferie = Tid.efter(masterDato, K.SOMMERFERIE_START) && Tid.før(masterDato, K.SOMMERFERIE_SLUT);

            if (sommerferie) {
                gemModenhed(K.SOMMERFERIE);
                pref.edit()
                        .putInt("senesteposition", -1)
                        .commit();
                return K.SOMMERFERIE;
            }
        }


        if (tempModenhed == K.MODENHED_MODEN) return K.MODENHED_MODEN;

        int idag = Util.lavDato(masterDato);

        if (tempModenhed == K.MODENHED_HELT_FRISK) {
            //koden herfra, hvor tempModenhed sættes til FØRSTE_DAG, er flyttet til A.allerFørsteGang() for at den ikke bliver kørt med mindre appen får hentet sine data

            return K.MODENHED_HELT_FRISK;
        }

        else if (tempModenhed == K.MODENHED_FØRSTE_DAG){
            int instDato  = pref.getInt("installationsdato", 0);
            if (idag == instDato) return K.MODENHED_FØRSTE_DAG;
            else {
                gemModenhed(K.MODENHED_ANDEN_DAG);
                pref.edit()
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_ANDEN_DAG første gang");
                return K.MODENHED_ANDEN_DAG;
            }
        }

        else if (tempModenhed == K.MODENHED_ANDEN_DAG){
            int instDatoPlusEn = pref.getInt("tjekInst", 0);
            if (idag == instDatoPlusEn) return K.MODENHED_ANDEN_DAG;
            else {
                gemModenhed(K.MODENHED_TREDJE_DAG);
                pref.edit()
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_TREDJE_DAG første gang");
                return K.MODENHED_TREDJE_DAG;
            }
        }

        else if (tempModenhed == K.MODENHED_TREDJE_DAG){
            int instDatoPlusTo = pref.getInt("tjekInst", 0);
            if (idag == instDatoPlusTo) return K.MODENHED_TREDJE_DAG;
            else {
                gemModenhed(K.MODENHED_FJERDE_DAG);
                pref.edit()
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_FJERDE_DAG første gang");
                return K.MODENHED_FJERDE_DAG;
            }
        }
        else if (tempModenhed == K.MODENHED_FJERDE_DAG){
            int instDatoPlusTre = pref.getInt("tjekInst", 0);
            if (idag == instDatoPlusTre) return K.MODENHED_FJERDE_DAG;
            else {
                gemModenhed(K.MODENHED_MODEN);
                femteDagFørsteGang = true;
                p("Modenhed sat til MODEN første gang");
            }
        }

        return K.MODENHED_MODEN;
    }

    void gemModenhed(int værdi){
        pref.edit().putInt(MODENHED,værdi).commit();
        p("gemModenhed() kaldt med vædi: "+værdi);
    }



    //-- Kaldes når appen har kørt alle tekster igennnem og skal starte forfra med tekst1
    private void sletAlt(Context c) {
        nulstil();
        p("sletAlt kaldt");
        sletDiskData(c);
        a.synligeTekster.clear();
        a.synligeTekster.add((Tekst) IO.læsObj("otekst1", c));
        Lyttersystem.getInstance().givBesked(K.NYE_TEKSTER_ONLINE,"nulstillet");
        gemModenhed(K.MODENHED_HELT_FRISK);
        a.allerFørsteGang(); //her sættes pref modenhed til 1 = FØRSTE DAG
    }

    void sletDiskData(Context c){
        p("sletDiskData() blev kaldt");
        pref.edit().clear().commit();

        ArrayList tomTekst = new ArrayList<Tekst>();
        IO.gemObj(tomTekst, "tempsynligeTekster", c);
        IO.gemObj(tomTekst, "htekster", c);
        ArrayList<Integer> tomTal = new ArrayList<>();
        IO.gemObj(tomTal, "synligeDatoer", c);
        IO.gemObj(tomTal, "gamle", c);

    }
    /**
     * Debugging
     */
    public void nulstil(){
        modenhed = 0;
        boolean femteDagFørsteGang = false;
        skærmVendt = -1;
    }

    /**
     * Debugging
     */
    public void sætTilstand(int kode){
        //TODO
    }

    void p(Object o){
        String kl = "Tilstand.";
        Util.p(kl+o);
    }


    @NonNull
    @Override
    public String toString() {
        String s = "Masterdato: "+masterDato + "\n"+
        "Modenhed: " + modenhed  + "\n"+
        "Femte dag, første gang?: " + femteDagFørsteGang + "\n"+
        "Skærm vendt=? " +skærmVendt + "\n";
        return s;
    }
}



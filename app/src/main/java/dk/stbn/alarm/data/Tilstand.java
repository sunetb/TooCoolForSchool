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
    SharedPreferences pref;
    boolean femteDagFørsteGang;
    public int skærmVendt;
    A a;

    private static Tilstand instans;

    public static Tilstand getInstance(Context c, A a){
        if (instans == null) {
            instans = new Tilstand(c, a);
            return instans;
        }
        else return instans;
    }


    Tilstand (Context c, A a) {
        this.a = a;
        masterDato = new DateTime();
        pref = PreferenceManager.getDefaultSharedPreferences(c);
        modenhed = opdaterModenhed(c);
        p("opdaterModenhed() returnerede: "+modenhed);
    }


    private int opdaterModenhed(Context c) {

        int modning = pref.getInt("modenhed", K.MODENHED_HELT_FRISK);
        p("Gemt modenhed er: "+modning);

        if (Tid.efter(masterDato, K.sommerferie_slut) && modenhed == K.SOMMERFERIE)
            pref.edit().putBoolean("harPasseretSommerferie", true).commit();

        boolean harPasseretSommerferie = pref.getBoolean("harPasseretSommerferie", false);

        if (harPasseretSommerferie) {
            sletAlt(c);//nulstiller bla. prefs og derfor også harPasseretSommer..
        }

        else{
            boolean sommerferie = Tid.efter(masterDato, K.sommerferie_start) && Tid.før(masterDato, K.sommerferie_slut);

            if (sommerferie) {
                pref.edit()
                        .putInt("modenhed", K.SOMMERFERIE)
                        .putInt("senesteposition", -1)
                        .commit();
                return K.SOMMERFERIE;
            }
        }


        if (modning == K.MODENHED_MODEN) return K.MODENHED_MODEN;

        int idag = Util.lavDato(masterDato);

        if (modning == K.MODENHED_HELT_FRISK) {
            //koden herfra, hvor modning sættes til FØRSTE_DAG, er flyttet til A.allerførsteGangInitOTekst() for at den ikke bliver kørt med mindre appen får hentet sine data

            return K.MODENHED_HELT_FRISK;
        }

        else if (modning == K.MODENHED_FØRSTE_DAG){
            int instDato  = pref.getInt("installationsdato", 0);
            if (idag == instDato) return K.MODENHED_FØRSTE_DAG;
            else {
                pref.edit()
                        .putInt("modenhed", K.MODENHED_ANDEN_DAG)
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_ANDEN_DAG første gang");
                return K.MODENHED_ANDEN_DAG;
            }
        }

        else if (modning == K.MODENHED_ANDEN_DAG){
            int instDatoPlusEn = pref.getInt("tjekInst", 0);
            if (idag == instDatoPlusEn) return K.MODENHED_ANDEN_DAG;
            else {
                pref.edit()
                        .putInt("modenhed", K.MODENHED_TREDJE_DAG)
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_TREDJE_DAG første gang");
                return K.MODENHED_TREDJE_DAG;
            }
        }

        else if (modning == K.MODENHED_TREDJE_DAG){
            int instDatoPlusTo = pref.getInt("tjekInst", 0);
            if (idag == instDatoPlusTo) return K.MODENHED_TREDJE_DAG;
            else {
                pref.edit()
                        .putInt("modenhed", K.MODENHED_FJERDE_DAG)
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_FJERDE_DAG første gang");
                return K.MODENHED_FJERDE_DAG;
            }
        }
        else if (modning == K.MODENHED_FJERDE_DAG){
            int instDatoPlusTre = pref.getInt("tjekInst", 0);
            if (idag == instDatoPlusTre) return K.MODENHED_FJERDE_DAG;
            else {
                pref.edit()
                        .putInt("modenhed", K.MODENHED_MODEN)
                        .commit();
                femteDagFørsteGang = true;
                p("Modenhed sat til MODEN første gang");
            }
        }

        return K.MODENHED_MODEN;
    }




    //-- Kaldes når appen er kørt igennnem og skal starte forfra med tekst1
    private void sletAlt(Context c) {
        nulstil();
        p("sletAlt kaldt");
        sletDiskData(c);
        a.synligeTekster.clear();
        a.synligeTekster.add((Tekst) IO.læsObj("otekst1", c));
        Lyttersystem.getInstance().givBesked(K.SYNLIGETEKSTER_OPDATERET,"nulstillet");
        pref.edit().putInt("modenhed", K.MODENHED_HELT_FRISK).commit();
        a.allerførsteGangInitOTekst();
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



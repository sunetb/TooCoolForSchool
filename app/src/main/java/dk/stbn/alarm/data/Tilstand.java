package dk.stbn.alarm.data;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.diverse.Tid;

public class Tilstand {



//TODO Skal den laves om til ViewModel?

    public DateTime masterDato;
    public int modenhed;
    private boolean femteDagFørsteGang;
    public int skærmVendt;
    public boolean hteksterKlar = false;
    public boolean aktivitetenVises = false; //tjekker om aktiviteten vises før der er data at vise
    public int sidstKendteVindueshøjde = 0;

    public boolean nyTekstversion = false;
    public boolean boot = false; //Er appen startet af boot-lytteren

    SharedPreferences pref;
    A a;



    //Test/debug
    public boolean testtilstand = false;
    public  boolean testtilstand_2 = false;
    public  boolean debugging = true;



    private final String MODENHED = "modenhed";


    private static Tilstand instans;

    public static Tilstand getInstance(SharedPreferences s) {
        if (instans == null) instans = new Tilstand(s);
        return instans;
    }


    private Tilstand(SharedPreferences s) {
        masterDato = new DateTime();
        p("Masterdato: "+masterDato);
        a = A.a;
        pref = s;
        modenhed = opdaterModenhed();
        p("Global modenhed efter opdaterModenhed: " + modenhed);

    }

    /**
     * Initialiserer én variabel: Tilstand.modenhed
     * @return
     */
    private int opdaterModenhed() {

        int tempModenhed = pref.getInt(MODENHED, K.MODENHED_HELT_FRISK);
        p("Gemt modenhed er: " + tempModenhed);
        if (tempModenhed == K.MODENHED_HELT_FRISK) {
            //koden herfra, hvor tempModenhed sættes til FØRSTE_DAG, er flyttet til A.allerFørsteGang()
            // for at den ikke bliver modnet med mindre appen får hentet sine data
            p("den var frisk");
            gemModenhed(K.MODENHED_HELT_FRISK);
            return K.MODENHED_HELT_FRISK;
        }
        //Har vi netop passeret sommerferien? Så nulstil appens data
        boolean harPasseretSommerferie = Tid.efter(masterDato, K.SOMMERFERIE_SLUT) && tempModenhed == K.SOMMERFERIE;
        if (harPasseretSommerferie) {
            p("Vi har passeret sommerferien");

            gemModenhed(K.MODENHED_FØRSTE_DAG);
            tempModenhed = K.MODENHED_HELT_FRISK;
            return K.MODENHED_HELT_FRISK;


        } else {//Er det sommerferie?
            boolean sommerferie = Tid.efter(masterDato, K.SOMMERFERIE_START) && Tid.før(masterDato, K.SOMMERFERIE_SLUT);
            p("Er det sommerferie? "+sommerferie);

            if (sommerferie) {

                gemModenhed(K.SOMMERFERIE);
                pref.edit()
                        .putInt("senesteposition", -1)
                        .commit();
                return K.SOMMERFERIE;
            }
        }
        if (tempModenhed == K.MODENHED_MODEN)
            return K.MODENHED_MODEN;

        int idag = Util.lavDato(masterDato);


        if (tempModenhed == K.MODENHED_FØRSTE_DAG) {
            int instDato = pref.getInt("installationsdato", 0);
            if (idag == instDato) return K.MODENHED_FØRSTE_DAG;
            else {
                gemModenhed(K.MODENHED_ANDEN_DAG);
                pref.edit()
                        .putInt("tjekInst", idag)
                        .commit();
                p("Modenhed sat til MODENHED_ANDEN_DAG første gang");
                return K.MODENHED_ANDEN_DAG;
            }
        } else if (tempModenhed == K.MODENHED_ANDEN_DAG) {
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
        } else if (tempModenhed == K.MODENHED_TREDJE_DAG) {
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
        } else if (tempModenhed == K.MODENHED_FJERDE_DAG) {
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

    void gemModenhed(int værdi) {
        pref.edit().putInt(MODENHED, værdi).commit();
        p("gemModenhed() kaldt med værdi: " + værdi);
    }



    /**
     * Debugging
     */
    public void nulstil() {
        p("nulstil() blev kaldt");
        instans = null;
    }

    /**
     * Debugging
     */
    public void sætTilstand(int kode) {
        //TODO
    }

    void p(Object o) {
        String kl = "Tilstand.";
        Util.p(kl + o);
    }


    @NonNull
    @Override
    public String toString() {
        String s = "Masterdato: " + masterDato + "\n" +
                "Modenhed: " + modenhed + "\n" +
                "Femte dag, første gang?: " + femteDagFørsteGang + "\n" +
                "Skærm vendt=? " + skærmVendt + "\n";
        return s;
    }
}



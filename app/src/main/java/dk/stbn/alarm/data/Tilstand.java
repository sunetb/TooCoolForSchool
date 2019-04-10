package dk.stbn.alarm.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.diverse.Tid;

public class Tilstand {

//TODO    Skal den laves om til ViewModel?

    public DateTime masterDato;
    public int modenhed;
    SharedPreferences pref;
    boolean tredjeDagFørsteGang;
    public int skærmVendt;

    private static Tilstand instans;

    public static Tilstand getInstance(Context c){
        if (instans == null) return new Tilstand(c);
        else return instans;
    }


    Tilstand (Context c) {
        masterDato = new DateTime();
        pref = PreferenceManager.getDefaultSharedPreferences(c);
        modenhed = tjekModenhed(c);
    }


    private int tjekModenhed(Context c) {

        DateTime sommerferie_start = new DateTime().withDayOfMonth(8).withMonthOfYear(6);
        p(sommerferie_start);
        DateTime sommerferie_slut =  new DateTime().withDayOfMonth(20).withMonthOfYear(8);
        p(sommerferie_slut);


        if (Tid.efter(masterDato, sommerferie_start) && Tid.før(masterDato, sommerferie_slut)) {
            p("Tjekmodenhed siger SOMMERFERIE");
            pref.edit().putInt("modenhed", K.MODENHED_HELT_FRISK)
                    .putInt("senesteposition", -1)
                    .commit();
            return K.SOMMERFERIE;
        }


        int modning = pref.getInt("modenhed", K.MODENHED_HELT_FRISK);
        p("Modenhed i prefs er "+modning);

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
                        .putInt("installationsdato2", idag)
                        .commit();
                return K.MODENHED_ANDEN_DAG;
            }
        }
        else if (modning == K.MODENHED_ANDEN_DAG){
            int instDatoPlusEn = pref.getInt("installationsdato2", 0);
            if (idag == instDatoPlusEn) return K.MODENHED_ANDEN_DAG;
            else {
                pref.edit()
                        .putInt("modenhed", K.MODENHED_TREDJE_DAG)
                        .putInt("installationsdato3", idag)
                        .commit();
                tredjeDagFørsteGang = true;
                p("tjekModenhed() Tredje dag første gang sat til true");
                return K.MODENHED_TREDJE_DAG;
            }
        }

        else if (modning == K.MODENHED_TREDJE_DAG){
            int instDatoPlusTo = pref.getInt("installationsdato3", 0);
            if (idag == instDatoPlusTo) return K.MODENHED_TREDJE_DAG;
            else {
                pref.edit()
                        .putInt("modenhed", K.MODENHED_MODEN)
                        .commit();
                p("Modenhed sat til MODEN første gang");
            }
        }

        return K.MODENHED_MODEN;
    }

    /**
     * Debugging
     */
    public void nulstil(){
        //TODO
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
        "Tredje dag, første gang?: " + tredjeDagFørsteGang + "\n"+
        "Skærm vendt=? " +skærmVendt + "\n";
        return s;
    }
}

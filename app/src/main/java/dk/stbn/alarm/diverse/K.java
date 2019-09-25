package dk.stbn.alarm.diverse;

import org.joda.time.DateTime;

public class K {

    // Modenhed
    public static final int MODENHED_HELT_FRISK  = 0;   //Otekst 1
    public static final int MODENHED_FØRSTE_DAG  = 1;   //Otekst 1
    public static final int MODENHED_ANDEN_DAG   = 2;   //Otekst 1 + 2
    public static final int MODENHED_TREDJE_DAG  = 3;   //Otekst 1 + 2 + Itekst 1
    public static final int MODENHED_FJERDE_DAG  = 4;   //Otekst 2 + Itekst 1 + 2
    public static final int MODENHED_MODEN       = 5;
    public static final int SOMMERFERIE          = 6;



    public static String modenhed(int m){
        switch (m){
            case 0: return "MODENHED_HELT_FRISK";
            case 1: return "MODENHED_FØRSTE_DAG";
            case 2: return "MODENHED_ANDEN_DAG";
            case 3: return "MODENHED_TREDJE_DAG";
            case 4: return "MODENHED_FJERDE_DAG";
            case 5: return "MODENHED_MODEN";
            case 6: return "SOMMERFERIE";
        }
        return "FEJL: Ugyldig modenhed: "+m;
    }

    //Lyttersystem
    public static final int SYNLIGETEKSTER_OPDATERET = 1;
    public static final String ET = "SYNLIGETEKSTER_OPDATERET";

    public static final int HTEKSTER_OPDATERET = 2;
    public static final String TO = "HTEKSTER_OPDATERET";

    public static final int NYE_TEKSTER_ONLINE = 3;
    public static final String TRE = "NYE_TEKSTER_ONLINE";

    public static final int INGEN_NYE_TEKSTER_ONLINE = 4;
    public static final String FIRE = "INGEN_NYE_TEKSTER_ONLINE";

    public static final int NYE_HTEKSTER_PÅ_VEJ = 5;
    public static final String FEM = "NYE_HTEKSTER_PÅ_VEJ";

    public static final int SPROG_ÆNDRET = 6;
    public static final String SEKS = "SPROG_ÆNDRET";

    public static final int TEKSTBASEN_OPDATERET = 7;
    public static final String SYV = "TEKSTBASEN_OPDATERET";

    public static final int OFFLINE = 8;
    public static final String OTTE = "OFFLINE";



    public static String hændelsestekst(int hændelse){

        switch (hændelse){
            case 1 : return ET;
            case 2 : return TO;
            case 3 : return TRE;
            case 4 : return FIRE;
            case 5 : return FEM;
            case 6 : return SEKS;
            case 7 : return SYV;
        }
        return "FEJL: Ukendt eventtype: "+hændelse;
    }

    //Urler TODO: Obfuskeres
    public static final String henteurlDK = "http://www.lightspeople.net/sune/skole/tekster.xml";
    public static final String henteurlDE = "http://www.lightspeople.net/sune/skole/tekster_de.xml";
    public static final String versionUrl = "http://www.lightspeople.net/sune/skole/version.txt";

    //Datoer
    public static final DateTime SOMMERFERIE_START = new DateTime().withDayOfMonth(8).withMonthOfYear(6);
    public static final DateTime SOMMERFERIE_SLUT =  new DateTime().withDayOfMonth(10).withMonthOfYear(8);

    //Filnavne
    public static final String OTEKST_1 = "otekst1";
    public static final String OTEKST_2 = "otekst2";
    public static final String OTEKST_3 = "otekst3";
    public static final String ITEKSTER = "itekster";
    public static final String MTEKSTER = "mtekster";
    public static final String HTEKSTER = "htekster";
    public static final String GAMLE = "gamle";
    public static final String MASTERDATO = "masterdato";
    public static final String DATOLISTE = "datoliste";
    public static final String SYNLIGEDATOER = "synligeDatoer";
    public static final String SYNLIGETEKSTER = "synligetekster";
    public static final String TEMP_SYNLIGETEKSTER = "tempsynligeTekster";
    public static final String TEKSTVERSION = "tekstversion";

    //TODO gamle, osv..

}

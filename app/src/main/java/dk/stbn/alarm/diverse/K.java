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

    //Lyttersystem
    public static final int SYNLIGETEKSTER_OPDATERET = 1;
    public static final String ET = "SYNLIGETEKSTER_OPDATERET";

    public static final int HTEKSTER_OPDATERET = 2;
    public static final String TO = "HTEKSTER_OPDATERET";

    public static final int NYE_TEKSTER_ONLINE = 3;
    public static final String TRE = "NYE_TEKSTER_ONLINE";

    public static final int NYE_HTEKSTER_PÅ_VEJ = 4;
    public static final String FIRE = "NYE_HTEKSTER_PÅ_VEJ";

    public static String hændelsestekst(int hændelse){

        switch (hændelse){
            case 1 : return ET;
            case 2 : return TO;
            case 3 : return TRE;
            case 4 : return FIRE;
        }
        return "FEJL: Ukendt eventtype";
    }

    //Urler TODO: Obfuskeres
    public static final String henteurlDK = "http://www.lightspeople.net/sune/skole/tekster.xml";
    public static final String henteurlDE = "http://www.lightspeople.net/sune/skole/tekster_de.xml";
    public static final String versionUrl = "http://www.lightspeople.net/sune/skole/version.txt";

    //Datoer
    public static final DateTime SOMMERFERIE_START = new DateTime().withDayOfMonth(8).withMonthOfYear(6);
    public static final DateTime SOMMERFERIE_SLUT =  new DateTime().withDayOfMonth(20).withMonthOfYear(8);

    //Filnavne
    public static final String OTEKST_1 = "otekst1";
    public static final String OTEKST_2 = "otekst2";
    public static final String OTEKST_3 = "otekst3";
    public static final String ITEKSTER = "itekster";
    public static final String MTEKSTER = "mtekster";
    public static final String HTEKSTER = "htekster";
    //TODO gamle, osv..

}

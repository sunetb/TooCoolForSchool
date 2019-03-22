package dk.stbn.alarm.diverse;

public class K {

    public static final int MODENHED_HELT_FRISK  = 0;
    public static final int MODENHED_FØRSTE_DAG  = 1;
    public static final int MODENHED_ANDEN_DAG   = 2;
    public static final int MODENHED_TREDJE_DAG  = 3;
    public static final int MODENHED_FJERDE_DAG  = 4;
    public static final int MODENHED_MODEN       = 5;
    public static final int SOMMERFERIE          = 6;

    public static final int SYNLIGETEKSTER_OPDATERET = 1;
    public static final String ET = "SYNLIGETEKSTER_OPDATERET";

    public static final int HTEKSTER_OPDATERET = 2;
    public static final String TO = "HTEKSTER_OPDATERET";

    public static final int NYE_TEKSTER_ONLINE = 3;
    public static final String TRE = "NYE_TEKSTER_ONLINE";

    public static String hændelsestekst(int hændelse){

        switch (hændelse){
            case 1 : return ET;
            case 2 : return TO;
            case 3 : return TRE;
        }
        return "FEJL: Ukendt eventtype";
    }

}

package dk.stbn.alarm.diverse;

import org.joda.time.DateTime;

import dk.stbn.alarm.data.Util;

public class Tid {


    public static boolean erSammeDato(DateTime a, DateTime b){

        return kl00(a).equals(kl00(b));
    }


    public static boolean før (DateTime a, DateTime b){

        return kl00(a).isBefore(kl00(b));
    }

    public static boolean efter (DateTime a, DateTime b){

        return kl00(a).isAfter(kl00(b));
    }

    public static boolean fortid (DateTime d){

        return kl00(d).isBefore(kl00(new DateTime()));
    }

    public static boolean mellem(DateTime denne, DateTime start, DateTime slut )
    {
        return efter(denne, start) && før(denne, slut);
    }

    public static DateTime kl00(DateTime i){
        return i.withTime(0,0,0,0);
    }

    public static boolean syvDageFør(DateTime a, DateTime b) {
        return erSammeDato(kl00(a).plusDays(7), kl00(b));
    }

    static void p(Object o) {
        String kl = "Tid" + ".";
        Util.p(kl + o);
    }

    public static void testSyvdage(){
        DateTime a = new DateTime();
        DateTime b = new DateTime().plusDays(7);
        DateTime c = new DateTime().minusDays(7);
        DateTime d = new DateTime().plusDays(2);

        p("tester sydagefør...");
        if (syvDageFør(a,b)) p("Korrekt");
        if (syvDageFør(c,a)) p("Korrekt");
        if (syvDageFør(b,c)) p("Fejl i syvdagefør");
        if (syvDageFør(a,c)) p("Fejl i syvdagefør");
        if (syvDageFør(a,d)) p("Fejl i syvdagefør");
        if (syvDageFør(d,a)) p("Fejl i syvdagefør");
        if (syvDageFør(d,c)) p("Fejl i syvdagefør");
        if (syvDageFør(b,c)) p("Fejl i syvdagefør");
        p("test af syvdagefør slut");

    }
}

package dk.stbn.alarm.diverse;

import org.joda.time.DateTime;

import dk.stbn.alarm.data.Util;

public class Tid {


    public static boolean erSammeDato(DateTime a, DateTime b){

        return a.withTime(0,0,0,0).equals(b.withTime(0,0,0,0));
    }


    public static boolean før (DateTime a, DateTime b){

        return a.withTime(0,0,0,0).isBefore(b.withTime(0,0,0,0));
    }

    public static boolean efter (DateTime a, DateTime b){

        return a.withTime(0,0,0,0).isAfter(b.withTime(0,0,0,0));
    }

    public static boolean fortid (DateTime d){

        return d.withTime(0,0,0,0).isBefore(new DateTime().withTime(0,0,0,0));
    }

    public static boolean mellem(DateTime denne, DateTime start, DateTime slut )
    {
        return efter(denne, start) && før(denne, slut);
    }
}

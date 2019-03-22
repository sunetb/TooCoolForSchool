package dk.stbn.alarm.diverse;

import org.joda.time.DateTime;

public class Tid {


    public static boolean erSammeDato(DateTime tid, DateTime masterDato){

        return tid.toLocalDate().isEqual(masterDato.toLocalDate());
    }


    public static boolean før (DateTime a, DateTime b){

        return a.toLocalDate().isBefore(b.toLocalDate());
    }

    public static boolean efter (DateTime a, DateTime b){

        return a.toLocalDate().isAfter(b.toLocalDate());
    }

    public static boolean fortid (DateTime d){

        return d.toLocalDate().isBefore(new DateTime().toLocalDate());
    }

}

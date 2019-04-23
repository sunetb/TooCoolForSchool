package dk.stbn.alarm.lyttere;

import java.util.ArrayList;

import dk.stbn.alarm.data.A;
import dk.stbn.alarm.data.Tilstand;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.K;

/**
 * Created by sune on 8/23/16.
 */
public class Lyttersystem {

    boolean log = true;

    private static Lyttersystem instans;

    public static Lyttersystem getInstance(){
        if (instans == null) instans = new Lyttersystem();
        return instans;
    }

    private ArrayList<Observatør> observatører = new ArrayList<>();

    int senesteHændelse = 0;

    public void lyt(Observatør o) {
        if (!observatører.contains(o)) observatører.add(o);
    }

    public void afregistrer(Observatør o) {
        observatører.remove(o);
    }

    public void nulstil(){
        observatører.clear();
    }

    int getSenesteHændelse(){
        return senesteHændelse;
    }
    void setSenesteHændelse (int hændelse) {
        senesteHændelse = hændelse;
    }

    /**
     * Må KUN kaldes fra hovedtråden
      */
    public void givBesked(int hændelse, String besked) {
        if (log) p("1: givebesked MODTOG fra: "+besked+ ". Hændelse: "+K.hændelsestekst(hændelse));

        //setSenesteHændelse(hændelse);

        for (Observatør o: observatører) {
            o.opdater(hændelse);
        }
        //Util.baglog("Lyttersystem.givBesked(): Modenhed = "+ K.hændelsestekst(hændelse), A.a);
    }



    void p(Object o){
        Util.p("Lyttersystem."+o);
    }
}

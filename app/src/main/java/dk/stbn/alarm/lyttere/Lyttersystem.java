package dk.stbn.alarm.lyttere;

import java.util.ArrayList;

import dk.stbn.alarm.data.A;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.K;

/**
 * Created by sune on 8/23/16.
 */
public class Lyttersystem {

    private static Lyttersystem instans;

    public static Lyttersystem getInstance(){
        if (instans == null) return new Lyttersystem();
        else return instans;
    }

    ArrayList<Observatør> observatører = new ArrayList<>();

    int senesteHændelse = 0;

    int tæller= 0;

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

    // -- Må KUN kaldes fra hovedtråden
    public void givBesked(int hændelse, String besked, int id) {
        p("1: givebesked MODTOG: : "+besked+ " hændelse: "+K.hændelsestekst(hændelse)+ " id: "+id);
        //senesteHændelse =hændelse;
        setSenesteHændelse(hændelse);

        if (hændelse == K.HTEKSTER_OPDATERET) A.hteksterKlar= true;

     //   new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { //-- Sikrer at den køres i hovedtråden
       //     @Override
         //   public void run() {
           //     tæller = 0;
                for (Observatør o: observatører) {
                    o.opdater(hændelse);
             //       tæller++;
                }
                p("2: givBesked() SENDTE "+getSenesteHændelse() + ": "+ K.hændelsestekst(getSenesteHændelse())+" " + " tråd: "+Thread.currentThread().getName());
                //senesteHændelse= 0;
         //   }
       // },0);
        p("3: Hændelse: "+hændelse + " VS. senesteHændelse: "+getSenesteHændelse()+ " id: "+id);
        Util.baglog("Lyttersystem.givBesked(): Modenhed = "+ K.hændelsestekst(hændelse), A.a);
    }



    void p(Object o){
        Util.p("Lyttersystem."+o);
    }
}

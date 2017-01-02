package dk.stbn.cool.lyttere;

import java.util.ArrayList;

import dk.stbn.cool.data.A;
import dk.stbn.cool.data.Util;

/**
 * Created by sune on 8/23/16.
 */
public class Lyttersystem {

    static ArrayList<Observatør> observatører = new ArrayList<>();

    static int senesteHændelse = 0;

    static int tæller= 0;

    public static final int SYNLIGETEKSTER_OPDATERET = 1;
    static final String ET = "SYNLIGETEKSTER_OPDATERET";

    public static final int HTEKSTER_OPDATERET = 2;
    static final String TO = "HTEKSTER_OPDATERET";

    public static final int NYE_TEKSTER_ONLINE = 3;
    static final String TRE = "NYE_TEKSTER_ONLINE";



    //static final int  = ;



    public static void lyt(Observatør o) {
        if (!observatører.contains(o)) observatører.add(o);
    }

    public static void afregistrer(Observatør o) {
        observatører.remove(o);
    }

    public static void nulstil(){
        observatører.clear();
    }

    static int getSenesteHændelse(){
        return senesteHændelse;
    }
    static void setSenesteHændelse (int hændelse) {
        senesteHændelse = hændelse;
    }

    // -- Må KUN kaldes fra hovedtråden
    public static void givBesked(int hændelse, String besked, int id) {
        p("1: givebesked MODTOG: : "+besked+ " hændelse: "+hændelsestekst(hændelse)+ " id: "+id);
        //senesteHændelse =hændelse;
        setSenesteHændelse(hændelse);

        if (hændelse == HTEKSTER_OPDATERET) A.hteksterKlar= true;

     //   new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { //-- Sikrer at den køres i hovedtråden
       //     @Override
         //   public void run() {
           //     tæller = 0;
                for (Observatør o: observatører) {
                    o.opdater(hændelse);
             //       tæller++;
                }
                p("2: givBesked() SENDTE "+getSenesteHændelse() + ": "+ hændelsestekst(getSenesteHændelse())+" " + " tråd: "+Thread.currentThread().getName());
                //senesteHændelse= 0;
         //   }
       // },0);
        p("3: Hændelse: "+hændelse + " VS. senesteHændelse: "+getSenesteHændelse()+ " id: "+id);
    }

    public static String hændelsestekst(int hændelse){

        String tekst = "FEJL: Ukendt eventtype";

        switch (hændelse){
            case 1 : return ET;
            case 2 : return TO;
            case 3 : return TRE;
        }

        return tekst;

    }

    static void p(Object o){
        Util.p("Lyttersystem."+o);
    }
}

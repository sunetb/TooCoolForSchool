package dk.stbn.cool;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

/**
 * Created by sune on 8/23/16.
 */
public class Lyttersystem {

    static ArrayList<Observatør> observatører = new ArrayList<>();
    static int senesteEvent = 0;
    static int tæller= 0;

    static final int SYNLIGETEKSTER_OPDATERET = 1;
    static final String ET = "SYNLIGETEKSTER_OPDATERET";

    static final int HTEKSTER_OPDATERET = 2;
    static final String TO = "HTEKSTER_OPDATERET";

    //static final int  = ;



    static void lyt(Observatør o) {
        observatører.add(o);
    }

    static void afregistrer(Observatør o) {
        observatører.remove(o);
    }

    static void givBesked (int event) {
        senesteEvent =event;

        p("givbesked() blev kaldt med "+event + ": "+eventTekst(event));

        if (event == HTEKSTER_OPDATERET) A.hteksterKlar= true;

        new Handler(Looper.getMainLooper()).post(new Runnable() { //-- Sikrer at den køres i hovedtråden
            @Override
            public void run() {
                tæller = 0;
                for (Observatør o: observatører) {
                    o.opdater(senesteEvent);
                    tæller++;
                }
                p("opdater() kaldt "+tæller+ " gange. ");
            }
        });
    }

    static String eventTekst (int event){

        String eventTekst = "FEJL: Ukendt eventtype";

        switch (event){
            case 1 : return ET;
            case 2 : return TO;
        }

        return eventTekst;

    }

    static void p(Object o){
        String kl = "Lyttersystem.";
        Util.p(kl+o);
    }
}

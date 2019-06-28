package dk.stbn.alarm.data;

import android.content.Context;
import android.content.ContextWrapper;

import org.joda.time.DateTime;

import java.util.ArrayList;

import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;

public class Tekstlogik {



    private static Tekstlogik tl;

    private Tekstlogik(){

    }

    public static Tekstlogik getInstance(){
        if (tl == null) tl = new Tekstlogik();
        return tl;
    }



    /**
     * Udvælger tekster på baggrund af modenhed og om der er ny version på nettet
     */
    public void udvælgTekster(Context c) {
        int modenhed = Tilstand.getInstance(c).modenhed;

        ArrayList<Tekst> tempSynlige = new ArrayList<>();

        if (modenhed == K.SOMMERFERIE) {
            p("sommerferie!!!");

            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_1, c));
            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_2, c));
            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_3, c));
//GEM DETTE
        } else if (modenhed == K.MODENHED_HELT_FRISK) {
            p("udvælgTekster() Modenhed: Helt frisk");
            allerFørsteGang();
            IO.gemObj(new DateTime(), K.MASTERDATO, c);

        } else if (modenhed == K.MODENHED_FØRSTE_DAG) {
            p("Dag 1, ikke første gang");

            tempSynlige.add((Tekst) IO.læsObj(K.OTEKST_1, c));

        } else if (modenhed == K.MODENHED_ANDEN_DAG) {
            p("Dag 2 ");

            Tekst oTekst1 = (Tekst) IO.læsObj(K.OTEKST_1, c);
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, c);
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);
//hertil
        } else if (modenhed == K.MODENHED_TREDJE_DAG) {
            p("Dag 3 ");

            Tekst oTekst1 = (Tekst) IO.læsObj(K.OTEKST_1, c);
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, c);
            tempSynlige.add(oTekst1);
            tempSynlige.add(oTekst2);

            ArrayList<Tekst> tekster = findItekster(c);
            if (tekster.size() > 0)
                tempSynlige.add(tekster.get(0));

        } else if (modenhed == K.MODENHED_FJERDE_DAG) {
            p("Dag 4 ");
            Tekst oTekst2 = (Tekst) IO.læsObj(K.OTEKST_2, c);


            tempSynlige.add(oTekst2);
            ArrayList<Tekst> tekster = findItekster(c);
            //Tag kun de første to I-tekster, så der vises tre i alt
            if (tekster.size() > 0) tempSynlige.add(tekster.get(0));
            if (tekster.size() > 1) tempSynlige.add(tekster.get(1));

            //sørger for at der altid er tre tekster, også lige efter sommerferien
            if (tempSynlige.size() == 2)
                tempSynlige.add(0, (Tekst)IO.læsObj(K.OTEKST_1, c));

        } else if (modenhed == K.MODENHED_MODEN) {
            p("Dag 5: MODEN ");

            ArrayList<Tekst> itekster = findItekster(c);

            //Særtilfælde: er appen ung og har kun én eller to I-tekster?
            if (itekster.size() == 1) {
                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_1, c));
                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_2, c));
            }
            if (itekster.size() == 2) {

                tempSynlige.add((Tekst)IO.læsObj(K.OTEKST_2, c));
            }
            tempSynlige.addAll(itekster);
            ArrayList<Tekst> mtekster = findMtekster();
            tempSynlige.addAll(mtekster);

        }

        p("Tjekker om de cachede tekster skal erstattes..");

        //Der skal skiftes hvis gemt liste og ny liste er forskellig længde
        boolean skift = synligeTekster.size() != tempSynlige.size();

        //Der skal skiftes hvis appen ikke er moden
        if (modenhed != K.MODENHED_MODEN) skift = true;

        //Er appen moden og har listerne samme længde, må vi tjekke indholdet af listerne
        if (!skift) {
            boolean forskellige = false;

            for (int i = 0; i < synligeTekster.size(); i++) {
                Tekst synlig = synligeTekster.get(i);
                Tekst temp = tempSynlige.get(i);
                if (synlig.id_int != temp.id_int) {
                    forskellige = true;
                    break;
                }
            }


            skift = forskellige;
        }

        if (skift) {
            p("JA. Der er  et nyt udvalg af tekster");
            synligeTekster = tempSynlige;
            Tilstand.getInstance(c).pref.edit().putInt("senesteposition", -1).commit(); //Sætter ViewPagerens position til nyeste element
            lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "udvælgTekster(), der var et nyt udvalg");
            gemSynligeTekster();
        } else
            p("NEJ. Vi bruger de cachede tekster");

        if (modenhed < K.MODENHED_TREDJE_DAG){
            //sørg for at der ikke vises notifikationer i starten
            for (Tekst t : synligeTekster)
                IO.føjTilGamle(t.id_int, c);
        }

        p("udvælgTekster() færdig");
    }
    /**
     * Finder de I-tekster som skal vises idag
     * @return array med tekster
     *
     */
    ArrayList<Tekst> findItekster(Context c) {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> itekster = (ArrayList<Tekst>) IO.læsObj(K.ITEKSTER, c);

        Tekst dummyITekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "i", a.tilstand.masterDato);
        dummyITekst.lavId();

        a.p("Tjek dummytekst id: " + dummyITekst.id_int);
        a.p("itekster længde: " + itekster.size());

        boolean iFundet = false;
        for (int i = 0; i < itekster.size(); i++) {
            Tekst itekst = itekster.get(i);
            int tekstid = itekst.id_int;
            a.p("Tjek Itekster: " + tekstid);
            a.p("IdTekst: " + itekst.id);

             //Tjek om teksten skal vises
            if (!iFundet && tekstid >= dummyITekst.id_int) {

                if (tekstid == dummyITekst.id_int) {
                    a.p("Itekst eksakt match");
                    iFundet = true;

                    if (i > 1) r.add(itekster.get(i - 2));
                    if (i > 0) r.add(itekster.get(i - 1));
                    r.add(itekster.get(i));


                } else {
                    a.p("I ineksakt match");
                    iFundet = true;
                    if (i > 2) r.add(itekster.get(i - 3));
                    if (i > 1) r.add(itekster.get(i - 2));
                    if (i > 0) r.add(itekster.get(i - 1));
                    else r.add(itekster.get(i));
                }

            }


        }
        if (!iFundet){
            a.p("Appen er løbet tør for tekster (Snart sommerferie)");
            int længde = itekster.size();
            r.add(itekster.get(længde-3));
            r.add(itekster.get(længde-2));
            r.add(itekster.get(længde-1));

        }
        return r;


    }


    /**
     * Finder de M-tekster som skal vises idag
     * @return
     */
    private ArrayList<Tekst> findMtekster(Context c) {
        ArrayList<Tekst> r = new ArrayList<>();
        ArrayList<Tekst> mtekster = (ArrayList<Tekst>) IO.læsObj(K.MTEKSTER, c);

        Tekst dummyMTekst = new Tekst("DummyOverskrift", "DummyBrødtekst", "m", Tilstand.getInstance(c).masterDato);
        dummyMTekst.lavId();
        p("Tjek M dummytekst id: " + dummyMTekst.id_int);

        boolean mFundet = false;

        p("Mtekster længde: " + mtekster.size());

        for (int i = 0; i < mtekster.size(); i++) {
            Tekst mtekst = mtekster.get(i);
            p("tjek mtekster: " + mtekst.id_int);
            p("IdTekst: " + mtekst.id);

            if (mtekst.id_int >= dummyMTekst.id_int) {

                if (!mFundet) {

                    if (mtekst.id_int == dummyMTekst.id_int) {
                        p("Eksakt match Mtekst");
                        r.add(mtekst);

                    } else if (AlarmLogik.getInstance().visMtekst(mtekst.dato, Tilstand.getInstance(c).masterDato)) {
                        r.add(mtekst);
                        p("Mtekst ineksakt match --");
                    }
                    mFundet = true;
                }
            }
        }
        return r;

    }

    void p(Object o) {
        String kl = "Tekstlogik.";
        Util.p(kl + o);
    }

}

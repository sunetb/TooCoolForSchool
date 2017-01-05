package dk.stbn.cool.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import dk.stbn.cool.Tekst;
import dk.stbn.cool.diverse.IO;
import dk.stbn.cool.lyttere.Alarm_Lytter;
import dk.stbn.cool.lyttere.Boot_Lytter;

/**
 * Created by sune on 6/3/16.
 */
public class Util {

    static long starttid = 0;

    static double tid (){
        return (double) (System.currentTimeMillis()-starttid)/1000.0;
    }

    public static void notiBrugt(Context c, Intent intent){
        p("Util.notiBrugt kaldt");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

        //tjek om første opstart. Hmm ER det nu også nødvendigt?
        boolean førsteOpstart = sp.getBoolean("førstegang", true);

        if (førsteOpstart) IO.gemObj(new ArrayList<Integer>(), "gamle", c);

        sp.edit().putBoolean("førstegang", false).commit();
        String id = "";
        id = intent.getExtras().getString("tekstId");
        int id_int = intent.getExtras().getInt("id_int", 0);

        p("Util.notiBrugt modtog: id: "+id + "id_int: "+id_int);

        IO.føjTilGamle(id_int, c);
        p("Util.notiBrugt tjek sættet:");

        PendingIntent i = PendingIntent.getBroadcast(c, id_int, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alm = A.alm;
        if (alm == null) alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        else p("Util.notiBrugt(): alarmManager eksisterer");
        i.cancel();
        alm.cancel(i);
    }

    static void startAlarm (Context c, Tekst t) {
        p("Util.startAlarm() modtog "+t.overskrift);

        ComponentName receiver = new ComponentName(c, Boot_Lytter.class);
        PackageManager pm = c.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        AlarmManager alarmMgr = A.alm;
        PendingIntent alarmIntent;

        if (alarmMgr == null)  alarmMgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        else p("Util.startAlarm()  alarmManager eksisterer");
        Intent intent = new Intent(c, Alarm_Lytter.class);

        intent.putExtra("id_int", t.id_int);
        intent.putExtra("tekstId", t.id);
        intent.putExtra("overskrift", t.overskrift);
        String action = ""+t.id_int;
        if (t.kategori.equals("mgentag")) action+="gentag"; //--M-tekster har TO notifikationer: en syv dage før og en på dagen
        intent.setAction(action); //Fjollet hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig
        alarmIntent = PendingIntent.getBroadcast(c, t.id_int, intent,  PendingIntent.FLAG_CANCEL_CURRENT);

        p("Util.startAlarm()      Dato: "+t.dato.toString());
        p("Util.startAlarm() Dags Dato: "+A.masterDato);

        alarmMgr.set(AlarmManager.RTC, t.dato.getMillis(), alarmIntent);
    }

    public static void opdaterKalender(Context c, String kaldtfra){
        p("opdaterKalender() kaldt fra "+kaldtfra);
        ArrayList<Integer> gamle = (ArrayList<Integer>) IO.læsObj("gamle", c);
        System.out.println("opdaterKalender() tjek sættet:");
        for (Integer s : gamle) System.out.println(s);

        ArrayList<Integer> datoliste = (ArrayList<Integer>) IO.læsObj("datoliste", c);

        for (int i = 0 ; i < datoliste.size(); i++){

            if (!gamle.contains((datoliste.get(i)))) {
                Tekst t = (Tekst) IO.læsObj(""+datoliste.get(i), c);
                if (t.id_int <300000000){  //Hvis I-tekst

                    if (t.dato.isBeforeNow())
                        gamle.add(t.id_int);
                    else
                        Util.startAlarm(c,t);
                }
                else{ //-- Hvis m-tekst
                    if(t.dato.isBeforeNow())
                        gamle.add(t.id_int);
                    else {
                        //-- M-tekster har både notifikation på dagen ...
                        Util.startAlarm(c,t);

                        //-- ...og syv dage før
                        Tekst temp = t;
                        temp.dato = temp.dato.minusDays(7);
                        temp.kategori="mgentag";
                        Util.startAlarm(c,temp);

                    }
                }

            }
            else p("Noti "+datoliste.get(i)+" er allerede brugt");

        }

        IO.gemObj(gamle, "gamle", c);
    }

    static boolean visMtekst(DateTime mTid){
        String logbesked = "Util.visMtekst() "+ mTid.getDayOfMonth()+ "/"+mTid.getMonthOfYear();
        //-- Eks: 11 september     ///Vises                    5, 6, 7, 8, 9, 10, 11
                                   ///Vises ikke: 1, 2, 3. 4.                         12, 13, 14, sept

        //-- Tjek om  m-dato er idag
        if (erSammeDato(mTid)) return true;


        //-- Tjek om idag er 12 sept eller efter.
        if (mTid.isBefore(A.masterDato)) return false;

        //-- Tjek om idag er 4. sept eller tidligere
        DateTime syvFør = mTid.minusDays(7);
        p(logbesked + " dato var mindre end en uge gammel. Skal den vises? "+!syvFør.isAfter(A.masterDato));

        return !syvFør.isAfter(A.masterDato);
    }

    static boolean erSammeDato(DateTime tid){
        //-- Sammenligner en DateTime med dags dato men ignorerer klokkelæt (og årstal)
        int dag = tid.getDayOfMonth();
        DateTime nu = A.masterDato;
        int idagD = nu.getDayOfMonth();

        if (dag != idagD) return false;

        int mrd = tid.getMonthOfYear();
        int idagMrd = nu.getMonthOfYear();

        return mrd == idagMrd;
    }

    static ArrayList[] parseXML (String xml, String kaldtFra) {
        p("Util.parseXML kaldt fra "+kaldtFra);

        ArrayList<Tekst> oteksterTmp = new ArrayList<Tekst>();
        ArrayList<Tekst> teksterTmp = new ArrayList<Tekst>();
        ArrayList<Tekst> mteksterTmp = new ArrayList<Tekst>();
        ArrayList<Tekst> hteksterTmp = new ArrayList<Tekst>();
        DateTime idag = A.masterDato;
        try {

            XmlPullParser parser;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            boolean startnu= false;
            int eventType = parser.getEventType();
            String tag = "";
            String txt = "";
            String put = "";
            int celletæller=0;

            Tekst tempTekst = new Tekst();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    tag = parser.getName();
                    if ("ss:Worksheet".equals(tag) ) startnu=true;
                    if(startnu) {
                       if ("Cell".equals(tag)) {
                            celletæller++;
                            //   p("____<Cell> start "+celletæller);
                        } else if ("Row".equals(tag)) {
                            //  p("<Row>");

                            tempTekst = new Tekst();
                            //tempTekst = new Tekst();
                        }
                    }
                }//END IF START_TAG

                else if (startnu && parser.getEventType() == XmlPullParser.TEXT) {
                    txt = parser.getText();
                    put+=txt;

                }//END IF TEXT

                else if (startnu && eventType == XmlPullParser.END_TAG){
                    tag = parser.getName();
                   if ("Cell".equals(tag)){
                        if (celletæller == 1){

                            tempTekst.kategori = txt;

                           if ("h".equalsIgnoreCase(txt)){
                                tempTekst.kategori = "h";
                            }
                            if ("o".equalsIgnoreCase(txt)){
                                tempTekst.kategori = "o";
                            }
                            else if ("stop".equalsIgnoreCase(txt)){
                                p("parseXML() stop fundet");


                                break;
                            }
                            else {
                               // p("Værdi: "+txt);
                                String type= txt.substring(0,1);
                                tempTekst.kategori = type;
                               // p("Type: "+type);
                                if (txt.length() > 1) {
                                    String dato = txt.substring(1);
                                    int længde = dato.length();
                                    if (længde >4) {
                                        dato = dato.substring(0,længde-4);
                                        længde = dato.length();
                                        p("ParseXML lang dato læst: "+dato);

                                    }
                                    String tmpMåned = dato.substring(længde-2, længde);
                                    int måned = tryParseInt(tmpMåned);
                                    String tmpDag = dato.substring(0, længde-2);
                                    int dag = tryParseInt(tmpDag);

                                    if (måned > 12) { //Fejl: dag og måned er måske ombyttet
                                        if (dag < 13) {
                                            int tmpTmpMåned = dag;
                                            dag = måned;
                                            måned = tmpTmpMåned;
                                        }
                                        else {
                                            p("Util.parseXML(): FEJL: Ugyldig dato: "+txt);
                                        }
                                    }

                                    //vi regner selv ud hvilket år vi skal skrive, så vi kan spare opdatering af datafilen
                                    int år = idag.getYear();
                                    int måneddd = idag.getMonthOfYear();
                                    boolean andetHalvår = måneddd > 6 ;
                                    boolean førsteHalvår = måneddd < 7;
                                    if (andetHalvår && måned < 7) år++; //Vi er i efteråret, og tekster fra og med januar skal være næste år
                                    else if (førsteHalvår && måned > 6) år--; //vi er i foråret og tekster juli-dec skal være sidste år

                                    tempTekst.dato = new DateTime(år,måned,dag,0,0);

									
                                }
                            }
                        }
                        else if (celletæller == 2){

                            tempTekst.overskrift = put;
                        }
                        else if (celletæller == 3) {

                            put  = put.replaceFirst("<title", "<!--title")
                                    .replaceFirst("</title>", "<title-->");

                            if (tempTekst.kategori.equalsIgnoreCase("h"))
                                put =  put.replaceFirst("<body", "<body style=\"color: yellow; background-color: black;\"");
                            else
                                put = put.replaceFirst("<body", "<body style=\"color: white; background-color: black;\"");

                            tempTekst.brødtekst = put;
						    tempTekst.lavId();
							
                            if      (tempTekst.kategori.equalsIgnoreCase("o")) oteksterTmp.add(tempTekst);
                            else if (tempTekst.kategori.equalsIgnoreCase("i")) teksterTmp.add(tempTekst);
                            else if (tempTekst.kategori.equalsIgnoreCase("m")) mteksterTmp.add(tempTekst);
                            else if (tempTekst.kategori.equalsIgnoreCase("h")) hteksterTmp.add(tempTekst);

                            
                        }

                        put="";
                    }
                    else if ("Row".equals(tag)) {
                        celletæller =0;
                    }
                }// END IF END_TAG

                eventType = parser.next();

            }//end while

        } catch (XmlPullParserException e) {
            e.printStackTrace();p(e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            p(ex.getMessage());}

        ArrayList[] data = {oteksterTmp, teksterTmp, mteksterTmp, hteksterTmp};
        p("Util.parseXML(): Data længde: "+ data.length + " | o: "+oteksterTmp.size() + " | i: "+teksterTmp.size() + " | m: "+mteksterTmp.size() + " | h: "+hteksterTmp.size());
        return data;
    }

	static int lavDato (DateTime d) {
		
		String dato = ""+d.getDayOfMonth();
		int m = d.getMonthOfYear();
		if (m<10) dato +="0";
		dato += m+""+d.getYear();
		return tryParseInt(dato);
	}

    static ArrayList<Tekst> sorterStigende (ArrayList<Tekst> ind){
        ArrayList<Tekst> tempListe = new ArrayList<Tekst>();

        while(ind.size()>0){
            Tekst a = ind.get(0);
            int ix = 0;

            for (int i = 0; i < ind.size() - 1; i++) {

                Tekst b = ind.get(i + 1);
                if (a.id_int > b.id_int) {
                    a = b;
                    ix=i+1;
                }
            }
            tempListe.add(a);

            ind.remove(ix);
        }
        return tempListe;
    }

    static String inputStreamSomStreng(InputStream is) throws IOException {
        char[] buffer = new char[1024];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, "UTF-8");
        int read;
        do {
            read = in.read(buffer, 0, buffer.length);
            if (read>0) {
                out.append(buffer, 0, read);
            }
        } while (read>=0);
        in.close();
        return out.toString();
    }

    static Integer tryParseInt (String text) {
        try {
            return new Integer(text);
        } catch (NumberFormatException e) {

            return null;
        }
    }

    static ArrayList<Tekst> erstatAfsnit(ArrayList<Tekst>  input){
        ArrayList<Tekst> temp = new ArrayList<Tekst>();

        for (Tekst t : input) {
            String nyBrødtekst = t.brødtekst.replaceAll("\n", " ");

            t.brødtekst = nyBrødtekst;
            temp.add(t);
        }

        return temp;
    }

    public static void t(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    public static void p(Object o){
        String kl = o +"   #t:" + tid();
        System.out.println("_____"+kl);
        A.debugmsg += kl +"<br>";
    }

    public static void kørIBaggrund(Runnable r) {
        new Thread(r).start();
    }


}

package dk.stbn.alarm.data;

import android.content.Context;
import android.content.SharedPreferences;
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

/**
 * Created by sune on 6/3/16.
 */
public class Util {

    static long starttid = 0;

    static double tid (){
        return (double) (System.currentTimeMillis()-starttid)/1000.0;
    }

    public static boolean baglog = true; //Kun til test


    /**
     * Ikke kompatibel med testmetoden rul(int)
     * @param xml
     * @param kaldtFra
     * @return
     */
    static ArrayList[] parseXML (String xml, String kaldtFra) {
        p("Util.parseXML kaldt fra "+kaldtFra);

        ArrayList<Tekst> oteksterTmp = new ArrayList<Tekst>();
        ArrayList<Tekst> teksterTmp = new ArrayList<Tekst>();
        ArrayList<Tekst> mteksterTmp = new ArrayList<Tekst>();
        ArrayList<Tekst> hteksterTmp = new ArrayList<Tekst>();

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
                                p("Util.parseXML() Otekst fundet");
                            }
                            else if ("stop".equalsIgnoreCase(txt)){
                                p("Util.parseXML() stop fundet");
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
                                        p("Util.ParseXML lang dato læst: "+dato);

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
                                    DateTime idag = new DateTime();
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
                                    .replaceFirst("</title>", "<title-->").replaceAll("\"#212121\"", "white").replaceAll("#000000", "#ffffff");

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

            return -1;
        }
    }

    /*static ArrayList<Tekst> erstatAfsnit(ArrayList<Tekst>  input){
        ArrayList<Tekst> temp = new ArrayList<Tekst>();

        for (Tekst t : input) {
            String nyBrødtekst = t.brødtekst.replaceAll("\n", " ");

            t.brødtekst = nyBrødtekst;
            temp.add(t);
        }

        return temp;
    }*/

    public static void t(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    public static void p(Object o){
        String kl = o +"   #t:" + tid();
        System.out.println("________."+kl);
        A.debugmsg += kl +"<br>";
    }

    public static void baglog(String msg, Context c){
        
        if (baglog) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
            String log = sp.getString("baggrundslog", "");
            log += msg + "\n";
            sp.edit().putString("baggrundslog", log).apply();
        }
    }

    public static void skrivBaglog (Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String log = "%%%%%%%%%%%%%%%%%%%% Baggrundslog %%%%%%%%%%%%%%%%%%%%\n" + sp.getString("baggrundslog", "");
        p(log);
    }

    public static String getBaglog (Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        String log = "%%%%%%%%%%%%%%%%%%%% Baggrundslog %%%%%%%%%%%%%%%%%%%%\n" + sp.getString("baggrundslog", "");
        return log;
    }





}

package com.example.cool;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.*;
import java.util.*;
import org.joda.time.*;
import org.xmlpull.v1.*;

/**
 * Created by sune on 6/3/16.
 */
public class Util {


    static long starttid = 0;

    static double tid (){
        return (double) (System.currentTimeMillis()-starttid)/1000.0;
    }

    static void notiBrugt(Context c, Intent intent){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

        //tjek om første opstart
        boolean førsteOpstart = sp.getBoolean("førstegang", true);
        ArrayList<String> gamle;

        //Set<String> brugteNotifikationer = sp.getStringSet("gamle", new HashSet<String>());
        if (førsteOpstart) gamle = new ArrayList<>();
        else gamle = (ArrayList<String>) IO.læsObj("gamle", c);

        sp.edit().putBoolean("førstegang", false).apply();

        String id = intent.getExtras().getString("tekstId", "");
        int id_int = intent.getExtras().getInt("id_int", 0);

        p("Util.notiBrugt modtog: id: "+id + "id_int: "+id_int);

        boolean føradd = gamle.contains(id);
        gamle.add(id);
        //sp.edit().putStringSet("gamle", gamle).apply();
        IO.gemObj(gamle, "gamle", c);
        boolean efteradd = gamle.contains(id);

        p("Util.notiBrugt tjek sættet:");
        for (String s : gamle) System.out.println(s);

        t(c,"notiBrugt("+id+") før add: "+føradd+ "efter add: "+efteradd);
        p("Util.notiBrugt("+id+") før add: "+føradd+ "efter add: "+efteradd);

        PendingIntent i = PendingIntent.getBroadcast(c, id_int, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alm = A.alm;
        if (alm == null) alm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        else p("Util.notiBrugt(): alarmManager ekisterer");
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
        //intent.setAction("com.example.notitest.START_ALARM"); //Fjollet hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig
//com.example.notitest.START_ALARM
        //int id_int = Util.tekstTilTal(id);

        alarmIntent = PendingIntent.getBroadcast(c, t.id_int, intent,  PendingIntent.FLAG_UPDATE_CURRENT);

        p("Util.startAlarm() Dato: "+t.dato.toString());

        alarmMgr.set(AlarmManager.RTC, t.dato.getMillis(), alarmIntent);
    }

    static void opdaterKalender(Context c){
        p("opdaterKalender() kaldt");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);

        //Set<String> gamle = pref.getStringSet("gamle", new HashSet<String>());
        ArrayList<String> gamle = (ArrayList<String>) IO.læsObj("gamle", c);

        System.out.println("opdaterKalender() tjek sættet:");
        for (String s : gamle) System.out.println(s);

        int iLængde = pref.getInt("teksterLængde", 0);
        int mLængde = pref.getInt("mteksterLængde", 0);

        //Evt if ilængde == 0...else
        for (int i = 0; i <iLængde; i++) {
            String id = pref.getString("i"+i, "fejl");
            if (id.equals("fejl")) p("Fejl ved indlæsning af itekst nr "+i);
            else if (!gamle.contains(id)) {
                Tekst t = (Tekst) IO.læsObj(id, c);
                p("opdaterKalender() init "+id);
                Util.startAlarm(c,t);
            }
            else p("Noti "+id+" er allerede brugt");
        }

        //Evt if mlængde == 0... else
        for (int j = 0; j <mLængde; j++) {
            String id = pref.getString("m"+j, "fejl");

            if (id.equals("fejl")) p("Fejl ved indlæsning af mtekst nr "+j);

            if (!gamle.contains(id)) {
                Tekst t = (Tekst) IO.læsObj(id, c);
                Util.startAlarm(c,t);
            }
            else p("Noti "+id+" er allerede brugt");
        }
    }


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

            while (eventType != parser.END_DOCUMENT) {
                if (eventType == parser.START_TAG) {
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

                else if (startnu && parser.getEventType() == parser.TEXT) {
                    txt = parser.getText();
                    put+=txt;

                }//END IF TEXT

                else if (startnu && eventType == parser.END_TAG){
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

                                    String tmpÅr = dato.substring(længde - 4, længde);
                                    int år = tryParseInt(tmpÅr);
                                   // p("År: "+år);
                                    String tmpMåned = dato.substring(længde-6, længde-4);
                                    int måned = tryParseInt(tmpMåned);
                                    //p("Måned: "+måned);
                                    String tmpDag = dato.substring(0, længde-6);
                                    int dag = tryParseInt(tmpDag);
                                    //p("Dag: "+dag);

                                    if (måned > 12) { //Fejl: dag og måned er ombyttet
                                        if (dag < 13) tempTekst.dato = new DateTime(år,dag,måned,1,0);
                                        else {
                                            p("Util.parseXML(): FEJL: Ugyldig dato");
                                        }
                                    }
                                    else tempTekst.dato = new DateTime(år,måned,dag,0,0);
                                    // DateTime()
									
                                }
                            }
                        }
                        else if (celletæller == 2){

                            tempTekst.overskrift = put; //Hmmm else her?
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
                            else if (tempTekst.kategori.equalsIgnoreCase("h")) {

                               /* //MEGET ad hoc
                                String nybrød = "<html><head><meta content=\"text/html; charset=ISO-8859-1\"http-equiv=\"content-type\"></head><body style=\"color: white; background-color: black;\">"+
                                        tempTekst.brødtekst+
                                        "</body></html>";
                                 tempTekst.brødtekst = nybrød;
                                //hertil
                                */

                                hteksterTmp.add(tempTekst);
                            }
                            
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


	static int lavDato (Date d) {
		
		String dato = ""+d.getDay();
		int m = d.getMonth()+1;
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

        /*
        p("sorterStigende() tjekker lister:");
         p("ind:");
         for (Tekst t: ind) p(t.datokode());
         p("Ud: ");
       for  (Tekst t: tempListe) p(t.datokode());
        */

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
/*
    static DateTime idTilDato (int id){
        int i = -1;
        if (id > 300000000) i = id/300000000;
        else i = id/200000000;
        p("idTilDato(): "+i);
        String s = ""+i;
        int å = tryParseInt(s.substring(0,3));
        int m = tryParseInt(s.substring(4,5));
        int d = tryParseInt(s.substring(6,7));

        return new DateTime(å, m, d, 0, 0);
    }
*/
    static Integer tryParseInt (String text) {
        try {
            return new Integer(text);
        } catch (NumberFormatException e) {

            return null;
        }
    }


    /*

      static int tekstTilTal (String tekst) {

        int l = tekst.length();
        String udtekst = "";
        for (int i = 0;i<l; i++) {
            char a = tekst.charAt(i);
            int k = Character.getNumericValue(a);
            udtekst += ""+k;

        }
        return tryParseInt(udtekst);

    }

    public static void gemTekstliste (ArrayList<Tekst> liste, String filename, Context c) {
        p("Util.gemTekstliste("+filename+")");


        File directory = new File(c.getFilesDir().getAbsolutePath() + File.separator + "filer");

        if (!directory.exists()) {
            directory.mkdirs();
        }
        ObjectOutput out = null;

        try {
            out = new ObjectOutputStream(new FileOutputStream(directory+ File.separator + filename));
            out.writeObject(liste);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();p(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();p(e.getMessage());
        }
    }

    static ArrayList<Tekst> hentTekstliste (String filename, Context c) {
        p("Util.hentTekstliste("+filename+")");
        ObjectInputStream input = null;
        ArrayList<Tekst> mitArray = null;
        File directory = new File(c.getFilesDir().getAbsolutePath()+ File.separator + "filer");

        if (directory.exists()){

            try {
                input = new ObjectInputStream(new FileInputStream(directory+ File.separator + filename));
                mitArray = (ArrayList<Tekst>) input.readObject();
                input.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (mitArray != null)  p("læsTekstliste_længde: "+mitArray.size());

        return mitArray;
    }

    public static void gemTekst (Tekst t, String filename, Context c) {
        p("gemTekst("+filename+")");
        File directory = new File(c.getFilesDir().getAbsolutePath() + File.separator + "filer");

        if (!directory.exists()) {
            directory.mkdirs();
        }
        ObjectOutput out = null;

        try {
            out = new ObjectOutputStream(new FileOutputStream(directory+ File.separator + filename));
            out.writeObject(t);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();p(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();p(e.getMessage());
        }
    }

    static Tekst hentTekst (String filename, Context c) {
        p("hentTekst("+filename+")");
        ObjectInputStream input = null;
        Tekst minTekst = null;
        File directory = new File(c.getFilesDir().getAbsolutePath()+ File.separator + "filer");

        if (directory.exists()){

            try {
                input = new ObjectInputStream(new FileInputStream(directory+ File.separator + filename));
                minTekst = (Tekst) input.readObject();
                input.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (minTekst != null)  p("hentTekst(): Teksten var null)");

        return minTekst;
    }
*/
    static ArrayList<Tekst> erstatAfsnit(ArrayList<Tekst>  input){
        ArrayList<Tekst> temp = new ArrayList<Tekst>();

        for (Tekst t : input) {
            String nyBrødtekst = t.brødtekst.replaceAll("\n", " ");

            t.brødtekst = nyBrødtekst;
            temp.add(t);
        }

        return temp;
    }

    static void t(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    static void p(Object o){
        String kl = o +"   #t:" + tid();
        System.out.println(kl);
        A.debugmsg += kl +"<br>";
    }



}

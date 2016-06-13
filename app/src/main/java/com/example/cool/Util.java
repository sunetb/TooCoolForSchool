package com.example.cool;

import android.content.*;
import java.io.*;
import java.util.*;
import org.joda.time.*;
import org.xmlpull.v1.*;

/**
 * Created by sune on 6/3/16.
 */
public class Util {



    static ArrayList[] parseXML (String xml, String kaldtFra) {
        p("--parseXML kaldt fra "+kaldtFra);

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
                                p("Værdi: "+txt);
                                String type= txt.substring(0,1);
                                tempTekst.kategori = type;
                                p("Type: "+type);
                                if (txt.length() > 1) {
                                    String dato = txt.substring(1);
                                    int længde = dato.length();

                                    String tmpÅr = dato.substring(længde - 4, længde);
                                    int år = tryParseInt(tmpÅr);
                                    p("År: "+år);
                                    String tmpMåned = dato.substring(længde-6, længde-4);
                                    int måned = tryParseInt(tmpMåned);
                                    p("Måned: "+måned);
                                    String tmpDag = dato.substring(0, længde-6);
                                    int dag = tryParseInt(tmpDag);
                                    p("Dag: "+dag);

                                    if (måned > 12) { //Fejl: dag og måned er ombyttet
                                        if (dag < 13)tempTekst.dato = new DateTime(år,dag,måned,1,0);
                                        else {
                                            p("FEJL: Ugyldig dato");
                                        }
                                    }
                                    else tempTekst.dato = new DateTime(år,måned,dag,1,0);
                                    // DateTime()
									
                                }
                            }
                        }
                        else if (celletæller == 2){

                            tempTekst.overskrift = put; //Hmmm else her?
                        }
                        else if (celletæller == 3) {

                            put  = put.replaceFirst("<title", "<!--title")
                                    .replaceFirst("</title>", "title-->")
                                    .replaceFirst("<body", "<body style=\"color: white; background-color: black;\"");

                            tempTekst.brødtekst = put.replaceAll("<cft>", "");
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

        ArrayList[] data = {oteksterTmp, sorterStigende(teksterTmp), sorterStigende(mteksterTmp), hteksterTmp};

        return data;
    }

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
                if (a.datokode() > b.datokode()) {
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


    static Integer tryParseInt (String text) {
        try {
            return new Integer(text);
        } catch (NumberFormatException e) {

            return null;
        }
    }

    public static void skrivTekstliste (ArrayList<Tekst> liste, String filename, Context c) {
        p("skrivTekstliste("+filename+")");
        File directory = new File(c.getFilesDir().getAbsolutePath() + File.separator + "serlization");

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

    static ArrayList<Tekst> læsTekstliste (String filename, Context c) {
        p("læsTekstliste("+filename+")");
        ObjectInputStream input = null;
        ArrayList<Tekst> mitArray = null;
        File directory = new File(c.getFilesDir().getAbsolutePath()+ File.separator + "serlization");

        p("læsTekstliste_Filen eksisterer? "+directory.exists());
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

    static void p(Object o){

        String kl = "Util.";
        System.out.println(kl +o);// "   #t:" + t);;
        //instans.debugmsg += kl +o +"\n";
    }



}

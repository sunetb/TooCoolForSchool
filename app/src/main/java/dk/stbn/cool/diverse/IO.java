package dk.stbn.cool.diverse;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import dk.stbn.cool.data.Util;

/**
 * Created by sune on 6/24/16.
 */
public class IO {

    //TODO Lav typetjek

    public static Object læsObj(String filename, Context c) {
        p("hentet: "+filename);
        ObjectInputStream input = null;
        Object mitObj = null;
        File directory = new File(c.getFilesDir().getAbsolutePath()+ File.separator + "filer");

        if (directory.exists()){

            try {
                input = new ObjectInputStream(new FileInputStream(directory+ File.separator + filename));
                mitObj = input.readObject();
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

        return mitObj;
    }

    public static void gemObj(Object mitObjI, String filnavnI, Context cI) {
        final Object mitObj = mitObjI;
        final String filnavn = filnavnI;
        final Context c = cI;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File directory = new File(c.getFilesDir().getAbsolutePath() + File.separator + "filer");

                if (!directory.exists()) {
                    directory.mkdirs();
                }
                ObjectOutput out = null;

                try {
                    out = new ObjectOutputStream(new FileOutputStream(directory+ File.separator + filnavn));
                    out.writeObject(mitObj);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        p("gemt: "+filnavn);
    }

    public static void føjTilGamle(int id_int, Context c){
        final Integer id = id_int;
        final Context ctx = c;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> gamle =(ArrayList<Integer>) IO.læsObj("gamle", ctx);
                if(!gamle.contains(id))gamle.add(id);
                IO.gemObj(gamle, "gamle", ctx);
            }
        });

        p("føjet til gamle: "+id);

    }

    static void p(Object o){
        String kl = "IO.";
        Util.p(kl+o);
    }

}

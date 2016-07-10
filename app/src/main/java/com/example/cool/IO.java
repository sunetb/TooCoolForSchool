package com.example.cool;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/**
 * Created by sune on 6/24/16.
 */
public class IO {

    static Object læsObj (String filename, Context c) {
        p("hentet: "+filename);
        ObjectInputStream input = null;
        Object mitObj = null;
        File directory = new File(c.getFilesDir().getAbsolutePath()+ File.separator + "filer");

        if (directory.exists()){

            try {
                input = new ObjectInputStream(new FileInputStream(directory+ File.separator + filename));
                mitObj = (Object) input.readObject();
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

    static void gemObj (Object mitObj, String filename, Context c) {
        p("gemt: "+filename);

        File directory = new File(c.getFilesDir().getAbsolutePath() + File.separator + "filer");

        if (!directory.exists()) {
            directory.mkdirs();
        }
        ObjectOutput out = null;

        try {
            out = new ObjectOutputStream(new FileOutputStream(directory+ File.separator + filename));
            out.writeObject(mitObj);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static void p(Object o){
        String kl = "IO.";
        Util.p(kl+o);
    }

}

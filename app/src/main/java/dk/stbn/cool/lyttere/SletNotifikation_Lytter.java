package dk.stbn.cool.lyttere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dk.stbn.cool.data.Util;

/**
 * Created by sune on 6/11/16.
 * Modtager broadcast hvis en notifikation dismisses (swipe eller dismiss all)
 */
public class SletNotifikation_Lytter extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            //Husk at dette intent er et andet end det som elser fyres rundt. Dette har KUN tekstId

            String id = intent.getExtras().getString("tekstId");
			//int id_int = intent.getExtras().getInt("id_int", 0);
            //if (A.debugging) Toast.makeText(context, "Notifikation "+id+" slettet", Toast.LENGTH_LONG).show();
            p("Notifikation "+id+" slettet");

            Util.notiBrugt(context, intent);

        }


    void p (Object o){
        Util.p("NotifikationSlettet."+o);
    }

}

package com.example.cool;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class Forside_akt extends AppCompatActivity implements View.OnClickListener, Observatør{

    PagerAdapter pa;
    ViewPager vp;
    A a;
    SharedPreferences prefs;
    ImageButton frem, tilbage, del, kontakt, extras;
    ViewPager.OnPageChangeListener sideLytter;
    int visPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forside);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.cool_nobkgr_50x50_rund);

        p("oncreate() kaldt");
        a = A.a;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initUI();
        a.aktivitetenVises = true; //skal den stå tidligere?
        tjekOpstartstype(savedInstanceState);
        if (a.debugging && prefs.getBoolean("vistestdialog", true)) testDialog();

    }

    @Override
    public void onClick(View v) {
        int positionNu = vp.getCurrentItem();

        if (v==tilbage && positionNu > 0) {
            vp.setCurrentItem(--positionNu);
        }
        else if (v == frem && positionNu != a.synligeTekster.size()-1){
            vp.setCurrentItem(++positionNu);
        }
        else if (v == extras)
            showDialog(1);
        //TODO Lav de andrre knapper
        else if (v == del){
            if (a.debugging) {
                startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                del.setEnabled(false);
                del.getBackground().setAlpha(100);
            }
            else {
                //todo
            }

        } else if (v == kontakt) {
            if (a.debugging) {

                a.nulstil();
                startActivity(new Intent(this, Forside_akt.class));
                finish();
            }
            else {
                //todo
            }
        }
        knapstatus (vp.getCurrentItem(), "onClick()");



    }

    private void initUI() {
        vp = (ViewPager) findViewById(R.id.pager);
        pa = new PagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pa);
        p("adapteren sat på viewpageren");
        tilbage = (ImageButton) findViewById(R.id.tilbage);
        tilbage.setOnClickListener(this);
        frem = (ImageButton) findViewById(R.id.frem);
        frem.setOnClickListener(this);
        del = (ImageButton) findViewById(R.id.anbefal);
        del.setOnClickListener(this);
        kontakt = (ImageButton) findViewById(R.id.redigerFeedback);
        kontakt.setOnClickListener(this);
        extras = (ImageButton) findViewById(R.id.extras);
        extras.setOnClickListener(this);

        sideLytter = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                knapstatus(position, "sidelytter.onPageSelected()");
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        a.lyt(this);
        vp.addOnPageChangeListener(sideLytter);
        visPosition = prefs.getInt("seneste position", vp.getCurrentItem());
        if (visPosition == -1) visPosition = a.synligeTekster.size()-1;
        vp.setCurrentItem(visPosition);
		knapstatus(visPosition, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        a.afregistrer(this);
        vp.removeOnPageChangeListener(sideLytter);
        prefs.edit().putInt("seneste position", vp.getCurrentItem()).commit();
    }

    public void knapstatus (int nu, String kaldtFra) {
        int max = a.synligeTekster.size()-1;
		p("knapstatus: nu="+nu+" max="+max + "Kaldt fra "+kaldtFra);

        //TODO husk hTekster

        if (max == 0) nu =0;

        if (nu == max || max == -1 ) {
            frem.setEnabled(false);
            frem.getBackground().setAlpha(100);
        }
        else{
            frem.setEnabled(true);
            frem.getBackground().setAlpha(255);
        }
        if (nu == 0) {
            tilbage.setEnabled(false);
            tilbage.getBackground().setAlpha(100);
        }
        else{
            tilbage.setEnabled(true);
            tilbage.getBackground().setAlpha(255);
        }
        if (a.debugging){

        }
    }


    @Override
    public void opdater() {
        pa.notifyDataSetChanged();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                vp.setCurrentItem(a.synligeTekster.size()-1);
                knapstatus(a.synligeTekster.size()-1, " opdater()");

            }
        }, 50);

        t("Teksterne blev opdateret");
    }

    void p(Object o){
        String kl = "Forside.";
        Util.p(kl+o);
    }
    void t(String s){
        Util.t(this,s);
    }

     //----Bygger en AlertDialog med listen over Ekstra-tekster
    @Override
    protected Dialog onCreateDialog(int id){

        p("Dialog: htekster længde: "+a.htekster.size());
        AlertDialog.Builder extraliste = new AlertDialog.Builder(this);

        extraliste.setTitle("Extras");

        extraliste.setAdapter(new ArrayAdapter(this,
                        android.R.layout.simple_list_item_1, a.hteksterOverskrifter),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int valgt) {
                        Tekst valgtHTekst = a.htekster.get(valgt);

                        if (!a.synligeTekster.contains(valgtHTekst)) {
                            a.synligeTekster.add(valgtHTekst);
                            a.givBesked();
                            vp.setCurrentItem(a.synligeTekster.size()-1);
                        }
                        else {
                            vp.setCurrentItem(a.findTekstnr(valgtHTekst.id_int));
                        }
                    }
                }); // ArrayAdapter slut

        return extraliste.create();


    }// end onCreateDialog

    private boolean skærmvending () {

        int vindueshøjdetmp = Resources.getSystem().getDisplayMetrics().heightPixels;
        if (a.sidstKendteVindueshøjde==0) {
            a.sidstKendteVindueshøjde = vindueshøjdetmp;
            return false;
        }
        else if (a.sidstKendteVindueshøjde == vindueshøjdetmp) return false;
        else a.sidstKendteVindueshøjde = vindueshøjdetmp;

        return true;
    }

    private void tjekOpstartstype (Bundle b){
        Bundle startBundle= b;
        String opstart = "Forside startet. Hvordan? ";
        if (startBundle == null) startBundle = getIntent().getExtras();
        if (startBundle == null) p(opstart+" Startet fra hjemmeskærmen");
        else { //startet fra notifikation eller genstartet

            boolean fraAlarm = startBundle.getBoolean("fraAlarm", false);

            if (!fraAlarm) {

                if (skærmvending()) p(opstart+"På grund af skærmvending");
                else p(opstart+"Genstartet af systemet eller brugeren");
            }
            else {
                p(opstart+" NOTIFIKATION: "+ startBundle.getString("overskrift"));
                int idstr = startBundle.getInt("id_int");
                int husk = -1;
                for (int i = 0; i < a.synligeTekster.size(); i++) {
                    if (a.synligeTekster.get(i).id_int == idstr) {
                        husk = i;
                        break;
                    }
                }
                p("tjekOpstartstype: husk="+husk);
                if (husk != -1) {
                    vp.setCurrentItem(husk);
                    knapstatus(husk,"tjekOpstartstype()");
                    prefs.edit().putInt("seneste position", vp.getCurrentItem()).commit();

                }
                else {
                    p("FEJL: Teksten fre notifikationen fandtes ikke i synligeTekster!");
                    Tekst t = (Tekst) IO.læsObj(""+idstr, this);
                    a.synligeTekster.add(t);
                    pa.notifyDataSetChanged();
                    prefs.edit().putInt("seneste position", vp.getCurrentItem()).commit();
                   // vp.setCurrentItem(husk);
                    //knapstatus(a.synligeTekster.size()-1, "tjekOpstartstype()");

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        p("onDestroy() blev kaldt");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void testDialog () {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set titleis
        alertDialogBuilder.setTitle("Test-tilstand aktiveret");

        // set dialog message
        alertDialogBuilder
                .setMessage("Knapperne 'Del' (tre prikker forbundet med streger) og 'Send' (papirfly) har en anden funktion end normalt:\n\n" +
                        "'Del' aktiverer en ny side hvor du kan indstille telefonens dato. Dette skal du gøre først. Fjern fluebenet i 'Automatisk dato og klokkeslæt'. Tryk 'Indstil dato'. Tryk på Tilbage-knappen for at komme tilbage til appen\n\n" +
                        "'Send' opdaterer data, så du kan se hvordan det ville se ud hvis appen blev genstartet. Den skal du trykke på for at se appen på en bestemt dato.\n\n" +
                        "Husk at stille datoen på din telefon tilbage når du er færdig med at teste")
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        prefs.edit().putBoolean("vistestdialog", false).commit();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}

package dk.stbn.alarm.aktivitetFragment;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;

import dk.stbn.alarm.R;
import dk.stbn.alarm.data.Tekst;
import dk.stbn.alarm.data.A;
import dk.stbn.alarm.data.Tilstand;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.IO;
import dk.stbn.alarm.diverse.K;
import dk.stbn.alarm.lyttere.Lyttersystem;
import dk.stbn.alarm.lyttere.Observatør;
import dk.stbn.alarm.lyttere.SletNotifikation_Lytter;

public class Forside_akt extends AppCompatActivity implements View.OnClickListener, Observatør {

    PagerAdapter pa;
    ViewPager vp;
    A a;
    SharedPreferences prefs;
    ImageButton frem, tilbage, del, kontakt, extras;
    ViewPager.OnPageChangeListener sideLytter;
    int visPosition = 0;
    boolean datoÆndret = false;
    ArrayAdapter hListeadapter = null;
    public BroadcastReceiver mLangReceiver = null;
    Tilstand tilstand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forside);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.cool_nobkgr_50x50_rund);

        p("%%%%%%%%%%%%%%%%%%%%%%% oncreate() kaldt  %%%%%%%%%%%%%%%%%%%%%%%");
        a = A.a;
        prefs = a.pref;
        tilstand = Tilstand.getInstance(prefs);
        p(" idag er: "+ tilstand.masterDato.getDayOfMonth() + ": " + tilstand.masterDato.getMonthOfYear() + " - "+ tilstand.masterDato.getYear());
        setupLangReceiver();
        initUI();
        tilstand.aktivitetenVises = true;
        tjekOpstartstype(savedInstanceState);

        if (tilstand.testtilstand){
            if (prefs.getBoolean("vistestdialog", true)) testDialog(TESTTILSTAND_1, "Test-tilstand aktiveret");
        }
        else if (tilstand.testtilstand_2){
            if (prefs.getBoolean("vistestdialog", true)) testDialog(TESTTILSTAND_2, "Test-tilstand aktiveret");
        }

       // Tekst test = new Tekst("Test", "TEST", "t", new DateTime().plusSeconds(30));
        //p(test);
        //Util.startAlarm(this, test);
        //TESTET feb 2019 og virker: Alarm_Lytter.bygNotifikation (this, "hej", test.id, test.id_int);


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
        else if (v == del){
            if (tilstand.testtilstand || tilstand.testtilstand_2) {
                if (tilstand.testtilstand_2) {
                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                    datoÆndret = true;
                }
                else {
                    t("Spoler 1 dag frem...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //a.rul(1);
                        }
                    }, 110);
                    finish();
                }            }
            else {
                p("klikket på Del");
//Todo: Ny aktivitet med forklaring af pointsystem mm
                Intent i = new Intent();
                i.setAction(Intent.ACTION_SEND);

               // i.putExtra(Intent.EXTRA_TEXT, "Hej tjek denne app ud: https://play.google.com/store/apps/details?id=dk.stbn.cool.alarm");

                Tekst deletekst = a.synligeTekster.get(vp.getCurrentItem());

                String s = deletekst.overskrift + "\n\n" + Html.fromHtml(deletekst.brødtekst).toString() + "\nTjek appen TOO COOL FOR SCHOOL ud på:  https://play.google.com/store/apps/details?id=dk.stbn.cool.alarm" ;
                p(s);
                i.putExtra(Intent.EXTRA_TEXT, s);

                i.setType("text/plain");
                startActivity(i);
            }

        } else if (v == kontakt) {
            if (tilstand.testtilstand) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //a.rul(6);
                    }
                }, 110);
                t("Spoler 6 dage frem...");
                finish();
            }
            else {
                p("klikket på Kontakt");
                Intent i = new Intent(this, Kontakt.class);
                startActivity(i);
                //Kontakt.intent(this).start();
            }
        }
        knapstatus (vp.getCurrentItem(), "onClick()");
    }

    private void initUI() {
        vp = (ViewPager) findViewById(R.id.pager);
        //vp.setOffscreenPageLimit(7); //Lappeløsning nu og her

        pa = new PagerAdapter(getSupportFragmentManager());//getChildFragmentManager());//
        vp.setAdapter(pa);
        tilbage = (ImageButton) findViewById(R.id.tilbage);
        tilbage.setOnClickListener(this);
        frem = (ImageButton) findViewById(R.id.frem);
        frem.setOnClickListener(this);
        del = (ImageButton) findViewById(R.id.anbefal);
        del.setOnClickListener(this);
        kontakt = (ImageButton) findViewById(R.id.redigerFeedback);
        kontakt.setOnClickListener(this);


        if (tilstand.debugging) {

        }
        extras = (ImageButton) findViewById(R.id.extras);
        extras.setOnClickListener(this);
        if (tilstand.debugging) {

        }
        if (!tilstand.hteksterKlar) {
            extras.setEnabled(false);
            extras.getBackground().setAlpha(100);
        }
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


        // -- DEBUGGING
        if (tilstand.debugging) {

            del.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    tilstand.testtilstand =  !tilstand.testtilstand;
                    if (tilstand.testtilstand) {
                        testDialog(TESTTILSTAND_1,"Test-tilstand aktiveret");

                        del.setImageResource(R.drawable.en);
                        kontakt.setImageResource(R.drawable.seks);
                    }
                    else {
                        del.setImageResource(R.drawable.ic_share_black_24dp);
                        kontakt.setImageResource(R.drawable.ic_send_black_24dp);
                        //-- Færdig med at teste, nultil listen over forældede tekster
                        IO.gemObj(new ArrayList<Integer>(), "gamle", getApplicationContext());
                        if (tilstand.modenhed == K.MODENHED_MODEN) a.udvælgTekster();
                    }
                    return true;
                }
            });

            kontakt.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    tilstand.testtilstand_2 =  !tilstand.testtilstand_2;
                    if (tilstand.testtilstand_2) {
                        testDialog(TESTTILSTAND_2, "Test-tilstand aktiveret");

                        del.setImageResource(R.drawable.cool_nobkgr_50x50_rund);

                    }
                    else {
                        del.setImageResource(R.drawable.ic_share_black_24dp);
                        //-- Færdig med at teste, nultil listen over forældede tekster
                        IO.gemObj(new ArrayList<Integer>(), "gamle", getApplicationContext());
                        if (tilstand.modenhed == K.MODENHED_MODEN) a.udvælgTekster();
                    }
                    return true;
                }
            });

            extras.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    t("Nulstiller og genindlæser appens data");
                    p("Nulstiller og genindlæser appens data");

                    prefs.edit().putInt("tekstversion", 0).commit();
                    prefs.edit().putBoolean("tvingNyTekst", true).commit();
                    //a.sletData();
                    testDialog("Nu er det vigtigt at du lukker appen. BÅDE med tilbage-knappen OG ved at trykke på firkant-knappen / holde HOME nede i lang tid til joblisten dukker op, og derefter swiper appen ud", "OBS OBS OBS");
                    return true;
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (datoÆndret){
            p("onStart: Dato ændret");
            datoÆndret = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //a.rul(1);
                }
            }, 110);
            finish();

        }
        else {
            a.lytter.lyt(this);
            vp.addOnPageChangeListener(sideLytter);
            if (tilstand.debugging) pa.notifyDataSetChanged(); //lidt groft ?
            visPosition = prefs.getInt("senesteposition", vp.getCurrentItem());
            p("onstart visposition fra prefs: "+visPosition);
            p("Hvad var der i prefs? "+prefs.getInt("senesteposition", 100000));
            p("hvor lang er tekstlisten "+a.synligeTekster.size());
            if (visPosition >= a.synligeTekster.size()) visPosition = -1;
            if (visPosition == -1) visPosition = a.synligeTekster.size() - 1;
            p("onstart visposition efter tjek: "+visPosition);

            vp.setCurrentItem(visPosition);
            knapstatus(visPosition, "onStart()");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        a.lytter.afregistrer(this);
        vp.removeOnPageChangeListener(sideLytter);
        tilstand.aktivitetenVises = false;
        prefs.edit().putInt("senesteposition", vp.getCurrentItem()).commit();

    }

    public void knapstatus (int nu, String kaldtFra) {
        int max = a.synligeTekster.size()-1;
		p("knapstatus: nu="+nu+" max="+max + "Kaldt fra "+kaldtFra);

        if (max == 0) nu = 0;

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
        if (tilstand.debugging){
            if (tilstand.testtilstand){
                del.setImageResource(R.drawable.en);
                kontakt.setImageResource(R.drawable.seks);
            }
            else if (tilstand.testtilstand_2){
                del.setImageResource(R.drawable.cool_nobkgr_50x50_rund);
            }
        }
    }


    @Override
    public void opdater(int event) {
        if (event == K.SYNLIGETEKSTER_OPDATERET){
            pa.notifyDataSetChanged();
            vp.setCurrentItem(a.synligeTekster.size()-1);
            knapstatus(a.synligeTekster.size()-1, " opdater()");
             new Handler().postDelayed(() -> {

             }, 10);

        }
        else if (event == K.HTEKSTER_OPDATERET){
            if (hListeadapter != null) hListeadapter.notifyDataSetChanged();
            extras.setEnabled(true);
            extras.getBackground().setAlpha(255);
        }
        else if (event == K.NYE_HTEKSTER_PÅ_VEJ){
            extras.setEnabled(false);
            extras.getBackground().setAlpha(100);
        }
        else if (event == K.OFFLINE){
            a.synligeTekster.add(new Tekst("OFFLINE", "Error: no internet connection. Check yourt settings and try again later", "a", new DateTime()));
            Lyttersystem.getInstance().givBesked(K.SYNLIGETEKSTER_OPDATERET, "Forside, ingen netforbindelse");
        }
    }

  private boolean skærmvending () {

        int vindueshøjdetmp = Resources.getSystem().getDisplayMetrics().heightPixels;

        if (tilstand.sidstKendteVindueshøjde==0) {
            tilstand.sidstKendteVindueshøjde = vindueshøjdetmp;
            return false;
        }
        else if (tilstand.sidstKendteVindueshøjde == vindueshøjdetmp) return false;
        else tilstand.sidstKendteVindueshøjde = vindueshøjdetmp;
        tilstand.skærmVendt++;
        p("Skærm vendt "+tilstand.skærmVendt + " gange");
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
                int id = startBundle.getInt("id_int");

                p(opstart+" NOTIFIKATION: "+ startBundle.getString("overskrift") + "id_int: "+id);
                IO.føjTilGamle(id,this);

                int husk = -1;
                for (int i = 0; i < a.synligeTekster.size(); i++) {
                    p("søger synligetkster igennem: "+ a.synligeTekster.get(i).id_int);

                    if (a.synligeTekster.get(i).id_int == id) {
                        husk = i;
                        break;
                    }
                }
                p("tjekOpstartstype: husk="+husk);
                if (husk != -1) {
                    vp.setCurrentItem(husk);
                    knapstatus(husk,"tjekOpstartstype()");
                    prefs.edit().putInt("senesteposition", vp.getCurrentItem()).commit();

                }
                else {
                    p("FEJL: Teksten fra notifikationen fandtes ikke i synligeTekster!");
                    Tekst t = (Tekst) IO.læsObj(""+id, this);
                    a.synligeTekster.add(t);
                    pa.notifyDataSetChanged();
                    prefs.edit().putInt("senesteposition", a.synligeTekster.size()-1).commit();
                   // vp.setCurrentItem(husk);
                    //knapstatus(a.synligeTekster.size()-1, "tjekOpstartstype()");

                }
                //Util.baglog("Forside.tjekOpstartsType(): Startet fra noti: "+ id + " " +startBundle.getString("overskrift"), this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mLangReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        tilstand.aktivitetenVises = false;
        super.onDestroy();
        p("onDestroy() blev kaldt");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //kun til test
    private void testDialog (String besked, String overskrift) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(overskrift);
        alertDialogBuilder
                .setMessage(besked)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        prefs.edit().putBoolean("vistestdialog", false).commit();
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    String TESTTILSTAND_1 = "Nu er 'DEL' og 'KONTAKT'-knapperne omdefineret:\n\n"+

            "-Knappen '1' ruller appens interne dato 1 dag frem\n\n"+
            "-Knappen '6' ruller appens interne dato 6 dage frem\n\n" +
            "\n"+
            "Du kan deaktivere test-tilstand igen ved at holde knappen '1' nede i et par sekunder";

    String TESTTILSTAND_2 = "TEST-TILSTAND 2\n" +
            "\n" +
            "Nu er 'DEL'-knappen omdefineret:\n\n"+

            "-Den åbner telefonens indstillinger for Dato og Tid\n\n"+
            "-Hvis der er hak i Automatisk dato og klokkelsæt, fjern dette hak og vælg en dato\n\n" +
            "\n"+
            "Husk at sætte hak i Automatisk dato og klokkeslæt igen når du er færdig med at teste";

    ///kun til test
    void bygNotifikation (Context context, String overskrift, String id, int id_int) {

        p("bygnotifokation test modtog: "+overskrift+ " IDStreng: "+id + " id_int: "+id_int);

        NotificationCompat.Builder mBuilder =
                null;
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.cool_nobkgr_71x71)
                    .setContentTitle("Too Cool for School")
                    .setContentText(overskrift)
                    .setAutoCancel(true)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setOnlyAlertOnce(true);
       // }

        //ingen effekt.setDeleteIntent(PendingIntent.getActivity(context, 0, sletteIntent, 0))

        Intent resultIntent = new Intent(context, Forside_akt.class);
        resultIntent.putExtra("overskrift", overskrift);
        resultIntent.putExtra("tekstId", id);
        resultIntent.putExtra("id_int", id_int);
        resultIntent.putExtra("fraAlarm", true);
        resultIntent.setAction(id); //lille hack som gør at det bliver forskellige intents hvis det er to notifikationer samtidig
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //stackBuilder.addParentStack(Forside.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT //FLAG_ONE_SHOT//
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification n = mBuilder
                .build();

        //Hvis brugeren sletter notifikationen ved swipe eller tømmer alle notifikationer
        Intent sletteIntent = new Intent(context, SletNotifikation_Lytter.class);
        sletteIntent.putExtra("tekstId", id);
        sletteIntent.setAction(id);

        n.deleteIntent = PendingIntent.getBroadcast(context, 0, sletteIntent, 0);
        mNotificationManager.notify(id_int, n);


    }

//TODO: Flyttes til Application-singleton?
    //Fra https://stackoverflow.com/questions/34285383/android-how-to-detect-language-has-been-changes-on-phone-setting
    public BroadcastReceiver setupLangReceiver(){

        if(mLangReceiver == null) {
            p("Opretter sproglytter");
            mLangReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    p("Sprog ændret til: "+ Locale.getDefault().getLanguage());
                    a.lytter.givBesked(K.SPROG_ÆNDRET, "Sproglytter");
                }

            };

            IntentFilter filter = new IntentFilter(Intent.ACTION_LOCALE_CHANGED);
            registerReceiver(mLangReceiver, filter);
            p("Sproglytter oprettet");
        }

        return mLangReceiver;
    }
    //----Bygger en AlertDialog med listen over Ekstra-tekster
    boolean klikket = false; //Holder KUN dialogen i live hvis skærmen bliver vendt.

    @Override
    protected Dialog onCreateDialog(int id){

        p("Dialog: htekster længde: "+a.htekster.size());
        final AlertDialog.Builder extraliste = new AlertDialog.Builder(this);

        TextView t  = new TextView(this);
        t.setText("Extras");
        t.setGravity(Gravity.CENTER);
        t.setPadding(10, 10, 10, 10);
        t.setTextSize(22);
        //extraliste.setTitle("Extras");
        extraliste.setCustomTitle(t);

        ArrayAdapter aad =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1, a.hteksterOverskrifter); // ArrayAdapter slut

        extraliste.setAdapter(aad,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int valgt) {
                        Tekst valgtHTekst = a.htekster.get(valgt);

                        if (!a.synligeTekster.contains(valgtHTekst)) {
                            a.synligeTekster.add(valgtHTekst);
                            a.lytter.givBesked(K.SYNLIGETEKSTER_OPDATERET, "Forside, Dialog");
                            pa.notifyDataSetChanged();
                            vp.setCurrentItem(a.synligeTekster.size()-1);
                        }
                        else {
                            vp.setCurrentItem(a.findTekstnr(valgtHTekst.overskrift));
                        }
                        hListeadapter = null;
                        klikket = true;
                    }
                } );
        hListeadapter = aad;
        if (klikket) {
            doKeepDialog(extraliste);
            klikket= false;
        }
        return extraliste.create();


    }// end onCreateDialog



    //Bevarer dialog ved skærmvending tilpasset fra http://stackoverflow.com/questions/8537518/the-method-getwindow-is-undefined-for-the-type-alertdialog-builder
    private static void doKeepDialog(AlertDialog.Builder dialog){
        AlertDialog dlg = dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dlg.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dlg.getWindow().setAttributes(lp);
    }

    void p(Object o){
        String kl = "Forside.";
        Util.p(kl+o);
    }
    void t(String s){
        Util.t(this,s);
    }

}

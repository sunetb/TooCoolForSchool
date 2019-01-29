package dk.stbn.alarm.aktivitetFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import dk.stbn.alarm.R;
import dk.stbn.alarm.data.A;
import dk.stbn.alarm.data.Util;



public class Kontakt extends AppCompatActivity implements View.OnClickListener {

    A a = A.a;

    Button send;

    String navn, emne, besked;

    CheckBox sendlog;

    EditText et_navn, et_emne, et_besked;

    TextView version;

    //-> ress
    String manglerNavn = "Du mangler at skrive dit navn";
    String manglerBesked = "Du har ikke skrevet nogen besked i feltet";
    String ver;

    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.cool_nobkgr_50x50_rund);
        }
        p("Kontakt startet");
        setContentView(R.layout.activity_kontakt);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(this);

        sendlog = (CheckBox) findViewById(R.id.sendlog);

        et_navn = (EditText) findViewById(R.id.et_navn);
        et_emne = (EditText) findViewById(R.id.et_emne);
        et_besked = (EditText) findViewById(R.id.et_besked);
        et_besked.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    tilSingleton();
                    sendmail();
                    handled = true;
                    finish();
                }
                return handled;
            }
        });

        version = (TextView) findViewById(R.id.version);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ver =  getResources().getString(R.string.appversion)
                + " " + pInfo.versionName
                + " / " + pInfo.versionCode
                + " | " + getResources().getString(R.string.kildetekstversion)
                + " " + pref.getInt("tekstversion", 0);
        p(ver);  p(ver);
        version.setText(ver);
        //-- Skriver tekst-data til loggen
        a.testTekster ();
        //-- Skriver baggrunds-lytter-log
        Util.skrivBaglog(this);
    }

    @Override
    public void onClick(View v) {

        boolean udfyldt = false;

        emne = et_emne.getText().toString();

        if (et_navn.getText().toString().equals(""))
            Toast.makeText(this, getResources().getString(R.string.manglernavn), Toast.LENGTH_LONG).show();
        else {
            navn = et_navn.getText().toString();
            udfyldt = true;
        }

        if (udfyldt && et_besked.getText().toString().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.manglerbesked), Toast.LENGTH_LONG).show();
            udfyldt = false;
        }

        else if (udfyldt){
            besked = et_besked.getText().toString();
            udfyldt = true;
        }
        if (udfyldt) {
            tilSingleton();
            sendmail();
            finish();

        }


    }

    void sendmail() {
        p("endMail()");

        String afstedtekst = besked +"\n\n"+getResources().getString(R.string.hilsen)+"\n"+ navn;
        if (sendlog.isChecked()) afstedtekst+= ver+"\n\n\n\n"+ A.debugmsg + A.hale;

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sunetb@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, emne);
        i.putExtra(Intent.EXTRA_TEXT   , afstedtekst);
        try {
            startActivity(Intent.createChooser(i, getResources().getString(R.string.sendmail)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getResources().getString(R.string.manglermailklient), Toast.LENGTH_SHORT).show();
        }

    }

    void tilSingleton (){
        navn = et_navn.getText().toString();
        emne = et_emne.getText().toString();
        besked = et_besked.getText().toString();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        tilSingleton();

    }
    void p(Object o){

        String kl = "Kontakt.";
        Util.p(kl+o);
    }

}
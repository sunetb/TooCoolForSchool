package com.example.cool;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import android.content.*;


public class Kontakt extends AppCompatActivity implements View.OnClickListener {

    A instans = A.a;

    Button send;

    String navn, emne, besked;

    CheckBox sendlog;

    EditText et_navn, et_emne, et_besked;
    boolean logMig = true;


    //-> ress
    String manglerNavn = "Du mangler at skrive dit navn";
    String manglerMail = "Du mangler at skrive din email-adresse";
    String manglerBesked = "Du har ikke skrevet nogen besked i feltet";
    String beskedSendt = "Din besked er afsendt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.cool_nobkgr_50x50_rund);
        }
        p("Kontakt startet");
        setContentView(R.layout.activity_kontakt);

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


    }


    @Override
    public void onClick(View v) {

        boolean udfyldt = false;

        emne = et_emne.getText().toString();

        if (et_navn.getText().toString().equals(""))
            Toast.makeText(this, manglerNavn, Toast.LENGTH_LONG).show();
        else {
            navn = et_navn.getText().toString();
            udfyldt = true;
        }

        if (udfyldt && et_besked.getText().toString().equals("")) {
            Toast.makeText(this, manglerBesked, Toast.LENGTH_LONG).show();
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

        String afstedtekst = besked +"\n\n"+"Med venlig hilsen\n"+ navn;
        if (sendlog.isChecked()) afstedtekst+= "\n\n\n\n"+ instans.debugmsg;

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sunetb@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, emne);
        i.putExtra(Intent.EXTRA_TEXT   , afstedtekst);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Der er ikke installeret nogen e-mail klient.", Toast.LENGTH_SHORT).show();
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
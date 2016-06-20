package com.example.cool;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class Forside_akt extends AppCompatActivity implements View.OnClickListener {

    PagerAdapter pa;
    ViewPager vp;
    A a;
    SharedPreferences prefs;
    ImageButton frem, tilbage, del, kontakt, extras;
    ViewPager.OnPageChangeListener sideLytter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forside);
        getSupportActionBar().setIcon(R.drawable.cool_nobkgr_50x50_rund);

        p("oncreate() kaldt");
        a = A.a;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initUI();
    }

    @Override
    public void onClick(View v) {
        int positionNu = vp.getCurrentItem();
        int maxPosition = a.synligeTekster.size()-1;

        if (v==tilbage && positionNu > 0) {
            vp.setCurrentItem(--positionNu);
        }
        else if (v == frem && positionNu != maxPosition){
            vp.setCurrentItem(++positionNu);
        }
        //TODO Lav de andrre knapper
        else t("Ikke implementeret");
        knapstatus (vp.getCurrentItem(), maxPosition);

    }

    private void initUI() {
        vp = (ViewPager) findViewById(R.id.pager);
        pa = new PagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pa);

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
                knapstatus(position, a.synligeTekster.size()-1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        };
    }



    @Override
    protected void onStart() {
        super.onStart();

        vp.addOnPageChangeListener(sideLytter);
        int visPosition = prefs.getInt("seneste position", vp.getCurrentItem());
        vp.setCurrentItem(visPosition);
		knapstatus(visPosition, a.synligeTekster.size()-1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        vp.removeOnPageChangeListener(sideLytter);
        prefs.edit().putInt("seneste position", vp.getCurrentItem()).apply();
        a.gemSynligeTekster();

    }

    public void knapstatus (int nu, int max) {
		p("knapstatus: nu="+nu+" max="+max);
        //husk tilfælde hvor nu og max begge er nul

        //TODO husk hTekster

        if (nu == max) {
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


    }

    void p(Object o){
        String kl = "Forside.";
        kl += o +"   #t:" + Util.tid();
        System.out.println(kl);
        A.debugmsg += kl +"<br>";
    }
    void t(String s){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }



}

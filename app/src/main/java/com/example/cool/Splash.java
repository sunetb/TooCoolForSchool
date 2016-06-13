package com.example.cool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends AppCompatActivity implements Runnable {

    static Splash splashAktivitet = null;
    Context ctx = this;
    Handler handler = new Handler();
    ImageView iv;
    SharedPreferences sp;
    int animLængde1;
    int animLængde2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        animLængde1 = sp.getInt("animLængde1", 3400);
        animLængde2 = sp.getInt("animLængde2", 2100);
        if (animLængde1 == 3400) gemKortereVærdier();

        iv = (ImageView) findViewById(R.id.billede);
        Animation a = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        a.setDuration(animLængde1);
        iv.startAnimation(a);
        handler.postDelayed(new Runnable () {


            @Override
            public void run() {
                Animation b = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
                b.setDuration(animLængde2);
                iv.startAnimation(b);

            }
        }, (animLængde1+animLængde2)+1000);



        if (savedInstanceState == null) {
            handler.postDelayed(this, (animLængde1+animLængde2)+1000);
        }
        splashAktivitet = this;
    }

    private void gemKortereVærdier() {
        sp.edit()
                .putInt("animLængde1", 2100)
                .putInt("animLængde1", 1500)
                .apply();
    }

    @Override
    public void run() {
        startActivity(new Intent(this, Forside_akt.class));
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        splashAktivitet.finish();
        splashAktivitet = null;
    }

    @Override
    public void finish() {
        super.finish();
        handler.removeCallbacks(this);
    }

}

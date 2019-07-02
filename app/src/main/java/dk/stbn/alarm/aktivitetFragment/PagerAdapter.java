package dk.stbn.alarm.aktivitetFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import dk.stbn.alarm.data.Tekstlogik;
import dk.stbn.alarm.data.Util;


/**
 * Created by sune on 5/31/16.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    Tekstlogik tl;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        tl = Tekstlogik.getInstance(null);
    }

    @Override
    public Fragment getItem(int position) {
        p("adapter getitem kaldt: "+position);
        Fragment f = new TekstFragment_frag();
        Bundle arg = new Bundle();
        arg.putInt("pos", position);
        f.setArguments(arg);
        return f;
    }


    @Override
    public int getCount() {
        return tl.synligeTekster.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return "";//a.overskrifter.get(position);
    }

    void p (Object o){
        Util.p("PagerAdapter."+o);
    }

    /*
    *
    * Skal ind i xml hvis denne skal være aktiv:
    *         <android.support.v4.view.PagerTabStrip
            android:id="@+id/pager_header"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="top"
            android:paddingBottom="4dp"
            android:paddingTop="4dp" />
    * */



}

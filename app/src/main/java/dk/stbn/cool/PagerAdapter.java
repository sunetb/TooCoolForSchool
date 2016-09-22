package dk.stbn.cool;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * Created by sune on 5/31/16.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    A a;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        a= A.a;
        a.nyPageradapter++;
        p("Pageradapter constructor kaldt "+a.nyPageradapter + "gange");
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
        return a.synligeTekster.size();
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

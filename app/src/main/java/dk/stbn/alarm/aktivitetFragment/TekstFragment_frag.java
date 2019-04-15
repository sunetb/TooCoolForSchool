package dk.stbn.alarm.aktivitetFragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import dk.stbn.alarm.R;
import dk.stbn.alarm.data.Tekst;
import dk.stbn.alarm.data.A;
import dk.stbn.alarm.data.Util;
import dk.stbn.alarm.diverse.K;


public class TekstFragment_frag extends Fragment implements View.OnClickListener {

	WebView w;
	View divi;

    int position = -1;
    A a;
	private Tekst tekst;

    public TekstFragment_frag() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = A.a;
		position = getArguments().getInt("pos", 0);
		if (position >= a.synligeTekster.size() || position < 0) position = a.synligeTekster.size()-1;
		tekst= a.synligeTekster.get(position);
		
        p("Fragment oncreate. Pos: "+position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        p("Fragment oncreateView Pos: "+position + "    Var bundle null? "+ (savedInstanceState == null));

        View v = inflater.inflate(R.layout.fragment_tekst_fragment_frag, container, false);
        TextView t = (TextView) v.findViewById(R.id.overskrift);
		t.setOnClickListener(this); //til fejlfinding
		w = (WebView) v.findViewById(R.id.brodtekstweb);
		w.setBackgroundColor(Color.BLACK); //forhindrer hvidt blink ved skærmvending

		p("synligeTekster.size="+a.synligeTekster.size());
		if (a.synligeTekster.size() == 0 || tekst == null){
			a.opdater(K.NYE_TEKSTER_ONLINE);
			p("onCreateView() FEJL ingen data");
			t.setText(Html.fromHtml("Vent et øjeblik"));
			w.loadData(A.hoved +"Netforbindelsen er måske langsom. Hvis der ikke sker noget om lidt, så prøv at genstarte appen.."+ A.hale, "text/html; charset=utf-8", "UTF-8");
		}
        else {
			t.setText(Html.fromHtml(tekst.overskrift));
			if (savedInstanceState == null) {

				w.loadData(tekst.brødtekst, "text/html; charset=utf-8", "UTF-8");

				p("Webviev loadet. Brødtekst var: "+ tekst.brødtekst.substring(169,190));
				p("Overskrift: "+tekst.overskrift);
				//p("øvrige data: længde: "+w. getOriginalUrl().length());
			}
			else {
				p("Webviev blev GENBRUGT. Brødtekst var: "+ tekst.brødtekst.substring(169,190));
				p("Overskrift: "+tekst.overskrift);
				//p("øvrige data: længde: "+w.getOriginalUrl().length());
			}
		}
		p("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
		p(a.synligeTekster.size());

		//-- Hvis teksten er M eller H: gul overskrift
		if (null == tekst || null == tekst.kategori) {

			t.setText(Html.fromHtml("Vent lidt"));
			w.loadData(A.hoved +"Der er sket en fejl. Vi arbejder på sagen. Netforbindelsen er måske langsom. Hvis der ikke sker noget om lidt, så prøv at genstarte appen.."+ A.hale, "text/html; charset=utf-8", "UTF-8");

			return v;
		}
		if (tekst.kategori.equals("h") || tekst.kategori.equals("m")) t.setTextColor(Color.YELLOW);
		else t.setTextColor(Color.WHITE);

		//-- Hvis teksten er H: Gul divider
		divi = v.findViewById(R.id.adskiller);
		if (tekst.kategori.equals("h")) divi.setBackgroundColor(Color.YELLOW);
		else divi.setBackgroundColor(Color.WHITE);

        return  v;
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		//http://stackoverflow.com/questions/23153084/view-of-a-fragment-becoming-null-at-the-time-of-onsaveinstancestate-callback
		p("onSaveInstanceState(): er webview null? "+ ( w==null ));
		if (w!=null) w.saveState(outState);
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		w.restoreState(savedInstanceState);
	}




	int kliktæller = 0;
	@Override
	public void onClick(View v) {
		kliktæller++;
		if (kliktæller  == 7)
		{
			w.loadData(A.debugmsg + A.hale, "text/html; charset=utf-8", "UTF-8");
			kliktæller =0;
			//todo: skift henteurl og aktiver testtilstand

		}
	}

	void p(Object o){
		Util.p("TekstFragment."+o);
	}
}
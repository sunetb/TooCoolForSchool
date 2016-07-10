package com.example.cool;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.graphics.*;



public class TekstFragment_frag extends Fragment implements View.OnClickListener {

	WebView w;

    int position = -1;
    A a;
	private Tekst tekst;

    public TekstFragment_frag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = A.a;
		position = getArguments().getInt("pos", 0);
		tekst= a.synligeTekster.get(position);
		
        p("Fragment oncreate "+position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        p("Fragment oncreateView");

        View v = inflater.inflate(R.layout.fragment_tekst_fragment_frag, container, false);
        TextView t = (TextView) v.findViewById(R.id.overskrift);
		t.setOnClickListener(this); //til fejlfinding
		w = (WebView) v.findViewById(R.id.brodtekstweb);
		w.setBackgroundColor(Color.BLACK); //forhindrer hvidt blink ved skærmvending


		if (a.synligeTekster.size() == 0 || tekst == null){
			p("onCreatView() ingen data");
			t.setText(Html.fromHtml("Vent et øjeblik"));
			w.loadData("Netforbindelsen er vist langsom. Hvis der ikke sker noget meget snart, så prøv at genstarte appen..", "text/html; charset=utf-8", "UTF-8"); //"UTF-8");
		}
        else {
			t.setText(Html.fromHtml(tekst.overskrift));
			if (savedInstanceState == null) {

				w.loadData(tekst.brødtekst, "text/html; charset=utf-8", "UTF-8"); //"UTF-8");

			}
		}
		if (tekst.kategori.equals("m")) t.setTextColor(Color.YELLOW);
		else t.setTextColor(Color.WHITE);




		
        return  v;
    }

	@Override
	public void onPause()
	{

		super.onPause();
		
	}

	
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{

		w.saveState(outState);
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{

		super.onActivityCreated(savedInstanceState);
		w.restoreState(savedInstanceState);
		
	}

	void p(Object o){
		String kl = "TekstFragment.";
		Util.p(kl+o);
	}


	int kliktæller = 0;
	@Override
	public void onClick(View v) {
		kliktæller++;
		if (kliktæller  == 7 )
		{
			w.loadData(a.debugmsg+a.hale, "text/html; charset=utf-8", "UTF-8");
			kliktæller =0;
		}
	}
}

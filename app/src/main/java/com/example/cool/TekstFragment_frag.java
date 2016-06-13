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


/**
 * A simple {@link Fragment} subclass.
 */
public class TekstFragment_frag extends Fragment {

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
		
        System.out.println("Fragment oncreate "+position);
		//WebView temp = new WebView(getActivity());
		//temp.loadData(a.synligeTekster.get(position).brødtekst, "text/html; charset=utf-8", "UTF-8"); //"UTF-8");
		
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("Fragment oncreateView");

        //position = getArguments().getInt("pos", 0);


        View v = inflater.inflate(R.layout.fragment_tekst_fragment_frag, container, false);
        TextView t = (TextView) v.findViewById(R.id.overskrift);
        t.setText(Html.fromHtml(tekst.overskrift));//a.synligeTekster.get(position).overskrift));//a.tekster.get(position)));

		//w = (WebView) v.findViewById(R.id.brodtekstweb);
		w = (WebView) v.findViewById(R.id.brodtekstweb);
		w.setBackgroundColor(Color.BLACK);

       if (savedInstanceState == null) {
		   
			w.loadData(tekst.brødtekst, "text/html; charset=utf-8", "UTF-8"); //"UTF-8");

		}
		//else w = a.wa;
		
        return  v;
    }

	@Override
	public void onPause()
	{
		// TODO: Implement this method
		//a.gemSynligTekst();
		super.onPause();
		
	}

	
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// TODO: Implement this method
		w.saveState(outState);
		super.onSaveInstanceState(outState);
		//w.saveState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onActivityCreated(savedInstanceState);
		w.restoreState(savedInstanceState);
		
	}
	
	
	//web.restoreState(savedInstanceState);
}

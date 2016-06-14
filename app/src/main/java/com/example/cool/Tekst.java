package com.example.cool;

import android.text.Html;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

public class Tekst implements Serializable {

	static final long serialVersionUID = 1234567890;

	int tekstid;
	String kategori;
	String idTekst;
	String overskrift;
	String brødtekst;

	DateTime dato;

	public Tekst () {
		tekstid = 0;
	}

	public Tekst(String over, String brød, String katg, DateTime d) {
		if (katg.equalsIgnoreCase("h")) dato = d.minusDays(7);
		else dato = d;
		kategori = katg;
		overskrift =over;
		brødtekst=brød;
		lavId();
	}



	int datokode() {
		if (kategori.equals("h")) return 0;
		int i = (dato.getYear()*10000)+(( dato.getMonthOfYear()*100)+dato.getDayOfMonth());
		return  i;

	}

	public void lavId (){
		if (kategori.equals("h")) {
			idTekst = "h";
			tekstid = 400000;
			return;
		}
		tekstid = lavIntId();
		idTekst = lavIdStreng();
	}



	String lavIdStreng () {
		String s = kategori;
		s+= dato.getDayOfMonth();
		int måned = dato.getMonthOfYear();

		if (måned < 10) s+= "0";
		s+=måned;
		s+= dato.getYear();

		//System.out.println("tjek lavIdStreng(): "+s);

		return s;
	}

	public int lavIntId() {
		/*
		2016-11-01
		*  20160000
		* +    1100
		* +       1
		* =20161101
		*
		*	>
		*
		* 2016-01-11
		*  20160000
		* +     100
		* +      11
		* =20160111
		*
		* */
		int dag = dato.getDayOfMonth();
		int mrd = dato.getMonthOfYear();
		int i = (dato.getYear()*10000)+(mrd*100)+dag;

		int typekode = 0;
		if 		(kategori.equalsIgnoreCase("o")) typekode= 1;
		else if (kategori.equalsIgnoreCase("i")) typekode= 2;
		else if (kategori.equalsIgnoreCase("m")) typekode= 3;
		else if (kategori.equalsIgnoreCase("h")) typekode= 4;
		else typekode= 5;



		return (typekode * 100000000) + i;

	}
	//Sikring
	public int getId () {
		if (tekstid == 0) {
			lavId();
		}
		return tekstid;
	}

	public String toString()
	{
		String s = ": Dato: "+datokode() + " id: "+tekstid +
		" | kat: "+kategori +
		" | overskr:" + overskrift+
		" | brødtekst:" + brødtekst.substring(0,15)+"...";

		return s;
	}

	public String toString(int simpel)
	{
		String s = ": Dato: "+datokode() +
			" | kat: "+kategori +
			" | overskr:" + overskrift;
		return s;
	}

}

package dk.stbn.alarm.data;

import org.joda.time.DateTime;

import java.io.Serializable;

public class Tekst implements Serializable {

	static final long serialVersionUID = 1234567890;


	public String kategori;
	public String id;
	public int id_int;
	public String overskrift;
	public String brødtekst;

	public DateTime dato;

	public Tekst () {
		id_int = 0;
	}

	public Tekst(String over, String brød, String katg, DateTime d) {
		if (katg.equalsIgnoreCase("h")) dato = d.minusDays(7); //H-teksters dato sættes 7 dage tilbage. TODO: Er det ikke M-Tekster der skal det?
		else dato = d;
		kategori = katg;
		overskrift =over;
		brødtekst=brød;
		lavId();
	}

	public void formater(){
		brødtekst = brødtekst.replaceAll("\n", " ");
	}

	int datokode() {
		if (kategori.equals("h")) return 0;
		else if (kategori.equals("o")) return 1;
		int i = (dato.getYear()*10000)+(( dato.getMonthOfYear()*100)+dato.getDayOfMonth());
		return  i;

	}

	public void lavId (){
		if (kategori.equals("h")) {
			id = "h";
			id_int = 400000;
			return;
		}
		else if (kategori.equals("o")) {
			id = "o";
			id_int = 500000;
			return;
		}
		id_int = lavIntId();
		id = lavIdStreng();
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
		if (id_int == 0) {
			lavId();
		}
		return id_int;
	}

	public String toString()
	{
		String s = ": Dato: "+datokode() + " id: "+ id_int +
		" | kat: "+kategori +
		" | overskr:" + overskrift+
		" | brødtekst:";
		if (brødtekst.length() > 15) s += brødtekst.substring(0,15)+"..."; //for at undgå StringIndexOutOfBounds
		else s +=brødtekst;

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

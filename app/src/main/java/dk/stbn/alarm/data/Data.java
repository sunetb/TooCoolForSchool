package dk.stbn.alarm.data;

import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

public class Data extends ViewModel {

    public ArrayList<Tekst> synligeTekster = new ArrayList();  //bruges af pageradapteren
    public ArrayList<Tekst> htekster = new ArrayList();
    public ArrayList<String> hteksterOverskrifter = new ArrayList();
    Tekstlogik tl;

    public Data (){
        super();

    }


}

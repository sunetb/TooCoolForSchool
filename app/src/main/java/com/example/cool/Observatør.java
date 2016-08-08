package com.example.cool;

/**
 * Created by sune on 7/8/16.
 * Del af eget lyttersystem, som giver mulighed for at give hovedaktiviteten besked udefra
 */
public interface Observatør {

     void opdater(int event);

}

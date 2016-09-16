# TooCoolForSchool
Denne app kan grundlæggende én ting: Vise en liste med tekster. 

Men der er en del regler for hvilke tekster der skal vises hvornår.

Der er fire typer af tekster:

O-tekster = 'obligatoriske' tekster

I-tekster = 'info' tekster.

M-tekster = 'mærkedagstekster'

H-tekster = 'hjælp'tekster

O-tekster (der er to stk i alt) er introducerende og vises kun mens appen er nyinstalleret. De handler om at formidle til brugeren hvad han/hun kan bruge denne app til og hvordan.

Appen installeres. På installationsdagen vises O-tekst nr 1 og ikke andet.
Dagen efter vises O-tekst 1 og Otekst nr 2 og ikke andet.

På dag 3 efter at appen er installeret, låses op for I-tekster.

I-tekster er de vigtigste tekster i appen. De handler om tips og ideer, indsigter og ting man kan prøve hvis man er skoletræt.
Mekanikken i I-tekster er: Der vises maksimalt 3 I-tekster. Hvis der allerede vies tre tekster og der kommer en ny I-tekst som skal vises, slettes den ældste I-tekst fra listen (First In First Out). Den "skubbes ud"

Hver I-tekst har tilknyttet en dato. Når vi når til den dato i kalenderen skal teksten vises i listen og der skal genereres en notifikation om at der er en ny tekst.

M-tekster er tekster til forskellige mærkedage, fx kvindernes internationale kampdag 8. marts. De vises fra en uge før selve dagen, til og med mærekedagens dato. Derefter vises de ikke igen.
Der vises en notifikation for M-teksten første dag teksten vises og sidste dag den vises.

H-tekster er egentlig ikke hjælp-tekster men nærmere underholdning med stille beskæftigelser.
De kan når som helst vælges fra en liste og den/de valgte H-tekster tilføjes midlertidigt til listen. Helt nøjagtigt slettes de sammen med Application-objektet, når appen ikke længere er i RAM.

Der ud over er der mulighed for at dele appen, ved at sende et link til en ven. Link til appen på Google Play.
 
Endelig kan man skrive en mail inde fra appen. Den sendes til appens ejer.

Teksterne som vises i appen, hentes første gang appen installeres fra en xml-fil som appens ejer kan lægge nye versioner op af. I en TXT-fil samme sted ligger et versionsnummer for teksterne. Denne fil hentes og læses hver gang appen startes, for at se om der skal hentes en ny version af filen med tekster.

Til test er indlagt en hemmelig testtilstand som aktiveres ved langt tryk på Del-knappen. Her bliver det muligt at sætte appens interne dato, så man spole hurtigt frem (1 eller 6 dage ad gangen)



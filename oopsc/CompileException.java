package oopsc;

import oopsc.parser.Position;

/**
 * Die Klasse repräsentiert die Ausnahme, die bei Übersetzungsfehlern erzeugt wird.
 * Sie wird in der Hauptmethode {@link OOPSC#main(String[]) OOPSC.main} gefangen und
 * ausgegeben.
 */
public class CompileException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * Konstruktor.
     * @param message Die Fehlermeldung. Ihr wird der Text "Fehler in Zeile x,
     *         Spalte y: " vorangestellt, bzw. lediglich "Fehler: ", wenn die
     *         Quelltextstelle unbekannt ist.
     * @param position Die Quelltextstelle an der der Fehler aufgetreten ist.
     *         Dieser Parameter kann auch null sein, wenn die Stelle nicht
     *         zugeordnet werden kann.
     */
    public CompileException(String message, Position position) {
        super("Fehler" + (position == null ? "" : " in Zeile " + position.getLine() + 
                ", Spalte " + position.getColumn()) + ": " + message);
    }
}

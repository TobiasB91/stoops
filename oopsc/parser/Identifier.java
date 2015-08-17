package oopsc.parser;

/**
 * Die Klasse repräsentiert einen Bezeichner im Quelltext.
 */
public class Identifier {
    /** Der Name des Bezeichners. */
    private final String name;
    
    /** Die Quelltextstelle, an der der Bezeichner gelesen wurde. */
    private final Position position;
    
    /**
     * Konstruktor.
     * @param name Der Name des Bezeichners.
     * @param position Die Quelltextstelle, an der der Bezeichner gelesen wurde.
     */
    public Identifier(String name, Position position) {
        this.name = name;
        this.position = position;
    }
    
    /**
     * Liefert den Namen des Bezeichners. 
     */
    public String getName() {
        return name;
    }
    
    /**
     * Liefert die Quelltextstelle, an der der Bezeichner gelesen wurde.
     */
    public Position getPosition() {
        return position;
    }
}

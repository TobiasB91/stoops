package oopsc.parser;

/**
 * Die Klasse repräsentiert ein Symbol, das von der lexikalischen
 * Analyses erkannt wurde.
 */
public class Symbol {
    /** Alle definierten Symbole. */
    public enum Id {
        IDENT, NUMBER, TRUE, FALSE,
        BEGIN, END,
        CLASS, IS, METHOD, RETURN, EXTENDS,
        READ, WRITE,
        IF, THEN, ELSE, ELSEIF,
        WHILE, DO,
        COLON, SEMICOLON, COMMA, PERIOD,
        LPAREN, RPAREN,
        EQ, NEQ, GT, GTEQ, LT, LTEQ, AND, OR, NOT,
        PLUS, MINUS, TIMES, DIV, MOD, 
        BECOMES, NEW,
        SELF,
        NULL,
        EOF
    };
    
    /** Der Typ des Symbols. */
    private final Id id;
    
    /** Wenn das Symbol NUMBER ist, steht die gelesene Zahl in diesem Attribut. */
    private final int number;

    /** Wenn das Symbol IDENT ist, steht der gelesene Bezeichner in diesem Attribut. */
    private final String ident;
    
    /** Die Position, an der das Symbol im Quelltext gefunden wurde. */
    private final Position position;
    
    /**
     * Konstruktor.
     * @param id Das erkannte Symbol.
     * @param number Die gelesene Zahl.
     * @param ident Der gelesene Bezeichner.
     * @param position Die Quelltextstelle, an der das Symbol erkannt wurde.
     */
    private Symbol(Id id, int number, String ident, Position position) {
        this.id = id;
        this.number = number;
        this.ident = ident;
        this.position = new Position(position.getLine(), position.getColumn());
    }
    
    /**
     * Konstruktor.
     * @param id Das erkannte Symbol.
     * @param position Die Quelltextstelle, an der das Symbol erkannt wurde.
     */
    public Symbol(Id id, Position position) {
        this(id, 0, null, position);
    }
    
    /**
     * Konstruktor.
     * @param number Die gelesene Zahl.
     * @param position Die Quelltextstelle, an der die Zahl gelesen wurde.
     */
    public Symbol(int number, Position position) {
        this(Id.NUMBER, number, null, position);
    }
    
    /**
     * Konstruktor.
     * @param ident Der gelesene Bezeichner.
     * @param position Die Quelltextstelle, an der der Bezeichner gelesen wurde.
     */
    public Symbol(String ident, Position position) {
        this(Id.IDENT, 0, ident, position);
    }
    
    /**
     * Liefert den Typ des Symbols.
     * @return Der Symboltyp.
     */
    public Id getId() {
        return id;
    }
    
    /**
     * Liefert die Zahl für Symbole vom Typ NUMBER.
     * @return Die gelesene Zahl.
     */
    public int getNumber() {
        assert id == Id.NUMBER;
        return number;
    }
    
    /**
     * Liefert den Bezeichner für Symbole vom Typ IDENT.
     * @return Der gelesene Bezeichner.
     */
    public String getIdent() {
        assert id == Id.IDENT;
        return ident;
    }
    
    /**
     * Liefert die Position, an der das Symbol im Quelltext gefunden wurde.
     * @return Die Position des ersten Zeichens des Symbols.
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Die Methode erzeugt aus diesem Objekt eine darstellbare Zeichenkette.
     * @return Die Zeichenkette.
     */
    public String toString() {
        switch (id) {
        case IDENT:
            return "IDENT: " + ident;
        case NUMBER:
            return "NUMBER: " + number;
        default:
            return id.toString();
        }
    }
}

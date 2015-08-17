package oopsc.declarations;

import oopsc.CompileException;
import oopsc.parser.Identifier;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Deklaration eines Attributs oder einer lokalen Variablen.
 */
public class VarDeclaration extends Declaration {
    /** Der Typ der Variablen bzw. des Attributs. */
    private final ResolvableIdentifier type;
    
    /** Wird hier ein Attribut deklariert (statt einer lokalen Variablen)? */
    private boolean attribute;
    
    /**
     * Die Position der Variablen im Stapelrahmen bzw. des Attributs im Objekt.
     * Dies wird während der Kontextanalyse eingetragen.
     */ 
    private int offset;
    
    /**
     * Konstruktor.
     * @param name Der Name der deklarierten Variablen bzw. des Attributs.
     * @param type Der Typ der Variablen bzw. des Attributs.
     * @param attribute Wird hier ein Attribut deklariert (statt einer lokalen
     *         Variablen)?
     */
    public VarDeclaration(Identifier name, ResolvableIdentifier type, boolean attribute) {
        super(name);
        this.type = type;
        this.attribute = attribute;
    }
    
    /**
     * Der Typ der Variablen bzw. des Attributs.
     * @return Der Typ.
     */
    public ResolvableIdentifier getType() {
        return type;
    }
    
    /**
     * Wird hier ein Attribut deklariert (statt einer lokalen Variablen)?
     * @return true: Attribut, false: lokale Variable.
     */
    public boolean isAttribute() {
        return attribute;
    }
    
    /**
     * Setzen der Position der Variablen im Stapelrahmen bzw. des Attributs im Objekt.
     * Dies wird während der Kontextanalyse eingetragen.
     * @param offset Die Position im Stapelrahmen bzw. im Objekt.
     */
    void setOffset(int offset) {
        this.offset = offset;
    }
    
    /**
     * Liefert die Position der Variablen im Stapelrahmen bzw. des Attributs im Objekt.
     * @return Die Position im Stapelrahmen bzw. im Objekt.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Führt die Kontextanalyse für diese Variablen-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
        declarations.resolveType(type);
    }

    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println(getIdentifier().getName() + 
                (type.getDeclaration() == null ? "" : " (" + offset + ")") +
                " : " + type.getName());
    }
}

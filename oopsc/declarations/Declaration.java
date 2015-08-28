package oopsc.declarations;

import oopsc.CompileException;
import oopsc.parser.Identifier;
import oopsc.streams.TreeStream;

/**
 * Die Basisklasse zur Repräsentation deklarierter Objekte.
 */
public abstract class Declaration {
    /** Der Name der deklarierten Klasse, Methode oder Variablen. */
    private Identifier identifier;
    
    /**
     * Konstruktor.
     * @param identifier Der Name der deklarierten Klasse, Methode oder Variablen.
     */
    public Declaration(Identifier identifier) {
        this.identifier = identifier;
    }
    
    /**
     * Liefert den Namen der deklarierten Klasse, Methode oder Variablen.
     * @return Der Name.
     */
    public Identifier getIdentifier() {
        return identifier;
    }
    
    
    /** Wertet konstante Ausdrücke aus und wendet ein paar Transformationen an. */
    public void optimize() {
    	
    }
    

    /**
     * Führt die Kontextanalyse für diese Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    abstract void contextAnalysis(Declarations declarations) throws CompileException;

    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    abstract void print(TreeStream tree);
}

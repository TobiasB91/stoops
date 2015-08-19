package oopsc.declarations;

import java.util.LinkedList;

import oopsc.CompileException;
import oopsc.parser.Identifier;
import oopsc.parser.Position;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert eine Klassendeklaration im Syntaxbaum.
 * Zudem stellt sie Methoden zum Typvergleich zur Verfügung.
 */
public class ClassDeclaration extends Declaration {
    /** 
     * Konstante für die Größe der Verwaltungsinformation am Anfang eines jeden Objekts.
     * Bisher ist die Größe 0.
     */
    public static final int HEADER_SIZE = 0;
    
    /** Ein interner Typ für das Ergebnis von Methoden. */
    public static final ClassDeclaration VOID_TYPE = new ClassDeclaration(new Identifier("_Void", null));

    /** Ein interner Typ für null. Dieser Typ ist kompatibel zu allen Klassen. */
    public static final ClassDeclaration NULL_TYPE = new ClassDeclaration(new Identifier("_Null", null));

    /** Der interne Basisdatentyp für Zahlen. */
    public static final ClassDeclaration INT_TYPE = new ClassDeclaration(new Identifier("_Integer", null));

    /** Der interne Basisdatentyp für Wahrheitswerte. */
    public static final ClassDeclaration BOOL_TYPE = new ClassDeclaration(new Identifier("_Boolean", null));

    /** Die Klasse Integer. */
    public static final ClassDeclaration INT_CLASS = new ClassDeclaration(new Identifier("Integer", null));
    
    /** Die Klasse Boolean. */
    public static final ClassDeclaration BOOL_CLASS = new ClassDeclaration(new Identifier("Boolean", null));

    static {
        // Integer und Boolean enthalten ein Element
    	INT_CLASS.attributes.add(new VarDeclaration(new Identifier("_value", null), new ResolvableIdentifier("Integer", null), true));
    	BOOL_CLASS.attributes.add(new VarDeclaration(new Identifier("_value", null), new ResolvableIdentifier("Boolean", null), true));
    }

    /** Die Attribute dieser Klasse. */
    private final LinkedList<VarDeclaration> attributes;
    
    /** Die Methoden dieser Klasse. */
    private final LinkedList<MethodDeclaration> methods;

    /** Die innerhalb dieser Klasse sichtbaren Deklarationen. */
    private Declarations declarations;
    
    /** 
     * Die Größe eines Objekts dieser Klasse. Die Größe wird innerhalb von 
     * {@link #contextAnalysis(Declarations) contextAnalysis} bestimmt.
     */
    private int objectSize;

    /**
     * Konstruktor.
     * @param name Der Name der deklarierten Klasse.
     * @param attributes Die Attribute dieser Klasse.
     * @param methods Die Methoden dieser Klasse.
     */
    public ClassDeclaration(Identifier name, LinkedList<VarDeclaration> attributes, LinkedList<MethodDeclaration> methods) {
        super(name);
        this.attributes = attributes;
        this.methods = methods;
    }

    /**
     * Privater Konstruktor für Klassenattribute.
     * @param name Der Name der deklarierten Klasse.
     */
    private ClassDeclaration(Identifier name) {
        this(name, new LinkedList<VarDeclaration>(), new LinkedList<MethodDeclaration>());
    }
    
    /**
     * Liefert die innerhalb dieser Klasse sichtbaren Deklarationen.
     * @return Die Deklarationen.
     */
    public Declarations getDeclarations() {
        return declarations;
    }
    
    /**
     * Liefert die Größe eines Objekts dieser Klasse.
     * @return Die Größe in benötigten Speicherwörtern.
     */
    public int getObjectSize() {
        return objectSize;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Klassen-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void contextAnalysis(Declarations declarations) throws CompileException {
    	 // Neuen Deklarationsraum schaffen
        declarations.enter();
    	
        // Attribute eintragen
        for (VarDeclaration a : attributes) {
            declarations.add(a);
        }
        
        // Methoden eintragen
        for (MethodDeclaration m : methods) {
            declarations.add(m);
        }
        
        // Wird auf ein Objekt dieser Klasse zugegriffen, werden die Deklarationen
        // in diesem Zustand benötigt. Deshalb werden sie in der Klasse gespeichert.
        this.declarations = (Declarations) declarations.clone();
        
        // Deklarationsraum verlassen
        declarations.leave();
    }
    
    
    /**
     * Die Methode resolvt die Typen der Klassendeklaration.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void resolve() throws CompileException {
    	
    	// Neuen Deklarationsraum schaffen
        declarations.enter();

        
        // Standardgröße für Objekte festlegen
        objectSize = HEADER_SIZE;
        
    	// Attributtypen auflösen und Indizes innerhalb des Objekts vergeben
        for (VarDeclaration a : attributes) {
            a.contextAnalysis(this.declarations);
            a.setOffset(objectSize++);
        }
        
        // Kontextanalyse für Methoden durchführen
        for (MethodDeclaration m : methods) {
            m.setSelfType(this);
            m.contextAnalysis(this.declarations);
        }
        
        // Deklarationsraum verlassen
        declarations.leave();
    }
     
    /**
     * Die Methode prüft, ob dieser Typ kompatibel mit einem anderen Typ ist.
     * @param expected Der Typ, mit dem verglichen wird.
     * @return Sind die beiden Typen sind kompatibel?
     */
    public boolean isA(ClassDeclaration expected) {
        // Spezialbehandlung für null, das mit allen Klassen kompatibel ist,
        // aber nicht mit den Basisdatentypen _Integer und _Boolean sowie auch nicht
        // an Stellen erlaubt ist, wo gar kein Wert erwartet wird.
        if (this == NULL_TYPE && expected != INT_TYPE && expected != BOOL_TYPE && expected != VOID_TYPE) {
            return true;
        } else {
            return this == expected;
        }
    }
    
    /**
     * Die Methode erzeugt eine Ausnahme für einen Typfehler. Sie wandelt dabei intern verwendete
     * Typnamen in die auch außen sichtbaren Namen um.
     * @param expected Der Typ, der nicht kompatibel ist.
     * @param position Die Stelle im Quelltext, an der der Typfehler gefunden wurde.
     * @throws CompileException Die Meldung über den Typfehler.
     */
    public static void typeError(ClassDeclaration expected, Position position) throws CompileException {
        if (expected == INT_TYPE) {
            throw new CompileException("Ausdruck vom Typ Integer erwartet", position);
        } else if (expected == BOOL_TYPE) {
            throw new CompileException("Ausdruck vom Typ Boolean erwartet", position);
        } else if (expected == ClassDeclaration.VOID_TYPE) {
            throw new CompileException("Hier darf kein Wert zurückgeliefert werden", position);
        } else {
            throw new CompileException("Ausdruck vom Typ " + expected.getIdentifier().getName() + " erwartet", position);
        }
    }

    /**
     * Die Methode prüft, ob dieser Typ kompatibel mit einem anderen Typ ist.
     * Sollte das nicht der Fall sein, wird eine Ausnahme mit einer Fehlermeldung generiert.
     * @param expected Der Typ, mit dem verglichen wird.
     * @param position Die Position im Quelltext, an der diese Überprüfung 
     *         relevant ist. Die Position wird in der Fehlermeldung verwendet.
     * @throws CompileException Die Typen sind nicht kompatibel.
     */
    public void check(ClassDeclaration expected, Position position) throws CompileException {
        if (!isA(expected)) {
            typeError(expected, position);
        }
    }
    
    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("CLASS " + getIdentifier().getName());
        tree.indent();
        if (!attributes.isEmpty()) {
            tree.println("ATTRIBUTES");
            tree.indent();
            for (VarDeclaration a : attributes) {
                a.print(tree);
            }
            tree.unindent();
        }
        if (!methods.isEmpty()) {
            tree.println("METHODS");
            tree.indent();
            for (MethodDeclaration m : methods) {
                m.print(tree);
            }
            tree.unindent();
        }
        tree.unindent();
    }

    /**
     * Generiert den Assembler-Code für diese Klasse. Dabei wird davon ausgegangen,
     * dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println("; CLASS " + getIdentifier().getName());
        
        // Synthese für alle Methoden
        for (MethodDeclaration m : methods) {
            m.generateCode(code);
        }
        code.println("; END CLASS " + getIdentifier().getName());
    }
}

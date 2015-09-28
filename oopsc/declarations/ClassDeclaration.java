package oopsc.declarations;

import java.util.ArrayList;
import java.util.LinkedList;

import oopsc.CompileException;
import oopsc.parser.Identifier;
import oopsc.parser.Position;
import oopsc.parser.ResolvableIdentifier;
import oopsc.parser.Symbol;
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
    public static final int HEADER_SIZE = 1;
    
    /** Ein interner Typ für das Ergebnis von Methoden. */
    public static final ClassDeclaration VOID_TYPE = new ClassDeclaration(new Identifier("_Void", null), null);

    /** Ein interner Typ für null. Dieser Typ ist kompatibel zu allen Klassen. */
    public static final ClassDeclaration NULL_TYPE = new ClassDeclaration(new Identifier("_Null", null), null);

    /** Der interne Basisdatentyp für Zahlen. */
    public static final ClassDeclaration INT_TYPE = new ClassDeclaration(new Identifier("_Integer", null), null);

    /** Der interne Basisdatentyp für Wahrheitswerte. */
    public static final ClassDeclaration BOOL_TYPE = new ClassDeclaration(new Identifier("_Boolean", null), null);

    /** Die Klasse Object. */
    public static final ClassDeclaration OBJECT_CLASS = new ClassDeclaration(new Identifier("Object", null), null);
    
    /** Die Klasse Integer. */
    public static final ClassDeclaration INT_CLASS = new ClassDeclaration(new Identifier("Integer", null), new ResolvableIdentifier("Object", null));
    
    /** Die Klasse Boolean. */
    public static final ClassDeclaration BOOL_CLASS = new ClassDeclaration(new Identifier("Boolean", null), new ResolvableIdentifier("Object", null));
         
    
    static {
        // Integer und Boolean enthalten ein Element
    	INT_CLASS.attributes.add(new VarDeclaration(new Identifier("_value", null), new ResolvableIdentifier("Integer", null), true, Symbol.Id.PUBLIC));
    	BOOL_CLASS.attributes.add(new VarDeclaration(new Identifier("_value", null), new ResolvableIdentifier("Boolean", null), true, Symbol.Id.PUBLIC));
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

    /** Der Basistyp der Klasse */
    private ResolvableIdentifier baseType;
    
    public ResolvableIdentifier getBaseType() {
		return baseType;
	}

	/** Gibt Auskunft, ob die Kontextanalyse gerade in Bearbeitung ist. */
    private boolean processing = false;
    
    /** Die virtuelle Methodentabelle der Klasse. */
    private ArrayList<MethodDeclaration> virtualMethodTable = new ArrayList<MethodDeclaration>();
    
    /**
     * Konstruktor.
     * @param name Der Name der deklarierten Klasse.
     * @param attributes Die Attribute dieser Klasse.
     * @param methods Die Methoden dieser Klasse.
     */
    public ClassDeclaration(Identifier name, ResolvableIdentifier baseType, LinkedList<VarDeclaration> attributes, LinkedList<MethodDeclaration> methods) {
        super(name, Symbol.Id.PUBLIC);
        this.attributes = attributes;
        this.methods = methods;
        this.baseType = baseType;
    }

    /**
     * Privater Konstruktor für Klassenattribute.
     * @param name Der Name der deklarierten Klasse.
     */
    private ClassDeclaration(Identifier name, ResolvableIdentifier baseType) {
        this(name, baseType, new LinkedList<VarDeclaration>(), new LinkedList<MethodDeclaration>());
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
     * Liefert die Attribute der Klasse zurück
     * @return Die Attribute der Klasse
     */
    public LinkedList<VarDeclaration> getAttributes() {
    	return attributes;
    }

    
    /**
     * Liefert die virtuelle Methodentabelle der Klasse.
     * @return Die virtuelle Methodentabelle der Klasse.
     */
    public ArrayList<MethodDeclaration> getVirtualMethodTable() {
		return virtualMethodTable;
	}


	/**
     * Die Methode führt die Kontextanalyse für diese Klassen-Deklaration durch.
     * Dabei analysiert diese nur Attribute
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void contextAnalysis(Declarations declarations) throws CompileException {
    	processing = true;
    	
    	if (baseType != null) { 
    		if (baseType.getDeclaration() == null) {
    			declarations.resolveType(baseType);	
    		}
    		
    		if (((ClassDeclaration)baseType.getDeclaration()).isProcessing()) {
	    		throw new CompileException("Zyklische Vererbung", baseType.getPosition());
	    	}
    		
    		((ClassDeclaration)baseType.getDeclaration()).contextAnalysis(declarations);
    		declarations = ((ClassDeclaration)baseType.getDeclaration()).getDeclarations();		
    		
    		
    	}
        
    	// Setze die Klasse aus der die Methoden stammen
    	declarations.setCallerClass(this); 
    	
    	 // Neuen Deklarationsraum schaffen
        declarations.enter();
    	
        // Attribute eintragen
        for (VarDeclaration a : attributes) {
            declarations.add(a);
        }
        
        // Methoden eintragen
        for (MethodDeclaration m : methods) {
            declarations.add(m);
            
            m.contextAnalysisForParams(declarations);
            m.contextAnalysisForReturnType(declarations);
        }
        
        // Wird auf ein Objekt dieser Klasse zugegriffen, werden die Deklarationen
        // in diesem Zustand benötigt. Deshalb werden sie in der Klasse gespeichert.
        this.declarations = (Declarations) declarations.clone();

    	
    	
        // Deklarationsraum verlassen
        declarations.leave();
        
        // Standardgröße für Objekte festlegen
        if (OBJECT_CLASS.isA(this)) {
        	objectSize = HEADER_SIZE;
        } else {
        	objectSize = ((ClassDeclaration)baseType.getDeclaration()).getObjectSize() + 1;
        }
        
        // Attributtypen auflösen und Indizes innerhalb des Objekts vergeben
        for (VarDeclaration a : attributes) {
            a.contextAnalysis(declarations);
            a.setOffset(objectSize++);
        }
        
        processing = false;
    }
    
    
    /**
     * Die Methode resolvt die Typen der Klassendeklaration.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void resolve() throws CompileException {        
    	if (!virtualMethodTable.isEmpty()) {
    		return;
    	}
    
        Declarations declarations = (Declarations) this.declarations.clone();
        
        ArrayList<MethodDeclaration> baseMethodTable = new ArrayList<MethodDeclaration>();
        if(baseType != null) {
            ClassDeclaration baseClass = (ClassDeclaration) baseType.getDeclaration();
            baseClass.resolve();
            baseMethodTable = baseClass.getVirtualMethodTable();
        }
        
        virtualMethodTable.addAll(baseMethodTable);
        
        // Kontextanalyse für Methoden durchführen
        for (MethodDeclaration m : methods) {
            m.setSelfType(this);
            m.setBaseType(this);
            m.setReturnType();
            m.contextAnalysis(declarations);
            boolean added = false;
        	for(int i = 0; i < baseMethodTable.size(); ++i) {
        		if (baseMethodTable.get(i).is(m)) {
        			virtualMethodTable.set(i, m);
        			added = true;
        		}
        	}
        	if(!added) {
        		virtualMethodTable.add(m);
        	}
        }
        
        for(int i = 0; i < virtualMethodTable.size(); ++i) {
        	virtualMethodTable.get(i).setVMTIndex(i);
        }
    }
     
    /**
     * Die Methode prüft, ob dieser Typ kompatibel mit einem anderen Typ ist.
     * @param expected Der Typ, mit dem verglichen wird.
     * @return Sind die beiden Typen sind kompatibel?
     */
    public boolean isA(ClassDeclaration expected) {
        return this == expected || this == NULL_TYPE && expected.isA(OBJECT_CLASS) || this != OBJECT_CLASS && baseType != null && ((ClassDeclaration) baseType.getDeclaration()).isA(expected) ;   
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
        tree.println("CLASS " + getIdentifier().getName() + (baseType != null ? " EXTENDS " + baseType.getName() : ""));
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
     * Durchläuft den Syntaxbaum und wertet konstante Ausdrücke aus 
     * und wendet ein paar Transformationen an.
     */
    public void optimize() {
    	for(MethodDeclaration m : methods) {
    		m.optimize();
    	}
    }
    
    
    /**
     * Generiert den Assembler-Code für diese Klasse. Dabei wird davon ausgegangen,
     * dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println("; CLASS " + getIdentifier().getName());

        //Label für die VMT
        code.println(getIdentifier().getName() + ":");
        
        //Adressen der VMT
        for(int i = 0; i < virtualMethodTable.size(); ++i) {
        	code.println("DAT 1, " + virtualMethodTable.get(i).getSelfType().getIdentifier().getName() + "_" + virtualMethodTable.get(i).getIdentifier().getName());
        }
        
        // Synthese für alle Methoden
        for (MethodDeclaration m : methods) {
            m.generateCode(code);
        }
        
        
        code.println("; END CLASS " + getIdentifier().getName());
    }

	/**
	 * @return the processing
	 */
	public boolean isProcessing() {
		return processing;
	}

}

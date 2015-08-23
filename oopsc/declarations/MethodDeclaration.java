package oopsc.declarations;

import java.util.LinkedList;
import oopsc.CompileException;
import oopsc.parser.Identifier;
import oopsc.parser.Position;
import oopsc.parser.ResolvableIdentifier;
import oopsc.statements.Statement;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert eine Methode im Syntaxbaum.
 */
public class MethodDeclaration extends Declaration {
    /** Die lokale Variable SELF. */
    private VarDeclaration self;
    
    private VarDeclaration result;
    
    /** Die lokalen Variablen der Methode. */
    private final LinkedList<VarDeclaration> vars;
    
    /** Die Anweisungen der Methode, d.h. der Methodenrumpf. */
    private final LinkedList<Statement> statements;
    
    /** Die Parameter der Methode */
    private final LinkedList<VarDeclaration> params;
    
    /** Die Quelltextposition des Methodenendes. */
    private final Position endPosition;
    
    /** Der Rückgabetyp der Methode */
    private final ResolvableIdentifier returnType;
    

	/**
     * Konstruktor.
     * @param name Der Name der deklarierten Methode.
     * @param returnIdent 
     * @param vars Die lokalen Variablen der Methode.
     * @param statements Die Anweisungen der Methode, d.h. der Methodenrumpf.
     * @param endPosition Die Quelltextposition des Methodenendes.
     */
    public MethodDeclaration(Identifier name, LinkedList<VarDeclaration> params, ResolvableIdentifier returnType, LinkedList<VarDeclaration> vars, LinkedList<Statement> statements,
            Position endPosition) {
        super(name);
        this.vars = vars;
        this.statements = statements;
        this.endPosition = endPosition;
        this.params = params;
        this.returnType = returnType;
    }

    void setReturnType() {
    	assert result == null;
    	
    	if(returnType != null) {
    		result = new VarDeclaration(new Identifier("_result", null), returnType , false);
    		result.getType().setDeclaration(returnType.getDeclaration());
    	} else {
    		result = new VarDeclaration(new Identifier("_result", null), new ResolvableIdentifier("Void", null), false);
    		result.getType().setDeclaration(ClassDeclaration.VOID_TYPE);
    	}
    }
    
    /**
     * Setzt die Klasse, zu der diese Methode gehört.
     * Dies muss vor der Kontextanalyse gemacht werden.
     * @param selfType Der Typ von SELF.
     */
    void setSelfType(ClassDeclaration selfType) {
        assert self == null;
        
        // SELF ist Variable vom Typ dieser Klasse
        self = new VarDeclaration(new Identifier("_self", null), 
                new ResolvableIdentifier(selfType.getIdentifier().getName(), null), false);
        self.getType().setDeclaration(selfType);
    }
    
    /**
     * Liefert die Klasse, zu der diese Methode gehört.
     * Sie muss vorher gesetzt worden sein.
     * @return Der Typ von SELF.
     */
    public ClassDeclaration getSelfType() {
        return (ClassDeclaration) self.getType().getDeclaration();
    }
    
    /**
     * Führt die Kontextanalyse für die Parameter der Methode aus.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Ein Fehler trat während der Kontextanalyse auf.
     */
    public void contextAnalysisForParams(Declarations declarations) throws CompileException {
    	for (VarDeclaration p : params) {
    		p.contextAnalysis(declarations);
    	}
    }
    
    /**
     * Führt die Kontextanalyse für den Rückgabetypen aus.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Ein Fehler trat während der Kontextanalyse auf.
     */
    public void contextAnalysisForReturnType(Declarations declarations) throws CompileException {
    	if (returnType != null) {
    		declarations.resolveType(returnType);	
    	}  	
	}
    
    /**
     * Führt die Kontextanalyse für diese Methoden-Deklaration durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis(Declarations declarations) throws CompileException {
    	
    	// Neuen Deklarationsraum schaffen
        declarations.enter();
        
        // SELF eintragen
        assert self != null;
        declarations.add(self);
 
        int offset = -(params.size()+2);
        
        // SELF liegt vor der Rücksprungadresse auf dem Stapel
        self.setOffset(offset);
 
        // _result liegt an derselben Stelle wie SELF
        result.setOffset(offset);
        declarations.add(result);
        
        for (VarDeclaration p : params) {
        	declarations.add(p);
        	p.setOffset(++offset);
        }
        
        // Rücksprungadresse und alten Rahmenzeiger überspringen
        offset = 1;
        
        // Lokale Variablen eintragen
        for (VarDeclaration v : vars) {
            declarations.add(v);
            v.setOffset(offset++);
        }
        
        // Löse Typen aller Variablen auf
        for (VarDeclaration v : vars) {
            v.contextAnalysis(declarations);
        }
        
        declarations.add(new VarDeclaration(new Identifier("_methodend", null), new ResolvableIdentifier("end_method_"+getSelfType().getIdentifier().getName()+"_"+getIdentifier().getName(), null), false));
        
        // Kontextanalyse aller Anweisungen durchführen
        for (Statement s : statements) {
            s.contextAnalysis(declarations);
        }
        
        // Überprüfe ob die Methode ein Return-Statement erreicht.
        if (returnType != null) {
        	boolean returns = false;
        	for (Statement s : statements) {
        		returns |= s.returns();
        	}
        	if (!returns) {
        		throw new CompileException("Kein Return-Statement erreichbar.", null);
        	}
        }
        
        // Alten Deklarationsraum wiederherstellen
        declarations.leave();
    }
    
    /**
     * Die Methode gibt diese Deklaration in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    void print(TreeStream tree) {
        tree.println("METHOD " + getIdentifier().getName());
        tree.indent();
        if (!params.isEmpty()) {
            tree.println("PARAMS");
            tree.indent();
            for (VarDeclaration p : params) {
                p.print(tree);
            }
            tree.unindent();
        }
        tree.indent();
        if (!vars.isEmpty()) {
            tree.println("VARIABLES");
            tree.indent();
            for (VarDeclaration v : vars) {
                v.print(tree);
            }
            tree.unindent();
        }
        if (!statements.isEmpty()) {
            tree.println("BEGIN");
            tree.indent();
            for (Statement s : statements) {
                s.print(tree);
            }
            tree.unindent();
        }
        tree.unindent();
        tree.unindent();
    }

    /**
     * Generiert den Assembler-Code für diese Methode. Dabei wird davon ausgegangen,
     * dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code) {
        code.println(getIdentifier().getPosition());
        code.setNamespace(self.getType().getName() + "_" + getIdentifier().getName());
        code.println("; METHOD " + getIdentifier().getName());
        code.println(self.getType().getName() + "_" + getIdentifier().getName() + ":");
        code.println("ADD R2, R1");
        code.println("MMR (R2), R3 ; Alten Stapelrahmen sichern");
        code.println("MRR R3, R2 ; Aktuelle Stapelposition ist neuer Rahmen");
        if (!vars.isEmpty()) {
            code.println("MRI R5, " + vars.size());
            code.println("ADD R2, R5 ; Platz für lokale Variablen schaffen");
        }
        for (Statement s : statements) {
            s.generateCode(code);
        }
        code.println(endPosition);
        code.println("; END METHOD " + getIdentifier().getName());
        code.println("end_method_"+getSelfType().getIdentifier().getName()+"_"+getIdentifier().getName()+":");
        code.println("MRI R5, " + (vars.size() + (ClassDeclaration.VOID_TYPE.isA(((ClassDeclaration)result.getType().getDeclaration())) ? 3 : 2) + params.size()));
        code.println("SUB R2, R5 ; Stack korrigieren");
        code.println("SUB R3, R1");
        code.println("MRM R5, (R3) ; Rücksprungadresse holen");
        code.println("ADD R3, R1");
        code.println("MRM R3, (R3) ; Alten Stapelrahmen holen");
        code.println("MRR R0, R5 ; Rücksprung");
    }

    /**
     * Getter für die Parameter.
     * @return Die Parameter
     */
	public LinkedList<VarDeclaration> getParams() {
		return params;
	}
	
    /**
     * Getter für den Rückgabetyp.
	 * @return the returnIdent
	 */
	public ResolvableIdentifier getReturnType() {
		return returnType;
	}

	
}

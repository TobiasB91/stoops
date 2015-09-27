package oopsc.expressions;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der einen Wert vom
 * vom Typ eines Basisdatentyps "boxt", d.h. in ein Objekt verpackt.
 * Dieser Ausdruck wird immer nachträglich während der Kontextanalyse in
 * den Syntaxbaum eingefügt.
 */
public class BoxExpression extends Expression {
    /** Der Ausdruck, der den zu verpackenden Wert liefert. */
    private Expression operand;

    /** Ein Ausdruck, der das entsprechende Rahmenobjekt erzeugt. */
    private Expression newType;

    /**
     * Konstruktor.
     * Der Konstruktor stellt fest, von welchem Basisdatentyp der zu
     * verpackende Ausdruck ist und erzeugt dann ein passendes Rahmenobjekt.
     * @param operand Der Ausdruck, der den zu verpackenden Wert liefert.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse des neuen Objekts 
     *         wurde ein Fehler gefunden.
     */
    public BoxExpression(Expression operand, Declarations declarations) throws CompileException {
        super(operand.getPosition());
        this.operand = operand;
        if (operand.getType().isA(ClassDeclaration.INT_TYPE)) {
            setType(ClassDeclaration.INT_CLASS);
            newType = new NewExpression(new ResolvableIdentifier("Integer", null), operand.getPosition());
        } else if(operand.getType().isA(ClassDeclaration.BOOL_TYPE)) {
        	setType(ClassDeclaration.BOOL_CLASS);
            newType = new NewExpression(new ResolvableIdentifier("Boolean", null), operand.getPosition());
        } else {
            assert false;
        }
        newType = newType.contextAnalysis(declarations);
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("BOX" + (getType() == null ? "" : " : " + getType().getIdentifier().getName()));
        tree.indent();
        operand.print(tree);
        tree.unindent();
    }

    /**
     * Durchläuft den Syntaxbaum und wertet konstante Ausdrücke aus 
     * und wendet ein paar Transformationen an.
     */
    public Expression optimize() {
    	operand = operand.optimize();
    	return this;
	}
    
    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        newType.generateCode(code);
        operand.generateCode(code);
        code.println("; BOX");
        code.println("MRM R5, (R2) ; Wert vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("MRM R6, (R2) ; Referenz auf neues Objekt holen (bleibt auf Stapel)");
        code.println("MRI R7, " + ClassDeclaration.HEADER_SIZE);
        code.println("ADD R6, R7 ; Speicherstelle in neuem Objekt berechnen");
        code.println("MMR (R6), R5 ; Wert in Objekt speichern");
    }
}

package oopsc.expressions;

import oopsc.declarations.ClassDeclaration;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der ein Objekt in
 * einen Wert eines Basisdatentyps auspackt ("unboxing").
 * Dieser Ausdruck wird immer nachträglich während der Kontextanalyse in
 * den Syntaxbaum eingefügt.
 */
public class UnBoxExpression extends Expression {
    /** Der Ausdruck, der das auszupackende Objekt berechnet. */
    private Expression operand;
    
    /**
     * Konstruktor.
     * Der Konstruktor stellt fest, von welcher Klasse der auszupackende
     * Ausdruck ist bestimmt den entsprechenden Basisdatentyp.
     * @param operand Der Ausdruck, der das auszupackende Objekt berechnet.
     */
    public UnBoxExpression(Expression operand) {
        super(operand.getPosition());
        this.operand = operand;
        if (operand.getType().isA(ClassDeclaration.INT_CLASS)) {
            setType(ClassDeclaration.INT_TYPE);
        } else if(operand.getType().isA(ClassDeclaration.BOOL_CLASS)) {
        	setType(ClassDeclaration.BOOL_TYPE);
        } else {
            assert false;
        }
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("UNBOX" + (getType() == null ? "" : " : " + getType().getIdentifier().getName()));
        tree.indent();
        operand.print(tree);
        tree.unindent();
    }

    /**
     * Durchläuft den Syntaxbaum und wertet konstante Ausdrücke aus 
     * und wendet ein paar Transformationen an.
     * @return Der optimierte Ausdruck.
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
        operand.generateCode(code);
        code.println("; UNBOX");
        code.println("MRM R5, (R2) ; Objektreferenz vom Stapel lesen");
        code.println("MRI R6, " + ClassDeclaration.HEADER_SIZE);
        code.println("ADD R5, R6 ; Adresse des Werts bestimmen");
        code.println("MRM R5, (R5) ; Wert auslesen");
        code.println("MMR (R2), R5 ; und auf den Stapel schreiben");
    }
}

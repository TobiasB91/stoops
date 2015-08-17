package oopsc.expressions;

import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der eine Referenz
 * dereferenziert, d.h. aus einer Variablen, deren Adresse gegeben ist, den
 * Wert ausliest.
 * Dieser Ausdruck wird immer nachträglich während der Kontextanalyse in
 * den Syntaxbaum eingefügt.
 */
public class DeRefExpression extends Expression {
    /** Der Ausdruck, der die Adresse berechnet. */
    private final Expression operand;
    
    /**
     * Konstruktor.
     * @param operand Der Ausdruck, der die Adresse berechnet.
     */
    public DeRefExpression(Expression operand) {
        super(operand.getPosition());
        this.operand = operand;
        setType(operand.getType());
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("DEREF" + (getType() == null ? "" : " : " + getType().getIdentifier().getName()));
        tree.indent();
        operand.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println("; DEREF");
        operand.generateCode(code);
        code.println("MRM R5, (R2) ; L-Wert vom Stapel holen");
        code.println("MRM R5, (R5) ; Dereferenzieren");
        code.println("MMR (R2), R5 ; Wieder ablegen");
    }
}

package oopsc.expressions;

import oopsc.CompileException;
import oopsc.declarations.Declarations;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck mit einem Attribut- bzw.
 * Methoden-Zugriffsoperator (d.h. der Punkt) im Syntaxbaum.
 */
public class AccessExpression extends Expression {
    /** Der linke Operand. */
    private Expression leftOperand;

    /** Der rechte Operand. */
    private VarOrCall rightOperand;
    
    /**
     * Konstruktor.
     * @param leftOperand Der linke Operand.
     * @param rightOperand Der rechte Operand.
     */
    public AccessExpression(Expression leftOperand, VarOrCall rightOperand) {
        super(leftOperand.getPosition());
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }
    
    /**
     * Ist dieser Ausdruck ein L-Wert, d.h. eine Referenz auf eine Variable?
     * @return Wenn der rechte Operand auch einer ist, dann ja.
     */
    public boolean isLValue() {
        return rightOperand.isLValue();
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public Expression contextAnalysis(Declarations declarations) throws CompileException {
        leftOperand = leftOperand.contextAnalysis(declarations);

        // Dereferenzieren. Außerdem könnte man einen Ausdruck wie z.B. 5.print
        // schreiben, wenn Integer Methoden hätte.
        leftOperand = leftOperand.box(declarations);

        // Der rechte Operand hat einen Deklarationsraum, der sich aus dem 
        // Ergebnistyp des linken Operanden ergibt.
        rightOperand.contextAnalysis(leftOperand.getType().getDeclarations(), false);

        // Der Typ dieses Ausdrucks ist immer der des rechten Operanden.
        setType(rightOperand.getType());

        return this;
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("PERIOD" + (getType() == null ? "" : " : " + 
                (isLValue() ? "REF " : "") + getType().getIdentifier().getName()));
        tree.indent();
        leftOperand.print(tree);
        rightOperand.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        leftOperand.generateCode(code);
        rightOperand.generateCode(code);
    }
}

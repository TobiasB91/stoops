package oopsc.statements;

import oopsc.CompileException;
import oopsc.declarations.Declarations;
import oopsc.expressions.Expression;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Zuweisung im Syntaxbaum.
 */
public class Assignment extends Statement {
    /** Der L-Wert, dem ein neuer Wert zugewiesen wird. */
    private Expression leftOperand;
    
    /** Der Ausdruck, dessen Ergebnis zugewiesen wird. */
    private Expression rightOperand;
    
    /**
     * Konstruktor.
     * @param leftOperand Der L-Wert, dem ein neuer Wert zugewiesen wird.
     * @param rightOperand Der Ausdruck, dessen Ergebnis zugewiesen wird.
     */
    public Assignment(Expression leftOperand, Expression rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }
    
    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void contextAnalysis(Declarations declarations) throws CompileException {
        leftOperand = leftOperand.contextAnalysis(declarations);
        rightOperand = rightOperand.contextAnalysis(declarations);
        if (!leftOperand.isLValue()) {
            throw new CompileException("L-Wert erwartet", leftOperand.getPosition());
        }
        rightOperand = rightOperand.box(declarations);
        rightOperand.getType().check(leftOperand.getType(), rightOperand.getPosition());
    }

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("ASSIGNMENT");
        tree.indent();
        leftOperand.print(tree);
        rightOperand.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println("; ASSIGNMENT");
        rightOperand.generateCode(code);
        leftOperand.generateCode(code);
        code.println("MRM R5, (R2) ; Referenz auf linken Wert vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("MRM R6, (R2) ; Rechten Wert vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("MMR (R5), R6 ; Zuweisen");
    }
}

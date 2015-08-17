package oopsc.statements;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.expressions.Expression;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Anweisung WRITE im Syntaxbaum.
 */
public class WriteStatement extends Statement {
    /** Der Ausdruck, der als ein Zeichen ausgegeben wird. */
    private Expression operand;
    
    /**
     * Konstruktor.
     * @param operand Der Ausdruck, der als ein Zeichen ausgegeben wird.
     */
    public WriteStatement(Expression operand) {
        this.operand = operand;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void contextAnalysis(Declarations declarations) throws CompileException {
        operand = operand.contextAnalysis(declarations);
        operand = operand.unBox();
        operand.getType().check(ClassDeclaration.INT_TYPE, operand.getPosition());
    }

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("WRITE");
        tree.indent();
        operand.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println("; WRITE");
        operand.generateCode(code);
        code.println("MRM R5, (R2) ; Auszugebenden Wert vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("SYS 1, 5 ; Wert ausgeben");
    }
}

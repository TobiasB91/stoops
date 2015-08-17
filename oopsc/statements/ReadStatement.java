package oopsc.statements;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.expressions.Expression;
import oopsc.expressions.NewExpression;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Anweisung READ im Syntaxbaum.
 */
public class ReadStatement extends Statement {
    /** Die Variable, in der das eingelesene Zeichen gespeichert wird. */
    private Expression operand;
    
    /** Ein Ausdruck, der ein neues Objekt vom Typ Integer erzeugen kann. */
    private Expression newInt;
    
    /**
     * Konstruktor.
     * @param operand Die Variable, in der das eingelesene Zeichen gespeichert wird.
     */
    public ReadStatement(Expression operand) {
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
        if (!operand.isLValue()) {
            throw new CompileException("L-Wert erwartet", operand.getPosition());
        }
        operand.getType().check(ClassDeclaration.INT_CLASS, operand.getPosition());
        newInt = new NewExpression(new ResolvableIdentifier("Integer", null), operand.getPosition())
                .contextAnalysis(declarations);
    }

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("READ");
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
        code.println("; READ");
        newInt.generateCode(code);
        operand.generateCode(code);
        code.println("MRM R6, (R2) ; Ziel vom Stapel entnehmen");
        code.println("SUB R2, R1");
        code.println("MRM R5, (R2) ; Geboxten Integer vom Stapel entnehmen"); 
        code.println("SUB R2, R1");
        code.println("MMR (R6), R5 ; Zuweisen");
        code.println("MRI R6, " + ClassDeclaration.HEADER_SIZE);
        code.println("ADD R5, R6");
        code.println("SYS 0, 6 ; Gelesenen Wert in R6 ablegen");
        code.println("MMR (R5), R6 ; Zeichen in neuen Integer schreiben");
    }
}

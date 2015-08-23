package oopsc.statements;

import java.util.LinkedList;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.expressions.Expression;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Anweisung WHILE im Syntaxbaum.
 */
public class WhileStatement extends Statement {
    /** Die Bedingung der WHILE-Anweisung. */
    private Expression condition;
    
    /** Die Anweisungen im Schleifenrumpf. */
    private final LinkedList<Statement> statements;
    
    /**
     * Konstruktor.
     * @param condition Die Bedingung der WHILE-Anweisung.
     * @param statements Die Anweisungen im Schleifenrumpf.
     */
    public WhileStatement(Expression condition, LinkedList<Statement> statements) {
        this.condition = condition;
        this.statements = statements;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void contextAnalysis(Declarations declarations) throws CompileException {
        condition = condition.contextAnalysis(declarations);
        condition = condition.unBox();
        condition.getType().check(ClassDeclaration.BOOL_TYPE, condition.getPosition());
        for (Statement s : statements) {
            s.contextAnalysis(declarations);
        }
    }

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("WHILE");
        tree.indent();
        condition.print(tree);
        if (!statements.isEmpty()) {
            tree.println("DO");
            tree.indent();
            for (Statement s : statements) {
                s.print(tree);
            }
            tree.unindent();
        }
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        String whileLabel = code.nextLabel();
        String endLabel = code.nextLabel();
        code.println("; WHILE");
        code.println(whileLabel + ":");
        condition.generateCode(code);
        code.println("MRM R5, (R2) ; Bedingung vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("ISZ R5, R5 ; Wenn 0, dann");
        code.println("JPC R5, " + endLabel + " ; Schleife verlassen");
        code.println("; DO");
        for (Statement s : statements) {
            s.generateCode(code);
        }
        code.println("; END WHILE");
        code.println("MRI R0, " + whileLabel);
        code.println(endLabel + ":");
    }
    
}

package oopsc.statements;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.expressions.Expression;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Anweisung "Methodenaufruf" im Syntaxbaum.
 */
public class CallStatement extends Statement {
    /** Der Ausdruck, der den Methodenaufruf repräsentiert. */
    private Expression call;
    
    /**
     * Konstruktor.
     * @param call Der Ausdruck, der den Methodenaufruf repräsentiert.
     */
    public CallStatement(Expression call) {
        this.call = call;
    }

    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public void contextAnalysis(Declarations declarations) throws CompileException {
        call = call.contextAnalysis(declarations);
        call.getType().check(ClassDeclaration.VOID_TYPE, call.getPosition());
    }

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("CALL");
        tree.indent();
        call.print(tree);
        tree.unindent();
    }

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println("; CALL");
        call.generateCode(code);
    }
    
}

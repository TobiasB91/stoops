package oopsc.statements;

import java.util.LinkedList;
import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.expressions.Expression;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert die Anweisung IF-THEN im Syntaxbaum.
 */
public class IfStatement extends Statement {
    /** Die Bedingung der IF-Anweisung. */
    private Expression condition;
    
    /** Die Anweisungen im THEN-Teil. */
    private final LinkedList<Statement> thenStatements;

    /** Die Anweisungen im ELSE-Teil. */
    private final LinkedList<Statement> elseStatements;
    
    /**
     * Konstruktor.
     * @param condition Die Bedingung der IF-Anweisung.
     * @param thenStatements Die Anweisungen im THEN-Teil.
     * @param elseStatements Die Anweisungen im ELSE-Teil.
     */
    public IfStatement(Expression condition, LinkedList<Statement> thenStatements, LinkedList<Statement> elseStatements) {
        this.condition = condition;
        this.thenStatements = thenStatements;
        this.elseStatements = elseStatements;
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
        for (Statement s : thenStatements) {
            s.contextAnalysis(declarations);
        }
        for (Statement s :  elseStatements) {
        	s.contextAnalysis(declarations);
        }
    }
    
    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("IF");
        tree.indent();
        condition.print(tree);
        if (!thenStatements.isEmpty()) {
            tree.println("THEN");
            tree.indent();
            for (Statement s : thenStatements) {
                s.print(tree);
            }
            tree.unindent();
        }
        if(!elseStatements.isEmpty()) {
        	tree.println("ELSE");
        	tree.indent();
        	for (Statement s : elseStatements) {
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
        String endLabel = code.nextLabel();
        String elseLabel = code.nextLabel();
        code.println("; IF");
        condition.generateCode(code);
        code.println("MRM R5, (R2) ; Bedingung vom Stapel nehmen");
        code.println("SUB R2, R1");
        code.println("ISZ R5, R5 ; Wenn 0, dann");
        code.println("JPC R5, " + elseLabel + " ; Sprung zu ELSE");
        code.println("; THEN");
        for (Statement s : thenStatements) {
            s.generateCode(code);
        }
        code.println("MRI R0, " + endLabel + " ; Sprung zu END IF");
        code.println("; ELSE");
        code.println(elseLabel + ":");
        for (Statement s : elseStatements) {
        	s.generateCode(code);
        }
        code.println("; END IF"); 
        code.println(endLabel + ":");
    }
}

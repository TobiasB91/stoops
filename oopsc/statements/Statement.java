package oopsc.statements;

import oopsc.CompileException;
import oopsc.declarations.Declarations;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die abstrakte Basisklasse für alle Anweisungen im Syntaxbaum.
 */
public abstract class Statement {
	/** 
	 * Die Methode prüft, ob ein Statement etwas zurückgibt.
	 * @return Returned?
	 */
	public boolean returns() {
		return false;
	}
	
	
    /**
     * Die Methode führt die Kontextanalyse für diese Anweisung durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public abstract void contextAnalysis(Declarations declarations) throws CompileException;

    /**
     * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public abstract void print(TreeStream tree);

    /**
     * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public abstract void generateCode(CodeStream code);
}

package oopsc.expressions;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.parser.Position;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck im Syntaxbaum, der ein neues Objekt erzeugt.
 */
public class NewExpression extends Expression {
    /** Der Typ des neuen Objekts. */
    private final ResolvableIdentifier newType;
    
    /**
     * Konstruktor.
     * @param newType Der Typ des neuen Objekts.
     * @param position Die Position, an der dieser Ausdruck im Quelltext beginnt.
     */
    public NewExpression(ResolvableIdentifier newType, Position position) {
        super(position);
        this.newType = newType;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public Expression contextAnalysis(Declarations declarations) throws CompileException {
        declarations.resolveType(newType);
        setType((ClassDeclaration) newType.getDeclaration());
        return this;
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println("NEW " + newType.getName() + (getType() == null ? "" : " : " + getType().getIdentifier().getName()));
    }

    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println(getPosition());
        code.println("; NEW " + newType.getName());
        code.println("ADD R2, R1");
        code.println("MRI R7, " + ((ClassDeclaration)newType.getDeclaration()).getIdentifier().getName());
        //code.println("MMR (R4), R7");
        code.println("MRI R6, _heap");
        code.println("MRM R5, (R6)");
        code.println("MMR (R5), R7");
        //code.println("MMR (R2), R4 ; Referenz auf neues Objekt auf den Stapel legen");
        code.println("MMR (R2), R5 ; Referenz auf neues Objekt auf den Stapel legen");
        code.println("MRI R7, " + ((ClassDeclaration) newType.getDeclaration()).getObjectSize());
        //code.println("ADD R4, R5 ; Heap weiter zählen");
        code.println("ADD R5, R7");
        code.println("MMR (R6), R5");
    }
}

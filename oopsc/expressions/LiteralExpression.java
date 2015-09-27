package oopsc.expressions;

import oopsc.declarations.ClassDeclaration;
import oopsc.parser.Position;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck mit einem Literal im Syntaxbaum.
 */
public class LiteralExpression extends Expression {
    /** Der Wert des Literals. */
    private final int value;
    
    /**
     * Konstruktor.
     * @param value Der Wert des Literals.
     * @param type Der Typ des Literals.
     * @param position Die Position, an der dieser Ausdruck im Quelltext beginnt.
     */
    public LiteralExpression(int value, ClassDeclaration type, Position position) {
        super(position);
        this.value = value;
        setType(type);
    }

    /**
     * Die Methode gibt dieses Literal und seinen Typ in einer Baumstruktur aus.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println(value + " : " + getType().getIdentifier().getName());
    }

    /**
     * Gibt den Wert des Literals zurück. Wird für die Auswertung konstanter Ausdrücke benötigt.
     * @return Der  Wert des Literals.
     */
    public int getValue() {
    	return value;
    }
    
    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        code.println(getPosition());
        code.println("; " + value + " : " + getType().getIdentifier().getName());
        code.println("MRI R5, " + value);
        code.println("ADD R2, R1");
        code.println("MMR (R2), R5");
    }
}

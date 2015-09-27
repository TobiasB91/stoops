package oopsc.expressions;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.parser.Position;
import oopsc.parser.Symbol;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert einen Ausdruck mit einem unären Operator im Syntaxbaum.
 */
public class UnaryExpression extends Expression {
    /** Der Operator. */
    private final Symbol.Id operator;
    
    /** Der Operand, auf den der Operator angewendet wird. */
    private Expression operand;
    
    /**
     * Konstruktor.
     * @param operator Der Operator.
     * @param operand Der Operand, auf den der Operator angewendet wird.
     * @param position Die Position, an der dieser Ausdruck im Quelltext beginnt.
     */
    public UnaryExpression(Symbol.Id operator, Expression operand, Position position) {
        super(position);
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
     * @param declarations Die an dieser Stelle gültigen Deklarationen.
     * @return Dieser Ausdruck.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    public Expression contextAnalysis(Declarations declarations) throws CompileException {
        operand = operand.contextAnalysis(declarations);
        operand = operand.unBox();
        switch (operator) {
        case MINUS:
            operand.getType().check(ClassDeclaration.INT_TYPE, operand.getPosition());
            break;
        case NOT:
            operand.getType().check(ClassDeclaration.BOOL_TYPE, operand.getPosition());
            break;
        default:
            assert false;
        }
        setType(operand.getType());
        return this;
    }

    /**
     * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
     * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
     * @param tree Der Strom, in den die Ausgabe erfolgt.
     */
    public void print(TreeStream tree) {
        tree.println(operator + (getType() == null ? "" : " : " + getType().getIdentifier().getName()));
        tree.indent();
        operand.print(tree);
        tree.unindent();
    }

    /**
     * Durchläuft den Syntaxbaum und wertet konstante Ausdrücke aus 
     * und wendet ein paar Transformationen an.
     */
    public Expression optimize() {
    	operand = operand.optimize();
    	if(operand instanceof LiteralExpression) {
    		switch (operator) {
    		case MINUS:	
    			return new LiteralExpression(-((LiteralExpression)operand).getValue() , ClassDeclaration.INT_TYPE, operand.getPosition());
    		case NOT:
    			return new LiteralExpression(((LiteralExpression)operand).getValue() == 1 ? 0 : 1, ClassDeclaration.BOOL_TYPE, operand.getPosition());
    		
    		default:
    			assert false;
    		}
    	}
    	return this;
	}
    
    /**
     * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    public void generateCode(CodeStream code) {
        operand.generateCode(code);
        code.println("; " + operator);
        code.println("MRM R5, (R2)");
        switch (operator) {
        case MINUS:
            code.println("MRI R6, 0");
            code.println("SUB R6, R5");
            code.println("MMR (R2), R6");
            break;
        case NOT:
        	code.println("XOR R5, R1"); //negiere mit XOR und speichere in R5
        	code.println("MMR (R2), R5"); 
        	break;
        default:
            assert false;
        }
    }
}

package oopsc.statements;

import java.util.LinkedList;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.declarations.VarDeclaration;
import oopsc.expressions.Expression;
import oopsc.expressions.VarOrCall;
import oopsc.parser.Position;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

public class ReturnStatement extends Statement {

	/** Der Ausdruck, der mit diesem Statement zurückgegeben werden soll. */
	private Expression returnExpression;
	
	private Assignment returnAssignment;	
	
	
	private Position position;
	
	/**
	 * Konstruktor.
	 * @param expr Ausdruck, der zurückgegeben werden soll. 
	 */
	public ReturnStatement(Expression returnExpression, Position position) {		
		this.returnExpression = returnExpression;
		this.position = position;
	}
	
	@Override
	public void contextAnalysis(Declarations declarations) throws CompileException {
		if (returnExpression != null) {
			
			ResolvableIdentifier result = new ResolvableIdentifier("_result", returnExpression.getPosition());
			declarations.resolveVarOrMethod(result);
			
			if(ClassDeclaration.VOID_TYPE.isA(((ClassDeclaration)((VarDeclaration)result.getDeclaration()).getType().getDeclaration()))) {
				throw new CompileException("Hier darf kein Wert zurückgeliefert werden", returnExpression.getPosition());
			}
			
			returnExpression = returnExpression.contextAnalysis(declarations).box(declarations);
			
			
			returnAssignment = new Assignment(new VarOrCall(result,new LinkedList<Expression>()), returnExpression);
			
			if(!(((ClassDeclaration)((VarDeclaration)result.getDeclaration()).getType().getDeclaration())).isA(returnExpression.getType())) {
				throw new CompileException("Ausdruck vom Typ " + ((VarDeclaration)result.getDeclaration()).getType().getName() + " erwartet", returnExpression.getPosition());
			}
		} else {
		
 			ResolvableIdentifier result = new ResolvableIdentifier("_result", null);
			declarations.resolveVarOrMethod(result);
			
			if ( !ClassDeclaration.VOID_TYPE.isA( ((ClassDeclaration)((VarDeclaration)result.getDeclaration()).getType().getDeclaration())) ) {
				throw new CompileException("Rückgabewert erwartet", position);
			}
			
		}

	}

	@Override
	public void print(TreeStream tree) {
		 tree.println("RETURN");
	        tree.indent();
	        if(returnExpression != null) {
	        	returnExpression.print(tree);
	        }
	        tree.unindent();
	}

	/**
     * Durchläuft den Syntaxbaum und wertet konstante Ausdrücke aus 
     * und wendet ein paar Transformationen an.
     */
    public void optimize() {
    	if (returnExpression != null) {
    		returnExpression = returnExpression.optimize();
	    }
    }
	
	@Override
	public void generateCode(CodeStream code) {
		if (returnExpression != null) {
			returnAssignment.generateCode(code);
		}
		code.println("MRI R0, "+code.getEndlabel());
	}
	
	@Override
	public boolean returns() {
		return true;
	}
	

}

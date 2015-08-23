package oopsc.statements;

import java.util.LinkedList;

import oopsc.CompileException;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.declarations.VarDeclaration;
import oopsc.expressions.Expression;
import oopsc.expressions.VarOrCall;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

public class ReturnStatement extends Statement {

	/** Der Ausdruck, der mit diesem Statement zurückgegeben werden soll. */
	private Expression returnExpression;
	
	private Assignment returnAssignment;	
	
	private String endLabel; 
	
	/**
	 * Konstruktor.
	 * @param expr Ausdruck, der zurückgegeben werden soll. 
	 */
	public ReturnStatement(Expression returnExpression) {
		this.returnExpression = returnExpression;
	}
	
	@Override
	public void contextAnalysis(Declarations declarations) throws CompileException {
		ResolvableIdentifier methodend = new ResolvableIdentifier("_methodend", null);
		declarations.resolveVarOrMethod(methodend);
		endLabel = ((VarDeclaration)methodend.getDeclaration()).getType().getName();
		if (returnExpression != null) {
			ResolvableIdentifier result = new ResolvableIdentifier("_result", returnExpression.getPosition());
			declarations.resolveVarOrMethod(result);
			
			returnExpression.contextAnalysis(declarations);
			returnExpression = returnExpression.box(declarations);
			
			
			returnAssignment = new Assignment(new VarOrCall(result,new LinkedList<Expression>()), returnExpression);
			
			if(!(((ClassDeclaration)((VarDeclaration)result.getDeclaration()).getType().getDeclaration())).isA(returnExpression.getType())) {
				throw new CompileException("Returntyp nicht richtig", returnExpression.getPosition());
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

	@Override
	public void generateCode(CodeStream code) {
		if (returnExpression != null) {
			returnAssignment.generateCode(code);
		}
		code.println("MRI R0, "+endLabel);
	}
	
	@Override
	public boolean returns() {
		return true;
	}
	

}

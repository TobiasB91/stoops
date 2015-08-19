package oopsc.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import oopsc.CompileException;
import oopsc.Program;
import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.MethodDeclaration;
import oopsc.declarations.VarDeclaration;
import oopsc.expressions.AccessExpression;
import oopsc.expressions.BinaryExpression;
import oopsc.expressions.Expression;
import oopsc.expressions.LiteralExpression;
import oopsc.expressions.NewExpression;
import oopsc.expressions.UnaryExpression;
import oopsc.expressions.VarOrCall;
import oopsc.statements.Assignment;
import oopsc.statements.CallStatement;
import oopsc.statements.IfStatement;
import oopsc.statements.ReadStatement;
import oopsc.statements.Statement;
import oopsc.statements.WhileStatement;
import oopsc.statements.WriteStatement;

/**
 * Die Klasse realisiert die syntaktische Analyse für die folgende Grammatik. 
 * Terminale stehen dabei in Hochkommata oder sind groß geschrieben:
 * <pre>
 * program      ::= { classdecl }
 *
 * classdecl    ::= CLASS identifier IS
 *                  { memberdecl } 
 *                  END CLASS
 *
 * memberdecl   ::= vardecl ';'
 *                | METHOD identifier IS methodbody
 * 
 * vardecl      ::= identifier { ',' identifier } ':' identifier
 * 
 * methodbody   ::= { vardecl ';' }
 *                  BEGIN statements
 *                  END METHOD
 * 
 * statements   ::= { statement }
 * 
 * statement    ::= READ memberaccess ';'
 *                | WRITE expression ';'
 *                | IF predicate 
 *                  THEN statements 
 *                  {ELSEIF predicate THEN statements }
 *                  [ELSE statements]
 *                  END IF
 *                | WHILE predicate 
 *                  DO statements 
 *                  END WHILE
 *                | memberaccess [ ':=' predicate ] ';'
 * 
 * 
 * predicate ::= conjunction { OR conjunction}
 * 
 * conjunction ::= relation { AND relation }
 * 
 * relation     ::= expression [ ( '=' | '#' | '<' | '>' | '<=' | '>=' ) expression ]
 * 
 * expression   ::= term { ( '+' | '-' ) term }
 * 
 * term         ::= factor { ( '*' | '/' | MOD ) factor }
 * 
 * factor       ::= '-' factor
 * 				  | NOT factor
 *                | memberaccess
 * 
 * memberaccess ::= literal { '.' varorcall }
 * 
 * literal    ::= number
 *                | character
 *                | NULL
 *                | SELF
 *                | NEW identifier
 *                | '(' predicate ')'
 *                | varorcall
 *                | TRUE 
 *                | FALSE
 * 
 * varorcall    ::= identifier
 * </pre>
 * Daraus wird der Syntaxbaum aufgebaut, dessen Wurzel die Klasse
 * {@link Program Program} ist.
 */
public class SyntaxAnalysis {
    /** Die lexikalische Analyse, die den Symbolstrom erzeugt. */
    private final LexicalAnalysis lexer;
    
    /**
     * Die Methode erzeugt einen "Unerwartetes Symbol"-Fehler.
     * @throws CompileException Die entsprechende Fehlermeldung.
     */
    private void unexpectedSymbol() throws CompileException {
        throw new CompileException("Unerwartetes Symbol " + lexer.getSymbol().getId().toString(), 
                lexer.getSymbol().getPosition());
    }
    
    /**
     * Die Methode überprüft, ob das aktuelle Symbol das erwartete ist. Ist dem so,
     * wird das nächste Symbol gelesen, ansonsten wird eine Fehlermeldung erzeugt.
     * @param id Das erwartete Symbol.
     * @throws CompileException Ein unerwartetes Symbol wurde gelesen.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void expectSymbol(Symbol.Id id) throws CompileException, IOException {
        if (id != lexer.getSymbol().getId()) {
            unexpectedSymbol();
        }
        lexer.nextSymbol();
    }
    
    /**
     * Die Methode überprüft, ob das aktuelle Symbol ein Bezeichner ist. Ist dem so,
     * wird er zurückgeliefert, ansonsten wird eine Fehlermeldung erzeugt.
     * @throws CompileException Ein unerwartetes Symbol wurde gelesen.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Identifier expectIdent() throws CompileException, IOException {
        if (lexer.getSymbol().getId() != Symbol.Id.IDENT) {
            unexpectedSymbol();
        }
        Identifier i = new Identifier(lexer.getSymbol().getIdent(), lexer.getSymbol().getPosition());
        lexer.nextSymbol();
        return i;
    }
    
    /**
     * Die Methode überprüft, ob das aktuelle Symbol ein Bezeichner ist. Ist dem so,
     * wird er in Form eines Bezeichners mit noch aufzulösender Vereinbarung
     * zurückgeliefert, ansonsten wird eine Fehlermeldung erzeugt.
     * @throws CompileException Ein unerwartetes Symbol wurde gelesen.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private ResolvableIdentifier expectResolvableIdent() throws CompileException, IOException {
        if (lexer.getSymbol().getId() != Symbol.Id.IDENT) {
            unexpectedSymbol();
        }
        ResolvableIdentifier r = new ResolvableIdentifier(lexer.getSymbol().getIdent(), lexer.getSymbol().getPosition());
        lexer.nextSymbol();
        return r;
    }
    
    /**
     * Die Methode parsiert eine oder mehrere Klassendeklarationen entsprechend der oben angegebenen
     * Syntax und liefert diese zurück.
     * @return Die Klassendeklarationen.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private LinkedList<ClassDeclaration> classdecl() throws CompileException, IOException {
        LinkedList<ClassDeclaration> classdecls = new LinkedList<ClassDeclaration>();
    	while (lexer.getSymbol().getId() == Symbol.Id.CLASS) {
    		lexer.nextSymbol();
	        Identifier name = expectIdent();
	        expectSymbol(Symbol.Id.IS);
	        LinkedList<VarDeclaration> attributes = new LinkedList<VarDeclaration>();
	        LinkedList<MethodDeclaration> methods = new LinkedList<MethodDeclaration>();
	        while (lexer.getSymbol().getId() != Symbol.Id.END) {
	            memberdecl(attributes, methods);
	        }
	        lexer.nextSymbol();
	        expectSymbol(Symbol.Id.CLASS);
	        classdecls.add(new ClassDeclaration(name, attributes, methods));
    	}
        return classdecls;
    }
    
    /**
     * Die Methode parsiert die Deklaration eines Attributs bzw. einer Methode
     * entsprechend der oben angegebenen Syntax und hängt sie an eine von
     * zwei Listen an.
     * @param attributes Die Liste der Attributdeklarationen der aktuellen Klasse.
     * @param methods Die Liste der Methodendeklarationen der aktuellen Klasse.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void memberdecl(LinkedList<VarDeclaration> attributes, 
            LinkedList<MethodDeclaration> methods)
            throws CompileException, IOException {
        if (lexer.getSymbol().getId() == Symbol.Id.METHOD) {
            lexer.nextSymbol();
            Identifier name = expectIdent();
            expectSymbol(Symbol.Id.IS);
            LinkedList<VarDeclaration> vars = new LinkedList<VarDeclaration>();
            LinkedList<Statement> statements = new LinkedList<Statement>();
            Position end = methodbody(vars, statements);
            methods.add(new MethodDeclaration(name, vars, statements, end));
        } else {
            vardecl(attributes, true);
            expectSymbol(Symbol.Id.SEMICOLON);
        }
    }

    /**
     * Die Methode parsiert die Deklaration eines Attributs bzw. einer Variablen
     * entsprechend der oben angegebenen Syntax und hängt sie an eine Liste an.
     * @param vars Die Liste der Attributdeklarationen der aktuellen Klasse oder 
     *         der Variablen der aktuellen Methode.
     * @param isAttribute Ist die Variable ein Attribut?.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void vardecl(LinkedList<VarDeclaration> vars, boolean isAttribute) throws CompileException, IOException {
        LinkedList<Identifier> names = new LinkedList<Identifier>();
        names.add(expectIdent());
        while (lexer.getSymbol().getId() == Symbol.Id.COMMA) {
            lexer.nextSymbol();
            names.add(expectIdent());
        }
        expectSymbol(Symbol.Id.COLON);
        ResolvableIdentifier ident = expectResolvableIdent();
        for (Identifier name : names) {
            vars.add(new VarDeclaration(name, ident, isAttribute));
        }
    }
    
    /**
     * Die Methode parsiert die Deklaration eines Methodenrumpfes entsprechend der 
     * oben angegebenen Syntax. Lokale Variablendeklarationen und Anweisungen werden
     * an die entsprechenden Listen angehängt.
     * @param vars Die Liste der lokalen Variablendeklarationen der aktuellen Methode.
     * @param statements Die Liste der Anweisungen der aktuellen Methode.
     * @return Die Position von END METHOD im Quelltext.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Position methodbody(LinkedList<VarDeclaration> vars, LinkedList<Statement> statements) 
            throws CompileException, IOException {
        while (lexer.getSymbol().getId() != Symbol.Id.BEGIN) {
            vardecl(vars, false);
            expectSymbol(Symbol.Id.SEMICOLON);
        }
        lexer.nextSymbol();
        statements(statements);
        Position position = lexer.getSymbol().getPosition();
        expectSymbol(Symbol.Id.END);
        expectSymbol(Symbol.Id.METHOD);
        return position;
    }
    
    /**
     * Die Methode parsiert eine Folge von Anweisungen entsprechend der 
     * oben angegebenen Syntax und hängt sie an eine Liste an.
     * @param statements Die Liste der Anweisungen.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void statements(LinkedList<Statement> statements) throws CompileException, IOException {
        while (lexer.getSymbol().getId() != Symbol.Id.END && lexer.getSymbol().getId() != Symbol.Id.ELSE && lexer.getSymbol().getId() != Symbol.Id.ELSEIF ) {
            statement(statements);
        }
    }
    
    /**
     * Die Methode parsiert eine Anweisung entsprechend der oben angegebenen
     * Syntax und hängt sie an eine Liste an.
     * @param statements Die Liste der Anweisungen.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private void statement(LinkedList<Statement> statements) throws CompileException, IOException {
        switch (lexer.getSymbol().getId()) {
        case READ:
            lexer.nextSymbol();
            statements.add(new ReadStatement(memberAccess()));
            expectSymbol(Symbol.Id.SEMICOLON);
            break;
        case WRITE:
            lexer.nextSymbol();
            statements.add(new WriteStatement(expression()));
            expectSymbol(Symbol.Id.SEMICOLON);
            break;
        case ELSEIF:
        case IF: 
    		boolean isIf = lexer.getSymbol().getId() == Symbol.Id.IF;
        	lexer.nextSymbol();
            Expression ifCondition = predicate();
            expectSymbol(Symbol.Id.THEN);
            LinkedList<Statement> thenStatements = new LinkedList<Statement>();
            LinkedList<Statement> elseStatements = new LinkedList<Statement>();
            statements(thenStatements);	
            if(lexer.getSymbol().getId() == Symbol.Id.ELSEIF) {
            	statement(elseStatements);
            } else if(lexer.getSymbol().getId() == Symbol.Id.ELSE) {
            	lexer.nextSymbol();
            	statements(elseStatements);
            } 
            if(isIf) {
            	expectSymbol(Symbol.Id.END);
            	expectSymbol(Symbol.Id.IF);
            }
            statements.add(new IfStatement(ifCondition, thenStatements, elseStatements));
            break;	
        case WHILE:
            lexer.nextSymbol();
            Expression whileCondition = predicate();
            expectSymbol(Symbol.Id.DO);
            LinkedList<Statement> whileStatements = new LinkedList<Statement>();
            statements(whileStatements);
            expectSymbol(Symbol.Id.END);
            expectSymbol(Symbol.Id.WHILE);
            statements.add(new WhileStatement(whileCondition, whileStatements));
            break;
        default:
            Expression e = memberAccess();
            if (lexer.getSymbol().getId() == Symbol.Id.BECOMES) {
                lexer.nextSymbol();
                statements.add(new Assignment(e, predicate()));
            } else {
                statements.add(new CallStatement(e));
            }
            expectSymbol(Symbol.Id.SEMICOLON);
        }
    }
    
    /**
     * Die Methode parsiert eine Relation entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression relation() throws CompileException, IOException {
        Expression e = expression();
        switch (lexer.getSymbol().getId()) {
        case EQ:
        case NEQ:
        case GT:
        case GTEQ:
        case LT:
        case LTEQ:
            Symbol.Id operator = lexer.getSymbol().getId();
            lexer.nextSymbol();
            return new BinaryExpression(e, operator, expression());
        default:
            return e;
        }
    }
    
    /**
     * Die Methode parsiert ein Prädikat entsprechend der oben angegebenen 
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression predicate() throws CompileException, IOException {
    	Expression e = conjunction();
    	while (lexer.getSymbol().getId() == Symbol.Id.OR) {
    		Symbol.Id operator = lexer.getSymbol().getId();
    		lexer.nextSymbol();
    		e = new BinaryExpression(e, operator, conjunction());
    	}
    	return e;
    }
    
    /**
     * Die Methode parsiert eine Konjunktion entsprechend der oben angegebenen 
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression conjunction() throws CompileException, IOException {
    	Expression e = relation();
    	while (lexer.getSymbol().getId() == Symbol.Id.AND) {
    		Symbol.Id operator = lexer.getSymbol().getId();
    		lexer.nextSymbol();
    		e = new BinaryExpression(e, operator, relation());
    	}
    	return e;
    }
    
    

    /**
     * Die Methode parsiert einen Ausdruck entsprechend der oben angegebenen
     * Syntax und liefert ihn zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression expression() throws CompileException, IOException {
        Expression e = term();
        while (lexer.getSymbol().getId() == Symbol.Id.PLUS || lexer.getSymbol().getId() == Symbol.Id.MINUS) {
            Symbol.Id operator = lexer.getSymbol().getId();
            lexer.nextSymbol();
            e = new BinaryExpression(e, operator, term());
        }
        return e;
    }

    /**
     * Die Methode parsiert einen Term entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression term() throws CompileException, IOException {
        Expression e = factor();
        while (lexer.getSymbol().getId() == Symbol.Id.TIMES || lexer.getSymbol().getId() == Symbol.Id.DIV ||
                lexer.getSymbol().getId() == Symbol.Id.MOD) {
            Symbol.Id operator = lexer.getSymbol().getId();
            lexer.nextSymbol();
            e = new BinaryExpression(e, operator, factor());
        }
        return e;
    }

    /**
     * Die Methode parsiert einen Faktor entsprechend der oben angegebenen
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression factor() throws CompileException, IOException {
        switch (lexer.getSymbol().getId()) {
        case NOT:
        case MINUS:
            Symbol.Id operator = lexer.getSymbol().getId();
            Position position = lexer.getSymbol().getPosition();
            lexer.nextSymbol();
            return new UnaryExpression(operator, factor(), position);
        default:
            return memberAccess();
        }
    }
    
    /**
     * Die Methode parsiert den Zugriff auf ein Objektattribut bzw. eine 
     * Objektmethode entsprechend der oben angegebenen Syntax und liefert 
     * den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression memberAccess() throws CompileException, IOException {
        Expression e = literal();
        while (lexer.getSymbol().getId() == Symbol.Id.PERIOD) {
            lexer.nextSymbol();
            e = new AccessExpression(e, new VarOrCall(expectResolvableIdent()));
        }
        return e;
    }

    /**
     * Die Methode parsiert ein Literal, die Erzeugung eines Objekts, einen
     * geklammerten Ausdruck oder einen einzelnen Zugriff auf eine Variable,
     * ein Attribut oder eine Methode entsprechend der oben angegebenen 
     * Syntax und liefert den Ausdruck zurück.
     * @return Der Ausdruck.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    private Expression literal() throws CompileException, IOException {
        Expression e = null;
        switch (lexer.getSymbol().getId()) {
        case NUMBER:
            e = new LiteralExpression(lexer.getSymbol().getNumber(), ClassDeclaration.INT_TYPE, 
                    lexer.getSymbol().getPosition());
            lexer.nextSymbol();
            break;
        case TRUE:
        	e = new LiteralExpression(1, ClassDeclaration.BOOL_TYPE, lexer.getSymbol().getPosition());
        	lexer.nextSymbol();
        	break;
        case FALSE:
        	e = new LiteralExpression(0, ClassDeclaration.BOOL_TYPE, lexer.getSymbol().getPosition());
        	lexer.nextSymbol();
        	break;
        case NULL:
            e = new LiteralExpression(0, ClassDeclaration.NULL_TYPE, lexer.getSymbol().getPosition());
            lexer.nextSymbol();
            break;
        case SELF:
            e = new VarOrCall(new ResolvableIdentifier("_self", lexer.getSymbol().getPosition()));
            lexer.nextSymbol();
            break;
        case NEW:
            Position position = lexer.getSymbol().getPosition();
            lexer.nextSymbol();
            e = new NewExpression(expectResolvableIdent(), position);
            break;
        case LPAREN:
            lexer.nextSymbol();
            e = predicate();
            expectSymbol(Symbol.Id.RPAREN);
            break;
        case IDENT:
            e = new VarOrCall(expectResolvableIdent());
            break;
        default:
            unexpectedSymbol();
        }
        return e;
    }

    /**
     * Konstruktor.
     * @param fileName Der Name des Quelltexts.
     * @param printSymbols Die lexikalische Analyse gibt die erkannten
     *         Symbole auf der Konsole aus.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws FileNotFoundException Der Quelltext wurde nicht gefunden.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    public SyntaxAnalysis(String fileName, boolean printSymbols) 
            throws CompileException, FileNotFoundException, IOException {
        lexer = new LexicalAnalysis(fileName, printSymbols);
    }

    /**
     * Die Methode parsiert den Quelltext und liefert die Wurzel des 
     * Syntaxbaums zurück.
     * @throws CompileException Der Quelltext entspricht nicht der Syntax.
     * @throws IOException Ein Lesefehler ist aufgetreten.
     */
    public Program parse() throws CompileException, IOException {
        lexer.nextSymbol();
        Program p = new Program(classdecl());
        expectSymbol(Symbol.Id.EOF);
        return p;
    }
}

package oopsc;

import java.util.LinkedList;

import oopsc.declarations.ClassDeclaration;
import oopsc.declarations.Declarations;
import oopsc.expressions.AccessExpression;
import oopsc.expressions.Expression;
import oopsc.expressions.NewExpression;
import oopsc.expressions.VarOrCall;
import oopsc.parser.ResolvableIdentifier;
import oopsc.streams.CodeStream;
import oopsc.streams.TreeStream;

/**
 * Die Klasse repräsentiert den Syntaxbaum des gesamten Programms.
 * Sie ist der Einstiegspunkt für die Kontextanalyse und die
 * Synthese.
 */
public class Program {
    /** Die benutzerdefinierten Klassen. */
    private LinkedList<ClassDeclaration> classes;
   
    /**
     * Eine Ausdruck, der ein Objekt der Klasse Main erzeugt und dann darin die
     * Methode main aufruft. Entspricht NEW Main.main.
     */
    private Expression main = new AccessExpression(
            new NewExpression(new ResolvableIdentifier("Main", null), null),
            new VarOrCall(new ResolvableIdentifier("main", null), new LinkedList<Expression>()));
    
    /**
     * Konstruktor.
     * @param theClass Die benutzerdefinierte Klasse.
     */
    public Program(LinkedList<ClassDeclaration> classes) {
    	classes.add(ClassDeclaration.INT_CLASS);
    	classes.add(ClassDeclaration.BOOL_CLASS);
    	classes.add(ClassDeclaration.OBJECT_CLASS);
        this.classes = classes;
    }
    
    /**
     * Die Methode führt die Kontextanalyse für das Programm durch.
     * @throws CompileException Während der Kontextanylyse wurde ein Fehler
     *         gefunden.
     */
    void contextAnalysis() throws CompileException {
        Declarations declarations = new Declarations();
        
        // Neuen Deklarationsraum schaffen
        declarations.enter();
               
        
        // Benutzerdefinierte Klassen hinzufügen
        for(ClassDeclaration cls : classes) {
        	declarations.add(cls);
        }
        
        // Kontextanalyse für die Klassen durchführen
        for(ClassDeclaration cls : classes) {
        	cls.contextAnalysis(declarations);
        }
        
        //Kontextanalyse für die Methoden der Klassen durchführen
        for(ClassDeclaration cls : classes) {
        	cls.resolve();
        }

        
        // Abhängigkeiten für Startup-Code auflösen
        main = main.contextAnalysis(declarations);
        
        // Deklarationsraum verlassen
        declarations.leave();
        
    }
    
    /**
     * Die Methode gibt den Syntaxbaum des Programms aus.
     */
    void printTree() {
        TreeStream tree = new TreeStream(System.out, 4);
        for(ClassDeclaration cls : classes) {
        	cls.print(tree);
        }
    }
    
    /**
     * Die Methode generiert den Assembler-Code für das Programm. Sie geht 
     * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
     * @param code Der Strom, in den die Ausgabe erfolgt.
     */
    void generateCode(CodeStream code, int stackSize, int heapSize) {
        // Start-Code: Register initialisieren
        code.setNamespace("_init");
        code.println("MRI R1, 1 ; R1 ist immer 1");
        code.println("MRI R2, _stack ; R2 zeigt auf Stapel");
        code.println("MRI R4, _heap ; R4 zeigt auf die nächste freie Stelle auf dem Heap");
        
        // Ein Objekt der Klasse Main konstruieren und die Methode main aufrufen.
        main.generateCode(code);
        code.println("MRI R0, _end ; Programm beenden");
        
        // Generiere Code für benutzerdefinierte Klassen
        for(ClassDeclaration cls : classes) {
        	cls.generateCode(code);
        }
        
        // Speicher für Stapel und Heap reservieren
        code.println("_stack: ; Hier fängt der Stapel an");
        code.println("DAT " + stackSize + ", 0");
        code.println("_heap: ; Hier fängt der Heap an");
        code.println("DAT " + heapSize + ", 0");
        code.println("_end: ; Programmende");
    }
}

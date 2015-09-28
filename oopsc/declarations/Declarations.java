package oopsc.declarations;

import java.util.LinkedList;
import java.util.HashMap;

import oopsc.CompileException;
import oopsc.parser.ResolvableIdentifier;
import oopsc.parser.Symbol;

/**
 * Die Klasse repräsentiert alle an einer bestimmten Stelle im Programm gültigen Deklarationen.
 * Die Deklarationen werden dabei als sich überlagernde Sichtbarkeitsebenen dargestellt.
 * Die Klasse stellt Methoden zum Auflösen von Bezeichnern bereit.
 */
public class Declarations {
    /**
     * Die Ebenen mit allen Deklarationen. Deklarationen in später hinzugefügten Ebenen überdecken
     * Deklarationen in früher hinzugefügten Ebenen. Jede Ebene wird durch eine Hash-Tabelle
     * realisiert.
     */
    private LinkedList<HashMap<String, Declaration>> levels = new LinkedList<HashMap<String, Declaration>>();
    
    private ClassDeclaration callerClass;
    
    /**
     * Die Method erstellt eine Kopie dieses Objekts. Dabei werden die Ebenen nicht kopiert,
     * sondern auch von der Kopie weiter benutzt. Die umgebende Liste wird aber kopiert,
     * so dass sie in beiden Instanzen unabhängig voneinander verändert werden kann. 
     * @return Die Kopie dieses Objekts.
     */
    public Object clone() {
        Declarations d = new Declarations();
        for (HashMap<String, Declaration> l : levels) {
            d.levels.add(l);
        }
        d.setCallerClass(callerClass);
        return d;
    }
    
    /**
     * Erzeugt eine neue Deklarationsebene.
     */
    public void enter() {
        levels.addFirst(new HashMap<String, Declaration>());
    }
    
    /**
     * Verwirft die zuletzt erzeugte Deklarationsebene.
     */
    public void leave() {
        levels.removeFirst();
    }
    
    /**
     * Die Methode fügt eine neue Deklaration in die oberste Ebene ein.
     * Wenn dort bereits die Deklaration eines gleichlautenden Bezeichners
     * vorhanden war, wird ein Fehler erzeugt.
     * @param declaration Die neu einzufügende Deklaration.
     * @throws CompileException Dieser Bezeichner wurde bereits in dieser Ebene verwendet.
     */
    public void add(Declaration declaration) throws CompileException {
        if (levels.getFirst().get(declaration.getIdentifier().getName()) != null) {
            throw new CompileException("Doppelte Deklaration von " + declaration.getIdentifier().getName(), 
                    declaration.getIdentifier().getPosition());
        } else {
            levels.getFirst().put(declaration.getIdentifier().getName(), declaration);
        }
    }

    /**
     * Die Methode ordnet einen Bezeichner seiner Deklaration im Programm zu.
     * @param identifier Der Bezeichner, der aufgelöst werden soll.
     * @throws CompileException Die Deklaration des Bezeichners wurde nicht gefunden.
     */
    private void resolve(ResolvableIdentifier identifier, ClassDeclaration callerCls) throws CompileException {
        if (identifier.getDeclaration() == null) {
            for (HashMap<String, Declaration> l : levels) {
                identifier.setDeclaration(l.get(identifier.getName()));
                if (identifier.getDeclaration() != null) {
                	
                	if(this.callerClass != null && this.callerClass.getBaseType() != null) {
                		for(VarDeclaration attribute : ((ClassDeclaration) this.callerClass.getBaseType().getDeclaration()).getAttributes()) {
                			if(identifier.getName().equals(attribute.getIdentifier().getName())) {
                				this.callerClass = ((ClassDeclaration)this.callerClass.getBaseType().getDeclaration());
                				break;
                			}
                		}
                	}
                	
                
                	
                    if (identifier.getDeclaration().getAccessRight() == Symbol.Id.PUBLIC) {
                    	return;
                    } else if (identifier.getDeclaration().getAccessRight() == Symbol.Id.PRIVATE) {
                    	if(this.callerClass == callerCls) {
                    		return;
                    	} else {
                    		throw new CompileException("Unerlaubter Zugriff auf PRIVATE-Deklaration", identifier.getPosition());
                    	}
                    } else {
                    	if(callerCls.isA(this.callerClass)) {
                    		return;
                    	} else {
                    		throw new CompileException("Unerlaubter Zugriff auf PROTECTED-Deklaration", identifier.getPosition());
                    	}
                    }
                }
            }
            throw new CompileException("Fehlende Deklaration von " + identifier.getName(), 
                    identifier.getPosition());
        }
    }

    /**
     * Die Methode ordnet einen Typ seiner Deklaration im Programm zu.
     * @param type Der Typ, der aufgelöst werden soll.
     * @throws CompileException Die Deklaration des Typs wurde nicht gefunden.
     */
    public void resolveType(ResolvableIdentifier type) throws CompileException {
        resolve(type, null);
        if (!(type.getDeclaration() instanceof ClassDeclaration)) {
            throw new CompileException("Typ erwartet", type.getPosition());
        }
    }

    /**
     * Die Methode ordnet eine Variable, ein Attribut oder einen Methodenaufruf 
     * der zugehörigen Deklaration im Programm zu.
     * @param varOrMethod Die Variable, das Attribut oder der Methodenaufruf.
     * @throws CompileException Die Deklaration der Variable, des Attributs oder 
     *         des Methodenaufruf wurde nicht gefunden.
     */
    public void resolveVarOrMethod(ResolvableIdentifier varOrMethod, ClassDeclaration callerCls) throws CompileException {
        resolve(varOrMethod, callerCls);
        if (varOrMethod.getDeclaration() instanceof ClassDeclaration) {
            throw new CompileException("Variable oder Methode erwartet", varOrMethod.getPosition());
        }
    }

	/**
	 * @return the callerClass
	 */
	public ClassDeclaration getCallerClass() {
		return callerClass;
	}

	/**
	 * @param callerClass the callerClass to set
	 */
	public void setCallerClass(ClassDeclaration callerClass) {
		this.callerClass = callerClass;
	}
}

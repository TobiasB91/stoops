package oopsc.parser;

import java.util.LinkedList;
import oopsc.declarations.Declaration;

/**
 * Die Klasse repräsentiert einen Bezeichner, dessen Vereinbarung im Laufe
 * der Kontextanalyse ermittelt wird. Alle Bezeichner werden in einer Liste
 * vermerkt, damit man sie alle bei Bedarf ausgeben kann.
 */
public class ResolvableIdentifier extends Identifier {
    /** Dieses Klassenattribut ist eine Liste, die alle zuordenbaren Bezeichner enthält. */
    private static LinkedList<ResolvableIdentifier> identifiers = new LinkedList<ResolvableIdentifier>();

    /** Die Deklaration dieses Bezeichners. Solange sie unbekannt ist, ist dieses Attribut null. */
    private Declaration declaration;

    /**
     * Konstruktor.
     * @param name Der Name des Bezeichners.
     * @param position Die Quelltextstelle, an der der Bezeichner gelesen wurde.
     */
    public ResolvableIdentifier(String name, Position position) {
        super(name, position);
        identifiers.add(this);
    }
    
    /**
     * Setzt die Deklaration dieses Bezeichners. 
     * Sie darf nur einmal gesetzt werden.
     * @param declaration Die Deklaration dieses Bezeichners.
     */
    public void setDeclaration(Declaration declaration) {
        assert this.declaration == null;
        this.declaration = declaration;
    }
    
    /** 
     * Liefert die Deklaration dieses Bezeichners.
     * @return Die Deklaration oder null, wenn sie unbekannt ist.
     */
    public Declaration getDeclaration() {
        return declaration;
    }

    /**
     * Die Klassenmethode gibt alle zuordenbaren Bezeichner mit ihrer
     * Quelltextstelle und die Stelle ihrer Vereinbarung aus. Sollte
     * ein Eintrag nach der Kontextanalyse noch "unbekannt" sein,
     * enthält der Übersetzer einen Fehler.
     */
    public static void print() {
        for (ResolvableIdentifier r : identifiers) {
            if (r.getPosition() != null) { // Ignoriere vom Übersetzer nachträglich erzeugte Bezeichner
                System.out.print("Zeile " + r.getPosition().getLine() + ", Spalte " + 
                        r.getPosition().getColumn() + ": " + r.getName() + " ist ");
                if (r.declaration == null) {
                    System.out.println("unbekannt");
                } else if (r.declaration.getIdentifier().getPosition() == null) {
                    System.out.println("vordefiniert");
                } else {
                    System.out.println("definiert in Zeile " + 
                            r.declaration.getIdentifier().getPosition().getLine() + ", Spalte " +
                            r.declaration.getIdentifier().getPosition().getColumn());
                }
            }
        }
    }
}

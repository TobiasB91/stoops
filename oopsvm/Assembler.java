package oopsvm;

import java.io.*;
import java.util.HashMap;

/**
 * Die Klasse implementiert einen einfachen Assembler, der einen Quelltext
 * aus einer Datei liest und daraus ein Abbild des Hauptspeichers mit dem
 * übersetzen Programm generiert. Syntax einer Quelltextzeile:
 * <pre>
 * line   ::= instr [ ';' comment ]
 * instr  ::= '#' number
 *            label ':'
 *          | 'MRI' reg ',' addr
 *          | 'MRR' reg ',' reg
 *          | 'MRM' reg ',' '(' reg ')'
 *          | 'MMR' '(' reg ')' ',' reg
 *          | 'ADD' reg ',' reg
 *          | 'SUB' reg ',' reg
 *          | 'MUL' reg ',' reg
 *          | 'DIV' reg ',' reg
 *          | 'MOD' reg ',' reg
 *          | 'AND' reg ',' reg
 *          | 'OR' reg ',' reg
 *          | 'XOR' reg ',' reg
 *          | 'ISZ' reg ',' reg
 *          | 'ISP' reg ',' reg
 *          | 'ISN' reg ',' reg
 *          | 'JPC' reg ',' addr
 *          | 'SYS' addr ',' addr
 *          | 'DAT' number, addr
 * label  ::= ident
 * reg    ::= 'R'number
 * addr   ::= label
 *          | number
 * ident  ::= letter { letter | digit }
 * number ::= [ '-' ] digit { digit }
 * letter ::= 'A' .. 'Z' | 'a' .. 'z' | '_'
 * digit  :: = '0' .. '9'
 * </pre>
 */
public class Assembler {
    /** Die Zuordnung von textuellen Marken zu Speicheradressen. */
    private HashMap<String, Integer> labels;
  
    /** Der Datenstrom, aus dem der Quelltext gelesen wird. */
    private InputStreamReader reader;
    
    /** Das zuletzt gelesenen Zeichen. Wird durch Aufruf von {@link #nextChar() nextChar} aktualisiert. */
    private int c;

    /** Die aktuell gelesene Zeile wird in diesem Puffer für eine mögliche Ausgabe zwischengespeichert. */
    private String line;
  
    /** In dieses Feld wird im zweiten Durchgang das Programm generiert. Im ersten Durchgang ist es null. */
    private int[] output;

    /** Die Adresse der nächsten zu beschreibenden Speicherzelle. */
    private int writePos;
    
    /** Die Anfangsadressen aller Instruktionen. */
    private int[] instructionAddresses;
    
    /** Die Nummer der aktuellen Instruktion. */
    private int instructionCounter;
    
    /** Die Anfangsaddressen der OOPS Programmzeilen. */
    private int[] lineAddresses;
    
    /** Die Anzahl an Zeilen des OOPS-Programms. */
    private int lineCounter;

    /** Soll eine Bildschirmausgabe während des ersten Durchgangs erfolgen? */
    private boolean showFirst;

    /** Soll eine Bildschirmausgabe während des zweiten Durchgangs erfolgen? */
    private boolean showSecond;

    /** Soll eine Bildschirmausgabe während des aktuellen Durchgangs erfolgen? */
    private boolean showCode;
  
    /**
     * Die Methode erlaubt, zwischen dem ersten und dem zweiten Assemblierungslauf
     * zu unterscheiden.
     * @return Ist der aktuelle Assemblierungsdurchgang der erste?
     */
    private boolean isFirstPass() {
        return output == null;
    }
    
    /**
     * Die Methode liest das nächste Zeichen aus der Eingabedatei.
     * Es wird im Attribut {@link #c c} bereitgestellt.
     * Wenn während der Assemblierung eine Ausgabe erfolgen soll, werden
     * zusätzlich die eingelesenen Zeichen im Attribut {@link #line line}
     * gesammelt und jeweils beim Lesen eines Zeilenendes ausgegeben.
     * Das ermöglicht es, an anderer Stelle der Ausgabe noch den erzeugten 
     * Code voranzustellen.
     * @throws IOException Die Ausnahme wird bei Leseproblemen der Datei erzeugt.
     */
    private void nextChar() throws IOException {
        if (c == '\n') {
            System.out.print(line);
            line = "";
        }
        c = reader.read();
        if (showCode) {
            if (c != -1) {
                line += (char) c;
            }
        }
    }
  
    /**
     * Die Methode liest ein Token ein. Dies sind ",", ":", "(", ")"
     * sowie Zeichenketten, die den nicht-Terminalen <i>ident</i> und <i>number</i>
     * aus der oben angegebenen Grammatik entsprechen. Alle Zeichen ab einem Semikolon
     * werden bis zum Zeilenende ignoriert, d.h. als Kommentar behandelt.
     * @return Das Token als Zeichenkette.
     * @throws IOException Die Ausnahme wird bei Leseproblemen der Datei erzeugt.
     * @throws Exception Ein ungültiges Zeichen wurde gelesen.
     */
    private String readToken() throws IOException, Exception {
        while (c != -1) {
            while (c != -1 && Character.isWhitespace((char) c)) {
                nextChar();
            }
        
            switch (c) {
            case -1: // Dateiende
                break;
            case ',':
            case ':':
            case '(':
            case ')':
            case '#':
                String token = "" + (char) c;
                nextChar();
                return token;
                
            case ';': // Kommentar: ignorieren bis Zeilenende
                while (c != -1 && c != '\n') {
                     nextChar();
                }
                break;
                
            default: // number oder ident
                if (c == '-' || Character.isDigit((char) c)) {
                    String number = "" + (char) c;
                    nextChar();
                    while (c != -1 && Character.isDigit((char) c)) {
                        number += (char) c;
                        nextChar();
                    }
                    if (number.equals("-")) {
                        throw new Exception("Zahl muss mindestens eine Ziffer haben: -");
                    }
                    return number;
                } else if (c == '_' || Character.isLetter((char) c)) {
                    String identifier = "" + (char) c;
                    nextChar();
                    while (c != -1 && (c == '_' || Character.isLetterOrDigit((char) c))) {
                        identifier = identifier + (char) c;
                        nextChar();
                    }
                    return identifier;
                } else {
                    throw new Exception("Unerwartetes Zeichen: " + (char) c + " (" + c + ")");
                }
            }
        }
        return ""; // Dateiende
    }
   
    /**
     * Die Methode wandelt einen Parameter in eine Zahl um.
     * Dabei gibt es drei Fälle: Wenn der Parameter ein Register sein soll,
     * wird das "R" entfernt und die Zahl dahinter zurückgeliefert. Soll
     * der Parameter kein Register sein und die Zeichenkette enthält eine
     * Zahl, so wird diese direkt zurückgegeben. Enthält die Zeichenkette
     * hingegen einen Bezeichner, so wird die zugeordnete Adresse aus der
     * Tabelle der definierten Marken entnommen. Das passiert nur im 2.
     * Assemblierungslauf, im ersten wird stattdessen 0 zurückgeliefert.
     * @param word Der Parameter als Zeichenkette.
     * @param register Soll der Parameter ein Register sein?
     * @return Die dem Parameter entsprechende Zahl.
     * @throws Exception Die Zeichenkette ist ungültig.
     */
    private int parseParam(String word, boolean register) throws Exception {
        if (word.equals("")) {
            throw new Exception("Parameter fehlt");
        } else if (register) {
            if (word.charAt(0) == 'R') {
                int num = Integer.parseInt(word.substring(1));
                if (!word.equals("R" + num)) {
                    throw new Exception("Falsches Register: " + word);
                }
                return num;
            } else {
                throw new Exception("Register erwartet: " + word);
            }
        } else if (Character.isLetter(word.charAt(0)) || word.charAt(0) == '_') {
            if (isFirstPass()) {
                return 0;
            } else {
                Integer address = labels.get(word);
                if (address == null) {
                    throw new Exception("Marke " + word + " nicht gefunden");
                } else {
                    return address;
                }
            }
        } else {
            return Integer.parseInt(word);
        }
    }

    /**
     * Die Methode schreibt den generierten Code in den Speicher.
     * Im ersten Assemblierungslauf wird kein Code geschrieben,
     * sondern nur mitgezählt, wie viel Platz verbraucht würde.
     * Dadurch lassen sich die Adressen der verwendeten Marken
     * bestimmen.
     * @param code Der Code, der in den Speicher geschrieben wird.
     */
    private void writeCode(int code) {
        if (!isFirstPass())
            output[writePos] = code;
        ++writePos;
    }
    
    /**
     * Die Methode erzeugt eine Tabelle, die die Adresse aller
     * Instruktionen enthält.
     * Im ersten Assemblierungslauf wird noch nichts geschrieben,
     * sondern nur mitgezählt, wie viel Platz verbraucht würde.
     */
    private void countInstructions() {
        if(!isFirstPass()) {
            instructionAddresses[instructionCounter] = writePos;
        }
        ++instructionCounter;
    }
  
    /**
     * Die Methode erzeugt eine Tabelle, die die Adresse aller
     * OOPS-Quelltextzeilen enthält.
     * Im ersten Assemblierungslauf wird noch nichts geschrieben,
     * sondern nur mitgezählt, wie viele Zeilen es gibt.
     */
    private void countLines(int line) {
        if (isFirstPass()) {
            lineCounter = Math.max(lineCounter, line);
        } else if (line > 0 && (lineAddresses[line - 1] == 0 || lineAddresses[line - 1] > writePos)) {
            lineAddresses[line - 1] = writePos;
        }
    }
  
    /**
     * Die Methode parsiert ein Zeile aus dem Quelltext und generiert den
     * entsprechenden Code. Die Syntax ist oben angegeben.
     * @throws IOException Die Ausnahme wird bei Leseproblemen der Datei erzeugt.
     * @throws Exception Beim Parsieren ist ein Fehler aufgetreten.
     */
    private void parseLine() throws IOException, Exception {
        String instruction = readToken();
        String word1 = readToken();
        String word2;

        if (instruction.equals("")) { // Dateiende
            return;
        } else if(instruction.equals("#")) { // Zeilennummer
            countLines(Integer.parseInt(word1));
        } else if(word1.equals(":")) { // Marke
            if (isFirstPass()) {
                String label = instruction;
                if (label.charAt(0) != '_' && !Character.isLetter(label.charAt(0)))
                    throw new Exception("Marke beginnt nicht mit einem Buchstaben: " + label + ":");
                else if (labels.get(label) == null) {
                    labels.put(label, writePos);
                } else {
                    throw new Exception("Marke " + label + " wurde mehrfach definiert");
                }
            } 
        } else { // Instruktion oder DAT
            try {
                Instruction inst = Instruction.valueOf(instruction);
                if (inst == Instruction.MMR) {
                    if (!word1.equals("(")) {
                        throw new Exception("Erster Parameter von MMR muss geklammert werden");
                    }
                    word1 = readToken();
                    word2 = readToken();
                    if (!word2.equals(")")) {
                        throw new Exception("Erster Parameter von MMR muss geklammert werden");
                    }
                }
                word2 = readToken();
                if (!word2.equals(",")) {
                    throw new Exception("Komma erwartet");
                }
                word2 = readToken();
                if (inst == Instruction.MRM) {
                    if (!word2.equals("(")) {
                        throw new Exception("Zweiter Parameter von MRM muss geklammert werden");
                    }
                    word2 = readToken();
                    String word3 = readToken();
                    if (!word3.equals(")")) {
                        throw new Exception("Zweiter Parameter von MRM muss geklammert werden");
                    }
                }
                int param1 = parseParam(word1, inst != Instruction.SYS);
                int param2 = parseParam(word2, inst != Instruction.MRI &&
                        inst != Instruction.JPC && inst != Instruction.SYS);
                countInstructions();
                if(inst == Instruction.MRI || inst == Instruction.JPC) {
                    if (showCode) {
                        System.out.format("%04x  %04x %04x  ", writePos, param1 << 4, param2 & 0xffff);
                    }
                    writeCode(inst.ordinal() << 8 | param1 << 4);
                    writeCode(param2);
                } else {
                    if (showCode) {
                        System.out.format("%04x  %04x       ", writePos, inst.ordinal() << 8 | param1 << 4 | param2);
                    }
                    writeCode(inst.ordinal() << 8 | param1 << 4 | param2);
                }
            } catch (IllegalArgumentException e) {
                if (instruction.equals("DAT")) { // DAT?
                    word2 = readToken();
                    if (!word2.equals(",")) {
                        throw new Exception("Komma erwartet");
                    }
                    word2 = readToken();
                    int param1 = parseParam(word1, false);
                    int param2 = parseParam(word2, false);
                    if (showCode) {
                        System.out.format("%04x  %04x %3s   ", 
                                writePos, param2 & 0xffff, param1 == 1 ? "" : "...");
                    }
                    if (Character.isLetter(word1.charAt(0))) {
                        throw new Exception("Erster Parameter von DAT kann keine Marke sein");
                    } else if (param1 <= 0) {
                        throw new Exception("Erster Parameter von DAT muss groesser als 0 sein");
                    } else if (param2 == 0) {
                        writePos += param1;
                    } else {
                        for (int j = 0; j < param1; ++j) {
                            writeCode(param2);
                        }
                    }
                } else { // ansonsten Fehler
                    throw new Exception("Unbekannte Anweisung " + instruction);
                }
            }
        }
    }

    /**
     * Die Methode führt einen Assemblierungsdurchgang aus.
     * Im ersten Durchgang wird dabei im Attribut {@link #writePos writePos}
     * lediglich die Größe des benötigten Speichers ermittelt. Im zweiten
     * Durchgang wird der Speicher im Attribut {@link #output output}
     * tatsächlich gefüllt.
     * @param fileName Der Name des Quelltexts.
     * @throws FileNotFoundException Der Quelltext existiert nicht.
     * @throws IOException Die Ausnahme wird bei Leseproblemen der Datei erzeugt.
     * @throws Exception Beim Assemblieren ist ein Fehler aufgetreten.
     */
    private void pass(String fileName) 
            throws FileNotFoundException, IOException, Exception {
        FileInputStream stream = new FileInputStream(fileName);
        try {
            reader = new InputStreamReader(stream, "UTF-8");
            line = "";
            nextChar();
            writePos = 0;
            instructionCounter = 0;
            while (c != -1) {
                parseLine();
            }
            if (showCode && !line.equals("")) {
                 System.out.println(line);
            }
        } finally {
            stream.close();
        }
    }

    /**
     * Konstruktor.
     * @param showFirst Soll eine Bildschirmausgabe während des ersten Durchgangs erfolgen?
     * @param showSecond Soll eine Bildschirmausgabe während des zweiten Durchgangs erfolgen?
     */
    public Assembler(boolean showFirst, boolean showSecond) {
        this.showFirst = showFirst;
        this.showSecond = showSecond;
    }
  
    /**
     * Die Methode wandelt einen Quelltext in Code um aus.
     * @param fileName Der Name des Quelltexts.
     * @return Der Speicher, der das übersetze Programm enthält.
     * @throws FileNotFoundException Der Quelltext existiert nicht.
     * @throws IOException Die Ausnahme wird bei Leseproblemen der Datei erzeugt.
     * @throws Exception Beim Assemblieren ist ein Fehler aufgetreten.
     */
    public int[] assemble(String fileName) 
            throws FileNotFoundException, IOException, Exception {
        labels = new HashMap<String, Integer>();
        output = null;
        showCode = showFirst;
        pass(fileName);
        output = new int[writePos];
        instructionAddresses = new int[instructionCounter];
        lineAddresses = new int[lineCounter];
        showCode = showSecond;
        pass(fileName);
        
        // Lücken bei den Startadressen von Zeilen so füllen, dass
        // immer auf die vorherige, eingetragene Zeile verwiesen wird.
        int last = 0;
        for (int i = 0; i < lineAddresses.length; ++i) {
            if (lineAddresses[i] == 0) {
                lineAddresses[i] = last;
            } else {
                last = lineAddresses[i];
            }
        }
        return output;
    }
    
    /**
     * Gibt die Adresse eine Marke zurück.
     * @param label Der Name der im Quelltext vorkommenden Marke, deren Adresse geliefert wird.
     * @return Die Adresse, die der Marke zugeordnet wurde oder null, falls die Marke unbekannt ist.
     */
    public Integer getLabelAddress(String label) {
        return labels.get(label);
    }
    
    /**
     * Liefert die Anfangsadressen aller Instruktionen. Datenbereiche sind nicht enthalten.
     * Dies dient üblicherweise der Visualisierung.
     * Diese Information steht erst nach dem Assemblieren zur Verfügung.
     * @return Ein Array mit so vielen Adressen, wie das Programm Instruktionen hat.
     */
    public int[] getInstructionAddresses() {
        return instructionAddresses;
    }
    
    /**
     * Liefert die Anfangsadressen aller OOPS-Quelltextzeile.
     * Dies dient üblicherweise der Visualisierung.
     * Diese Information steht erst nach dem Assemblieren zur Verfügung.
     * @return Ein Array mit so vielen Adressen, wie der OOPS-Quelltext Zeilen hat,
     *         wobei am Ende welche fehlen können, die keinen Code mehr erzeugt haben.
     */
    public int[] getLineAddresses() {
        return lineAddresses;
    }
}

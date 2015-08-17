package oopsdb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import oopsvm.MemoryListener;
import oopsvm.RegistersListener;

/**
 * Diese Klasse kapselt den Speicher für die Nutzung mit der Klasse JTable.
 */
class SourceModel extends Model implements MemoryListener, RegistersListener {
    private static final long serialVersionUID = 1L;

    /** Der Speicher. */
    private final ArrayList<String> lines = new ArrayList<String>();
    
    /** Die Anfangsadressen aller Instruktionen. */
    private final int[] lineAddresses;
    
    /** Alle Register. Eigentlich wird nur R0 benötigt. */
    private final int[] registers;
    
    /** Die Größe des Speichers. */
    private final int memorySize;
    
    /** Der Bereich, der Maschinen-Instruktionen zeigt. */
    private final AssemblerPane assemblerPane;
    
    /** Die aktuelle Zeilennummer. */
    private int currentLine;
    
    /**
     * Konstruktor.
     * @param memory Der Speicher.
     * @param instructionAddresses Die Anfangsadressen aller Anweisungen.
     * @param registers Alle Register.
     * @param memorySize Die Größe des Speichers.
     * @param assemblerPane Der Bereich, der den Assembler-Code zeigt.
     */
    public SourceModel(String fileName, int[] lineAddresses, int[] registers, int memorySize, 
            AssemblerPane assemblerPane)
            throws FileNotFoundException, IOException {
        this.lineAddresses = lineAddresses;
        this.registers = registers;
        this.memorySize = memorySize;
        this.assemblerPane = assemblerPane;
        FileInputStream stream = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            reader.close();
        }
    }
    
    /**
     * Liefert die Anzahl der Spalten zurück.
     * Es gibt nur die Quelltextspalte.
     * @return Die Anzahl der Spalten der Tabelle.
     */
    public int getColumnCount() {
        return 1;
    }
    
    /**
     * Liefert die Anzahl der Zeilen zurück.
     * Es gibt so viele Zeilen wie Quelltextzeilen.
     * @return Die Anzahl der Zeilen der Tabelle.
     */
    public int getRowCount() {
        return lines.size();
    }
    
    /**
     * Liefert den Spaltentitel für eine gegebene Spaltennummer.
     * Es wird kein Spaltentitel verwendet.
     * @param col Die Spaltennummer.
     * @return Der Spaltentitel.
     */
    public String getColumnName(int col) {
        return null;
    }
    
    /**
     * Liefert den Wert einer Tabellenzelle.
     * @param row Die Zeilennummer der Zelle.
     * @param col Die Spaltennummer der Zelle (immer 0).
     * @return Die Assember-Anweisung als Text.
     */
    public Object getValueAt(int row, int col) {
        return lines.get(row);
    }

    /**
     * Liefert zurück, ob eine Tabellezelle editiert werden darf.
     * Dies ist nicht der Fall.
     * @param row Die Zeilennummer der Zelle.
     * @param col Die Spaltennummer der Zelle.
     * @return Nein, Zellen sind nicht editierbar.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    /**
     * Der Inhalt einer Speicherzelle hat sich verändert.
     * Hier interessiert nur, dass hier gemeldet wird, wenn ein Haltepunkt
     * umgeschaltet wurde.
     * @param address Die Adresse der Speicherzelle.
     */
    public void memoryChanged(int address) {
        int line = getLineFromAddress(address);
        if (line > 0) {
            fireTableCellUpdated(line, 0);
        }
    }

    /**
     * Der Inhalt eines Registers hat sich verändert.
     * Ist das Register R0, müssen alte und neue Zelle aktualisiert werden. 
     * @param address Die Adresse der Speicherzelle.
     */
    public void registerChanged(int register) {
        if (register == 0) {
            int lastLine = currentLine;
            currentLine = getLineFromAddress(registers[0]);
            fireTableCellUpdated(lastLine, 0);
            fireTableCellUpdated(currentLine, 0);
        }
    }
    
    /**
     * Alles kann sich verändert haben. Das kann auch R0 sein.
     */
    public void refresh() {
        registerChanged(0);
    }
    
    /**
     * Bestimmt die Quelltextzeile zur Adresse einer Maschinen-Instruktion.
     * @param address Die Adresse.
     * @return Die Nummer der Quelltextzeile (0-basiert).
     */
    int getLineFromAddress(int address) {
        if (address < memorySize) {
            int line = Arrays.binarySearch(lineAddresses, address);
            if (line < 0) {
                line = -2 - line;
            }
            while (line > 0 && lineAddresses[line - 1] == lineAddresses[line]) {
                --line;
            }
            return line;
        } else {
            return -1;
        }
    }
    
    /**
     * Zeigt eine bestimmte Zeile die aktuelle Anweisung?
     * @param row Die Zeile, die abgefragt wird.
     * @return Enthält die Zeile die aktuelle Anweisung?
     */
    boolean isCurrent(int row) {
        return row != 0 && row == currentLine;
    }
    
    /**
     * Zeigt die erste Maschineninstruktion, die zu einer Quelltext-Zeile gehört.
     * @param line Die Nummer der Quelltext-Zeile (0-basiert).
     */
    void showLine(int line) {
        if (line >= 0 && line < lineAddresses.length) {
            assemblerPane.showAddress(lineAddresses[line]);
        }
    }
}

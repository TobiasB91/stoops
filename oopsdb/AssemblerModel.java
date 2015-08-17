package oopsdb;

import java.util.Arrays;
import oopsvm.Instruction;
import oopsvm.MemoryListener;
import oopsvm.RegistersListener;
import oopsvm.VirtualMachine;

/**
 * Diese Klasse kapselt den Speicher für die Nutzung mit der Klasse JTable.
 */
class AssemblerModel extends Model implements MemoryListener, RegistersListener {
    private static final long serialVersionUID = 1L;

    /** Der Speicher. */
    private final int[] memory;
    
    /** Die Anfangsadressen aller Anweisungen. */
    private final int[] instructionAddresses;
    
    /** Alle Register. Eigentlich wird nur R0 benötigt. */
    private final int[] registers;
    
    /** Der Index der aktuellen Programmanweisung in {@link #instructionAddresses instructionAddresses}. */
    private int currentIndex;
    
    /** Der Bereich, der den Quelltext zeigt. */
    private SourcePane sourcePane;
    
   /**
     * Konstruktor.
     * @param memory Der Speicher.
     * @param instructionAddresses Die Anfangsadressen aller Anweisungen.
     * @param registers Alle Register.
     */
    public AssemblerModel(int[] memory, int[] instructionAddresses, int[] registers) {
        this.memory = memory;
        this.instructionAddresses = instructionAddresses;
        this.registers = registers;
    }
    
    /**
     * Setzt den Bereich, der den Quelltext zeigt.
     * @param sourcePane Die Quelltextansicht.
     */
    void setSourcePane(SourcePane sourcePane) {
        this.sourcePane = sourcePane;
    }
    
    /**
     * Liefert die Anzahl der Spalten zurück.
     * Es gibt nur die Spalte der Assember-Anweisungen.
     * @return Die Anzahl der Spalten der Tabelle.
     */
    public int getColumnCount() {
        return 1;
    }
    
    /**
     * Liefert die Anzahl der Zeilen zurück.
     * Es gibt so viele Zeilen wie Anweisungen.
     * @return Die Anzahl der Zeilen der Tabelle.
     */
    public int getRowCount() {
        return instructionAddresses.length;
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
        int word = memory[instructionAddresses[row]];
        try {
            Instruction instruction = VirtualMachine.INSTRUCTIONS[word >> 8 & 0xff];
            int param1 = word >> 4 & 0x0f;
            int param2 = word & 0x0f;
            if (instruction == Instruction.MRI || instruction == Instruction.JPC) {
                param2 = memory[instructionAddresses[row] + 1];
            }
            return instruction.toString(param1, param2);
        } catch (ArrayIndexOutOfBoundsException e) {
            return String.format("??? (%04x)", word);
        }
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
     * Wenn diese zu einer Anweisung gehört, muss Darstellung aktualisiert werden.
     * @param address Die Adresse der Speicherzelle.
     */
    public void memoryChanged(int address) {
        int index = getInstructionFromAddress(address);
        if (index >= 0) {
            fireTableCellUpdated(index, 0);
        }
    }

    /**
     * Der Inhalt eines Registers hat sich verändert.
     * Ist das Register R0, müssen alte und neue Zelle aktualisiert werden. 
     * @param address Die Adresse der Speicherzelle.
     */
    public void registerChanged(int register) {
        if (register == 0) {
            int lastIndex = currentIndex;
            currentIndex = getInstructionFromAddress(registers[0]);
            if (lastIndex >= 0) {
                fireTableCellUpdated(lastIndex, 0);
            }
            if (currentIndex >= 0) {
                fireTableCellUpdated(currentIndex, 0);
            }
        }
    }
    
    /**
     * Alles kann sich verändert haben. Das kann auch R0 sein.
     */
    public void refresh() {
        super.refresh();
        registerChanged(0);
    }
    
    /**
     * Bestimmt die Nummer der Maschinen-Instruktion zu einer Adresse.
     * @param address Die Adresse.
     * @return Die Nummer der Instruktion. -1, wenn nicht gefunden.
     */
    int getInstructionFromAddress(int address) {
        if (address >= 0 && address <= instructionAddresses[instructionAddresses.length - 1] + 1) {
            int index = Arrays.binarySearch(instructionAddresses, address);
            if (index < 0) { // Wenn Fundstelle zwischen Anweisungen liegt,
                index = -2 - index; // aktualisiere die Anweisung davor.
            }
            return index;
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
        return row == currentIndex;
    }
    
    /**
     * Zeigt die Quelltext-Zeile, die zu einer Maschinen-Instruktion gehört.
     * @param instruction Die Nummer der Maschinen-Instruktion.
     */
    void showInstruction(int instruction) {
        if (sourcePane != null && instruction >= 0 && instruction < instructionAddresses.length) {
            sourcePane.showAddress(instructionAddresses[instruction]);
        }
    }
}

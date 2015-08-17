package oopsdb;

import oopsvm.MemoryListener;
import oopsvm.RegistersListener;

/**
 * Diese Klasse kapselt den Speicher für die Nutzung mit der Klasse JTable.
 */
class MemoryModel extends Model implements MemoryListener, RegistersListener {
    private static final long serialVersionUID = 1L;

    /** Der Speicher. */
    private final int[] memory;

    /** Der Registersatz. */
    private final int[] registers;
    
    /** Der alte Zustand des Registersatzes. */
    private final int[] prevRegisters;
    
    /**
     * Konstruktor.
     * @param memory Der Speicher.
     * @param registers Der Registersatz.
     */
    public MemoryModel(int[] memory, int[] registers) {
        this.memory = memory;
        this.registers = registers;
        prevRegisters = registers.clone();
    }
    
    /**
     * Liefert die Anzahl der Spalten zurück.
     * Es werden immer 16 Maschinenwörter pro Zeile angezeigt.
     * @return Die Anzahl der Spalten der Tabelle.
     */
    public int getColumnCount() {
        return 16;
    }
    
    /**
     * Liefert die Anzahl der Zeilen zurück.
     * @return Die Anzahl der Zeilen der Tabelle.
     */
    public int getRowCount() {
        return (memory.length + 15) / 16;
    }
    
    /**
     * Liefert den Spaltentitel für eine gegebene Spaltennummer.
     * @param col Die Spaltennummer.
     * @return Der Spaltentitel.
     */
    public String getColumnName(int col) {
        return String.format("%01x", col);
    }
    
    /**
     * Liefert den Wert einer Tabellenzelle.
     * @param row Die Zeilennummer der Zelle.
     * @param col Die Spaltennummer der Zelle.
     * @return Der Inhalt der entsprechenden Speicherzelle.
     */
    public Object getValueAt(int row, int col) {
        int address = row * 16 + col;
        if (address < memory.length) {
            return String.format("%04x", memory[address] & 0xffff);
        } else {
            return "";
        }
    }

    /**
     * Liefert zurück, ob eine Tabellezelle editiert werden darf.
     * Das ist nicht der Fall.
     * @param row Die Zeilennummer der Zelle.
     * @param col Die Spaltennummer der Zelle.
     * @return Nein, Zellen sind nicht editierbar.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    /**
     * Der Inhalt einer Speicherzelle hat sich verändert.
     * Der entsprechene Eintrag muss neu gezeichnet werden.
     * @param address Die Adresse der Speicherzelle.
     */
    public void memoryChanged(int address) {
        fireTableCellUpdated(address / 16, address % 16);
    }

    /**
     * Der Inhalt eines Registers hat sich verändert.
     * Die durch Register adressierten Speicherzellen werden farbig markiert,
     * weshalb Speicherzellen bei jeder Veränderung eines Registers neu
     * gezeichnet werden müssen. Zudem werden auch Stack und Heap farbig
     * unterlegt, weshalb ganze Zellbereiche bei der Veränderung von R2, R3 und R4
     * neu gezeichnet werden müssen.
     * @param address Die Adresse der Speicherzelle.
     */
    public void registerChanged(int register) {
        int address = prevRegisters[register];
        if (address >= 0 && address < memory.length) {
            fireTableCellUpdated(address / 16, address % 16);
        }
        address = registers[register];
        if (address >= 0 && address < memory.length) {
            fireTableCellUpdated(address / 16, address % 16);
        }
        if(register >= 2 && register <= 4 && prevRegisters[register] != 0) {
            for (address = Math.min(prevRegisters[register], registers[register]) + 1;
                    address < Math.max(prevRegisters[register], registers[register]); ++address) {
                fireTableCellUpdated(address / 16, address % 16);
            }
        }
        prevRegisters[register] = registers[register];
    }
}

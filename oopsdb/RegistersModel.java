package oopsdb;

import oopsvm.RegistersListener;

/**
 * Diese Klasse kapselt den Registersatz für die Nutzung mit der Klasse JTable.
 */
class RegistersModel extends Model implements RegistersListener {
    private static final long serialVersionUID = 1L;

    /** Der Registersatz. */
    private final int[] registers;
    
    /** Der Bereich, der den Speicher zeigt. */
    private final MemoryPane memoryPane;
    
    /**
     * Konstruktor
     * @param registers Der Registersatz.
     * @param memoryPane Der Bereich, der den Speicher zeigt.
     */
    public RegistersModel(int[] registers, MemoryPane memoryPane) {
        this.registers = registers;
        this.memoryPane = memoryPane;
    }
    
    /**
     * Liefert die Anzahl der Spalten zurück.
     * Jedes Register wird in einer Spalte angezeigt.
     * @return Die Anzahl der Spalten der Tabelle.
     */
    public int getColumnCount() {
        return registers.length;
    }
    
    /**
     * Liefert die Anzahl der Zeilen zurück.
     * Es gibt nur eine Zeile.
     * @return Die Anzahl der Zeilen der Tabelle.
     */
    public int getRowCount() {
        return 1;
    }
    
    /**
     * Liefert den Spaltentitel für eine gegebene Spaltennummer.
     * Es werden keine Spaltentitel verwendet.
     * @param col Die Spaltennummer.
     * @return Der Spaltentitel.
     */
    public String getColumnName(int col) {
        return null;
    }
    
    /**
     * Liefert den Wert eine Tabellenzelle.
     * @param row Die Zeilennummer der Zelle (Immer 0).
     * @param col Die Spaltennummer der Zelle. Ist die Nummer des Registers.
     * @return Den Namen und Inhalt eines Registers.
     */
    public Object getValueAt(int row, int col) {
        return String.format("R%d = %04x", col, registers[col] & 0xffff);
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
     * Der Inhalt eines Registers hat sich verändert.
     * Dann muss es neu gezeichnet werden.
     * @param register Die Nummer des Registers.
     */
    public void registerChanged(int register) {
        fireTableCellUpdated(0, register);
    }
    
    /**
     * Die von einem Register adressierte Speicherzelle wird angezeigt.
     * @param register Die Nummer des Registers.
     */
    void showRegister(int register) {
        memoryPane.showAddress(registers[register]);
    }
}

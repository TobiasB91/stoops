package oopsdb;

import javax.swing.table.AbstractTableModel;
import oopsvm.RefreshListener;

/**
 * Die Basisklasse für alle Tabellenmodelle.
 */
public abstract class Model extends AbstractTableModel implements RefreshListener {
    private static final long serialVersionUID = 1L;

    /**
     * Die Anzeige muss vollständig erneuert werden.
     */
    public void refresh() {
        fireTableDataChanged();
    }
}

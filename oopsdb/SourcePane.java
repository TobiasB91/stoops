package oopsdb;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import oopsvm.VirtualMachine;

/**
 * Die Klasse erzeugt eine Ansicht des Quelltexts des Programms.
 * Die aktuelle Zeile wird immer rot dargestellt.
 * Zudem kann durch Doppelklicken die passende Maschineninstruktion angezeigt werden.
 * Ein Doppelklick auf einen Zeilentitel schaltet einen Haltepunkt um.
 */
public class SourcePane extends JScrollPane implements TableModelListener {
    private static final long serialVersionUID = 1L;

    /**
     * Die Klasse erzeugt die Zeilentitel für die Quelltext-Ansicht.
     */
    private class RowHeaders extends oopsdb.RowHeaders {
        private static final long serialVersionUID = 1L;

        /**
         * Konstruktor.
         * @param table Die Table mit dem eigentlichen Inhalt.
         * @param lineAddresses Die Anfangsadressen der Quelltext-Zeilen.
         * @param vm Die virtuelle Maschine, die die Haltepunkte verwaltet.
         */
        public RowHeaders(JTable table, final int[] lineAddresses, final VirtualMachine vm) {
            super(table);
            
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int line = getSelectedRow();
                        if (line < lineAddresses.length && lineAddresses[line] > 0 && 
                                (line == 0 || lineAddresses[line] != lineAddresses[line - 1])) {
                            int address = lineAddresses[line];
                            if (vm.hasBreakPoint(address)) {
                                vm.removeBreakPoint(address);
                            } else {
                                vm.addBreakPoint(address);
                            }
                        }
                    }
                }
            });            
        }
        
        /**
         * Liefert die Zeilennummer zu einer Zeile.
         * @param row Die Tabellenzeile.
         * @param column Die Tabellenspalte (immer 0).
         */
        public Object getValueAt(int row, int column) {
            return "" + (row + 1);
        }
    }
    
    /** Die Tabelle mit den Anweisungen. */
    private final JTable table;

    /**
     * Konstruktor.
     * @param model Das Tabellenmodell mit Quelltextzeilen.
     */
    SourcePane(SourceModel model, final int[] lineAddresses, final VirtualMachine vm) {
        table = new JTable(model);
        getViewport().add(table);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        table.setShowGrid(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                 Component cell = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                 if (((SourceModel) table.getModel()).isCurrent(row)) {
                     cell.setForeground(RegistersPane.COLORS[0]);
                 } else {
                     cell.setForeground(Color.BLACK);
                 }

                 if (row < lineAddresses.length && lineAddresses[row] > 0 && 
                         (row == 0 || lineAddresses[row] != lineAddresses[row - 1]) &&
                     vm.hasBreakPoint(lineAddresses[row])) {
                     cell.setBackground(AssemblerPane.BREAKPOINT_COLOR);
                 } else {
                     cell.setBackground(Color.WHITE);
                 }

                 return cell;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);

        model.addTableModelListener(this);       
         
        // Zweite Tabelle für die Zeilentitel
        JTable rowHeaders = new RowHeaders(table, lineAddresses, vm);
        
        setRowHeaderView(rowHeaders);
        setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeaders.getTableHeader());

        // Gewünschte Breite so setzen, dass alle Spalten angezeigt werden
        setPreferredSize(new Dimension(
                (int) (rowHeaders.getPreferredSize().getWidth() +
                table.getPreferredSize().getWidth() +
                getVerticalScrollBar().getPreferredSize().getWidth()),
                (int) getPreferredSize().getHeight()));
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int line = table.getSelectedRow();
                    ((SourceModel) table.getModel()).showLine(line);
                }
            }
        });
    }
    
    /**
     * Die Methode sorgt dafür, dass die aktuelle Anweisung immer zu sehen ist
     * @param e Ein Tabellen-Ereignis. Es wird auf das Ereignis reagiert, das zum
     *         Neuzeichner der aktuellen Anweisung auffordert.
     */
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE && e.getFirstRow() == e.getLastRow() &&
                ((SourceModel) table.getModel()).isCurrent(e.getFirstRow())) {
            table.scrollRectToVisible(new Rectangle(table.getCellRect(e.getFirstRow(), 0, true)));
        }
    }
    
    /**
     * Zeige eine bestimmte Speicherzelle an.
     * @param address Die Adresse der anzuzeigenden Speicherzelle.
     */
    void showAddress(int address) {
        int line = ((SourceModel) table.getModel()).getLineFromAddress(address);
        if (line > 0) {
            table.requestFocusInWindow();
            table.scrollRectToVisible(new Rectangle(table.getCellRect(line, 0, true)));
            table.changeSelection(line, 0, false, false);
        }
    }
}

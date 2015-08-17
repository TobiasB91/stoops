package oopsdb;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import oopsvm.VirtualMachine;

/**
 * Die Klasse erzeugt eine Ansicht der Assembler-Anweisungen des Programms.
 * Die aktuelle Anweisung wird immer rot dargestellt.
 * Zudem kann durch Doppelklicken die passende Quelltextzeile angezeigt werden.
 * Ein Doppelklick auf einen Zeilentitel schaltet einen Haltepunkt um.
 */
public class AssemblerPane extends JScrollPane implements TableModelListener {
    private static final long serialVersionUID = 1L;

    /** Farbe zum Unterlegen der Zeilen mit Haltepunkten. */
    static final Color BREAKPOINT_COLOR = new Color(
            RegistersPane.COLORS[0].getRed() / 8 + 224,
            RegistersPane.COLORS[0].getGreen() / 8 + 224,
            RegistersPane.COLORS[0].getBlue() / 8 + 224);
    
    /**
     * Die Klasse erzeugt die Zeilentitel für die Assembler-Ansicht.
     */
    private class RowHeaders extends oopsdb.RowHeaders {
        private static final long serialVersionUID = 1L;
        private final int[] instructionAddresses;
 
        /**
         * Eine Hilfsklasse zum Zeichnen der Zeilentitel.
         * @param table Die Table mit dem eigentlichen Inhalt.
         * @param instructionAddresses Die Anfangsadressen der Maschinen-Instruktionen.
         * @param vm Die virtuelle Maschine, die die Haltepunkte verwaltet.
         */
        public RowHeaders(JTable table, final int[] instructionAddresses, final VirtualMachine vm) {
            super(table);
            this.instructionAddresses = instructionAddresses;
            
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int address = instructionAddresses[getSelectedRow()];
                        if (vm.hasBreakPoint(address)) {
                            vm.removeBreakPoint(address);
                        } else {
                            vm.addBreakPoint(address);
                        }
                    }
                }
            });            
        }

        /**
         * Liefert die Speicheraddresse zu einer Instruktion.
         * @param row Die Tabellenzeile und Nummer der Instruktion
         * @param column Die Tabellenspalte (immer 0).
         */
        public Object getValueAt(int row, int column) {
            return String.format("%04x", instructionAddresses[row]);
        }
    }
    
    /** Die Tabelle mit den Anweisungen. */
    private final JTable table;

    /**
     * Konstruktor.
     * @param model Das Tabellenmodell mit den Anweisungen.
     * @param instructionAddresses Die Anfangsadressen aller Anweisung.
     * @param vm Die virtuelle Maschine, in der Haltepunkte gesetzt werden können.
     */
    AssemblerPane(AssemblerModel model, final int[] instructionAddresses, final VirtualMachine vm) {
        table = new JTable(model);
        getViewport().add(table);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        table.setShowGrid(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                 Component cell = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                 
                 if (((AssemblerModel) table.getModel()).isCurrent(row)) {
                     cell.setForeground(RegistersPane.COLORS[0]);
                 } else {
                     cell.setForeground(Color.BLACK);
                 }

                 if (vm.hasBreakPoint(instructionAddresses[row])) {
                     cell.setBackground(BREAKPOINT_COLOR);
                 } else {
                     cell.setBackground(Color.WHITE);
                 }

                 return cell;
            }
        };

        table.setDefaultRenderer(Object.class, renderer);
        model.addTableModelListener(this);
        
         // Breite der Spalten passend setzen
        Component comp = table.getCellRenderer(0, 0).getTableCellRendererComponent(table, "MRI R0, 0000 ", false, false, 0, 0);
        table.getColumnModel().getColumn(0).setPreferredWidth(comp.getPreferredSize().width);
         
        // Zweite Tabelle für die Zeilentitel
        JTable rowHeaders = new RowHeaders(table, instructionAddresses, vm);

        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
                    int instruction = table.getSelectedRow();
                    ((AssemblerModel) table.getModel()).showInstruction(instruction);
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
                ((AssemblerModel) table.getModel()).isCurrent(e.getFirstRow())) {
            table.scrollRectToVisible(new Rectangle(table.getCellRect(e.getFirstRow(), 0, true)));
        }
    }
    
    /**
     * Zeige eine bestimmte Speicherzelle an.
     * @param address Die Adresse der anzuzeigenden Speicherzelle.
     */
    void showAddress(int address) {
        int index = ((AssemblerModel) table.getModel()).getInstructionFromAddress(address);
        if (index >= 0) {
            table.requestFocusInWindow();
            table.scrollRectToVisible(new Rectangle(table.getCellRect(index, 0, true)));
            table.changeSelection(index, 0, false, false);
        }
    }
}

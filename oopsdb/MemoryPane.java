package oopsdb;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Die Klasse erzeugt eine Ansicht des Speicherinhalts des Programms.
 * Speicherzellen, die von Registern adressiert werden, werden farbig
 * markiert.
 * Zudem kann durch Doppelklicken zu Adressen gesprungen werden.
 */
public class MemoryPane extends JScrollPane {
    private static final long serialVersionUID = 1L;

    /** Farbe zum Unterlegen des Bereichs im aktuellen Stackframe. */
    private static final Color FRAME_COLOR = new Color(
          RegistersPane.COLORS[2].getRed() / 8 + 224,
          RegistersPane.COLORS[2].getGreen() / 8 + 224,
          RegistersPane.COLORS[2].getBlue() / 8 + 224);

    /** Farbe zum Unterlegen des Bereichs vor dem aktuellen Stackframe. */
    private static final Color STACK_COLOR = new Color(
          RegistersPane.COLORS[3].getRed() / 8 + 224,
          RegistersPane.COLORS[3].getGreen() / 8 + 224,
          RegistersPane.COLORS[3].getBlue() / 8 + 224);

    /** Farbe zum Unterlegen des benutzten Heaps. */
    private static final Color HEAP_COLOR = new Color(
          RegistersPane.COLORS[4].getRed() / 8 + 224,
          RegistersPane.COLORS[4].getGreen() / 8 + 224,
          RegistersPane.COLORS[4].getBlue() / 8 + 224);

    /**
     * Eine Hilfsklasse, die dafür sorgt, dass Bereiche des Speichers farbig 
     * dargestellt werden.
     */
    private class Renderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        /** Der Registersatz. */
        private int[] registers;
        
        /** Die Untergrenze des Stacks. */
        private int stack;

        /** Die Untergrenze des Heaps. */
        private int heap;

        /**
         * Kosntruktor
         * @param registers Der Registersatz.
         * @param stack Die Untergrenze des Stacks.
         * @param heap Die Untergrenze des Heaps.
         */
        Renderer(int[] registers, int stack, int heap) {
            this.registers = registers;
            this.stack = stack;
            this.heap = heap;
        }
        
        /**
         * Einfärben der Speicherzellen.
         */
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
             Component cell = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
                    
             int address = row * 16 + column;
             if (address >= stack && address <= registers[3]) {
                 cell.setBackground(STACK_COLOR);
             } else if (address >= stack && address <= registers[2]) {
                 cell.setBackground(FRAME_COLOR);
             } else if (address >= heap && address <= registers[4]) {
                 cell.setBackground(HEAP_COLOR);
             } else {
                 cell.setBackground(Color.WHITE);
             }
             
             for (int i = 0; i < registers.length; ++i) {
                 if (address == registers[i]) {
                     cell.setForeground(RegistersPane.COLORS[i]);
                     return cell;
                 }
             }
             cell.setForeground(Color.BLACK);
             return cell;
        }
    }
    
    /** Die Tablle, die den Speicher darstellt. */
    private final JTable table;
    
    /** Die Größe des Hauptspeichers. */
    private final int memorySize; 

    /**
     * Konstruktor.
     * @param model Das Tabllenmodell, dass den Speicher kapselt.
     * @param memory Der Speicher.
     * @param registers Der Registersatz.
     * @param stack Die Untergrenze des Stacks.
     * @param heap Die Untergrenze des Heaps.
     */
    MemoryPane(MemoryModel model, final int[] memory, int[] registers, int stack, int heap) {
        memorySize = memory.length;
        table = new JTable(model);
        getViewport().add(table);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        table.setShowGrid(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Renderer renderer = new Renderer(registers, stack, heap);
        renderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, renderer);
        
         // Breite der Spalten passend setzen
        Component comp = table.getCellRenderer(0, 0).getTableCellRendererComponent(table, "0000 ", false, false, 0, 0);
        for (int i = 0; i < table.getColumnCount(); ++i) {
            table.getColumnModel().getColumn(i).setPreferredWidth(comp.getPreferredSize().width);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        
        // Zweite Tabelle für die Zeilentitel
        JTable rowHeaders = new RowHeaders(table) {
            private static final long serialVersionUID = 1L;

            public Object getValueAt(int row, int column) {
                return String.format("%04x", row * 16);
            }
        };

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
                    int address = table.getSelectedRow() * 16 + table.getSelectedColumn();
                    showAddress(memory[address]);
                }
            }
        });
    }
    
    /**
     * Zeige eine bestimmte Speicherzelle an.
     * @param address Die Adresse der anzuzeigenden Speicherzelle.
     */
    void showAddress(int address) {
        if (address >= 0 && address < memorySize) {
            table.requestFocusInWindow();
            table.scrollRectToVisible(new Rectangle(table.getCellRect(address / 16, address % 16, true)));
            table.changeSelection(address / 16, address % 16, false, false);
        }
    }
}

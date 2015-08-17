package oopsdb;

import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Diese Klasse erzeugt Zeilentitel für eine Tabelle. Sie basiert im Wesentlichen auf der 
 * Implementierung von Rob Camick (http://www.camick.com/java/source/RowNumberTable.java).
 */
public class RowHeaders extends JTable
        implements ChangeListener, PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private final JTable main;

    public RowHeaders(JTable table) {
        main = table;
        main.addPropertyChangeListener(this);

        setFocusable(false);
        setAutoCreateColumnsFromModel(false);
        setSelectionModel(main.getSelectionModel());

        TableColumn column = new TableColumn();
        column.setHeaderValue(" ");
        addColumn(column);
        RowHeaderRenderer renderer = new RowHeaderRenderer();
        column.setCellRenderer(renderer);
        Component comp = renderer.getTableCellRendererComponent(table, "0000 ", false, false, 0, 0);
        getColumnModel().getColumn(0).setPreferredWidth(comp.getPreferredSize().width);
        setPreferredScrollableViewportSize(getPreferredSize());
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Component c = getParent();

        //  Keep scrolling of the row table in sync with the main table.
        if (c instanceof JViewport) {
            JViewport viewport = (JViewport)c;
            viewport.addChangeListener( this );
        }
    }

    /*
     *  Delegate method to main table
     */
    @Override
    public int getRowCount() {
        return main.getRowCount();
    }

    @Override
    public int getRowHeight(int row) {
        int rowHeight = main.getRowHeight(row);

        if (rowHeight != super.getRowHeight(row)) {
            super.setRowHeight(row, rowHeight);
        }

        return rowHeight;
    }

    /*
     *  Don't edit data in the main TableModel by mistake
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void stateChanged(ChangeEvent e) {
        //  Keep the scrolling of the row table in sync with main table
        JViewport viewport = (JViewport) e.getSource();
        JScrollPane scrollPane = (JScrollPane)viewport.getParent();
        scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    }

    public void propertyChange(PropertyChangeEvent e) {
        //  Keep the row table in sync with the main table
        if ("selectionModel".equals(e.getPropertyName())) {
            setSelectionModel( main.getSelectionModel() );
        }

        if ("rowHeight".equals(e.getPropertyName())) {
            repaint();
        }
    }

    /*
     *  Attempt to mimic the table header renderer
     */
    private static class RowHeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public RowHeaderRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();

                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (isSelected) {
                setFont( getFont().deriveFont(Font.BOLD) );
            }

            setText((value == null) ? "" : value.toString());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));

            return this;
        }
    }
}

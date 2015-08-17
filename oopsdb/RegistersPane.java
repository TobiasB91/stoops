package oopsdb;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Die Klasse erzeugt eine Ansicht des Registersatzes.
 * Jedes Register wird in einer anderen Farbe dargestellt.
 * Es kann durch Doppelklicken zu Adressen gesprungen, die von
 * Registern adressiert werden.
 */
public class RegistersPane extends JTable {    
    private static final long serialVersionUID = 1L;

    /** Die Farben, in denen die Register dargestellt werden. */
    static final Color[] COLORS = {
        Color.RED,
        Color.BLACK,
        new Color(255, 96, 0),
        new Color(192, 192, 0),
        new Color(0, 192, 0),
        new Color(0, 192, 255),
        Color.BLUE,
        new Color(192, 0, 255),
    };

    /**
     * Eine Hilfsklasse, die dafür sorgt, dass die Register farbig 
     * dargestellt werden.
     */
   private class Renderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
             Component cell = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
             cell.setForeground(COLORS[column]);
             return cell;
        }
    }
    
   /**
    * Konstrukor.
    * @param model Das Tabellenmodell, das den Registersatz kapselt. 
    */
    RegistersPane(RegistersModel model) {
        super(model);
        setTableHeader(null);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setShowGrid(false);
        setRowSelectionAllowed(false);
        Renderer renderer = new Renderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        setDefaultRenderer(Object.class, renderer);
        
         // Breite der Spalten passend setzen
        Component comp = getCellRenderer(0, 0).getTableCellRendererComponent(this, "0000 ", false, false, 0, 0);
        for (int i = 0; i < getColumnCount(); ++i) {
            getColumnModel().getColumn(i).setPreferredWidth(comp.getPreferredSize().width);
        }
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int register = ((JTable) e.getSource()).getSelectedColumn();
                    ((RegistersModel) getModel()).showRegister(register);
                }
            }
        });
    }
}

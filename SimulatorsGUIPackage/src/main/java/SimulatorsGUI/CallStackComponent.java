/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

package SimulatorsGUI;

import Hack.VMEmulator.CallStackGUI;
import HackGUI.Utilities;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

/**
 * This class represents the gui of a CallStack.
 */
public class CallStackComponent extends JPanel implements CallStackGUI {

    // The vector containing the method names of this call stack.
    private Vector<String> methodNames;

    // The table representing this callStack.
    private final JTable callStackTable;

    // The name label
    private final JLabel nameLbl = new JLabel();

    /**
     * Constructs a new CallStackComponent.
     */
    public CallStackComponent() {
        methodNames = new Vector<>();
        callStackTable = new JTable(new CallStackTableModel());
        jbInit();
    }

    /**
     * Sets the call stack with the given vector of method names.
     */
    @SuppressWarnings("unchecked")
    public void setContents(Vector<String> newMethodNames) {
        methodNames = (Vector<String>) newMethodNames.clone();
        callStackTable.revalidate();

        Rectangle r = callStackTable.getCellRect(methodNames.size() - 1, 0, true);
        callStackTable.scrollRectToVisible(r);
        repaint();
    }

    /**
     * Resets the contents of this CallStackComponent.
     */
    public void reset() {
        methodNames.removeAllElements();
        callStackTable.revalidate();
        callStackTable.clearSelection();
    }

    // Initializing this component.
    private void jbInit() {
        callStackTable.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                callStackTable.clearSelection();
            }
        });
        callStackTable.setTableHeader(null);
        callStackTable.setDefaultRenderer(callStackTable.getColumnClass(0), getCellRenderer());

        nameLbl.setText("Call Stack");
        nameLbl.setFont(Utilities.labelsFont);
        Utilities.fixToPreferredSize(nameLbl);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalStrut(3));
        this.add(nameLbl);
        this.add(Box.createVerticalStrut(3));
        this.add(new JScrollPane(callStackTable));
        this.setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * Returns the cell renderer of this component.
     */
    protected DefaultTableCellRenderer getCellRenderer() {
        return new callStackTableCellRenderer();
    }

    // An inner class representing the model of the CallStack table.
    class CallStackTableModel extends AbstractTableModel {

        /**
         * Returns the number of columns.
         */
        public int getColumnCount() {
            return 1;
        }

        /**
         * Returns the number of rows.
         */
        public int getRowCount() {
            return methodNames.size();
        }

        /**
         * Returns the names of the columns.
         */
        public String getColumnName(int col) {
            return "";
        }

        /**
         * Returns the value at a specific row and column.
         */
        public Object getValueAt(int row, int col) {
            return methodNames.elementAt(row);
        }

        /**
         * Returns true of this table cells are editable, false -
         * otherwise.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /**
     * The Cell Renderer for the call stack's table.
     */
    public class callStackTableCellRenderer extends DefaultTableCellRenderer {

        /**
         * Returns the cell renderer component.
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
                                                       int row, int column) {

            setForeground(row == (methodNames.size() - 1) ? Color.blue : null);
            setBackground(null);

            return super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        }
    }
}

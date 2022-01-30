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

package HackGUI;

import Hack.ComputerParts.TextFileEvent;
import Hack.ComputerParts.TextFileEventListener;
import Hack.ComputerParts.TextFileGUI;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * A component for displaying a text file.
 */
public class TextFileComponent extends JPanel implements TextFileGUI {

    // A vector containing the listeners to this component.
    private final Vector<TextFileEventListener> listeners;
    private final Vector<String> rowsVector;

    // The table.
    private final JTable textFileTable;

    // The scroll pane on which the table is placed.
    private final JScrollPane scrollPane;

    // The label with the name of this component
    private final JLabel nameLbl;

    // A set of indices of highlighted rows.
    private final Set<Integer> highlightedLines;

    // Indicates whether this component is enabled.
    private boolean isEnabled;

    /**
     * Constructs a new TextFileComponent
     */
    public TextFileComponent(String name) {
        listeners = new Vector<>();
        rowsVector = new Vector<>();
        // The model of the table
        nameLbl = new JLabel(name);
        TextFileTableModel model = new TextFileTableModel();
        textFileTable = new WideTable(model, 1000);
        textFileTable.setDefaultRenderer(textFileTable.getColumnClass(0), getCellRenderer());
        textFileTable.setTableHeader(null);
        scrollPane = new JScrollPane(textFileTable);
        highlightedLines = new HashSet<>();
        enableUserInput();
        jbInit();
    }

    /**
     * Enables user input to the component.
     */
    public void enableUserInput() {
        textFileTable.setRowSelectionAllowed(true);
        isEnabled = true;
    }

    public void hideSelect() {
        textFileTable.clearSelection();
    }

    public void select(int from, int to) {
        textFileTable.setRowSelectionInterval(from, to);
        Utilities.tableCenterScroll(this, textFileTable, from);
    }

    public void addHighlight(int index, boolean clear) {
        if (clear)
            highlightedLines.clear();

        highlightedLines.add(index);
        Utilities.tableCenterScroll(this, textFileTable, index);
        repaint();
    }

    public void clearHighlights() {
        highlightedLines.clear();
        repaint();
    }

    public String getLineAt(int index) {
        return rowsVector.elementAt(index);
    }

    public int getNumberOfLines() {
        return rowsVector.size();
    }

    /**
     * Returns the cell renderer of this component.
     */
    protected DefaultTableCellRenderer getCellRenderer() {
        return new TextFileCellRenderer();
    }


    public void addTextFileListener(TextFileEventListener listener) {
        listeners.addElement(listener);
    }


    public void notifyTextFileListeners(String row, int rowNum) {
        TextFileEvent event = new TextFileEvent(this, row, rowNum);
        listeners.forEach(l -> l.rowSelected(event));
    }

    public void addLine(String line) {
        rowsVector.addElement(line);
        textFileTable.revalidate();
        repaint();
        addHighlight(rowsVector.size() - 1, false);
    }

    public void setContents(String[] lines) {
        rowsVector.removeAllElements();
        for (String line : lines)
            rowsVector.addElement(line);

        textFileTable.revalidate();
        repaint();
    }

    public void setContents(String fileName) {
        rowsVector.removeAllElements();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null)
                rowsVector.addElement(line);
        } catch (IOException ignored) {
        }
        textFileTable.clearSelection();
        textFileTable.revalidate();
        repaint();
    }

    /**
     * Resets the content of this component.
     */
    public void reset() {
        highlightedLines.clear();
        rowsVector.removeAllElements();
        textFileTable.revalidate();
        textFileTable.clearSelection();
        repaint();
    }

    // The initialization of this component.
    private void jbInit() {
        textFileTable.setFont(Utilities.valueFont);
        textFileTable.setShowHorizontalLines(false);
        textFileTable.getSelectionModel().addListSelectionListener(e -> {
            if (!isEnabled || e.getValueIsAdjusting())
                return;

            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (!lsm.isSelectionEmpty()) {
                int selectedRow = lsm.getMinSelectionIndex();
                notifyTextFileListeners(rowsVector.elementAt(selectedRow), selectedRow);
            }
        });

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(scrollPane.getHorizontalScrollBar().getBlockIncrement());

        nameLbl.setFont(Utilities.labelsFont);
        Utilities.fixToPreferredSize(nameLbl);

        setBorder(BorderFactory.createEtchedBorder());

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalStrut(2));
        this.add(nameLbl);
        this.add(Box.createVerticalStrut(3));
        this.add(scrollPane);
    }

    // An inner class representing the model of the breakpoint table.
    class TextFileTableModel extends AbstractTableModel {

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
            //return rows.length;
            return rowsVector.size();
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
            //return rows[row];
            return rowsVector.elementAt(row);
        }

        /**
         * Returns true of this table cells are editable, false -
         * otherwise.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    // An inner class representing the cell renderer of the table.
    public class TextFileCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
                                                       int row, int column) {

            setForeground(null);
            setBackground(null);

            setRenderer(row);
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);

            return this;
        }

        public void setRenderer(int row) {
            final Color bg = highlightedLines.contains(row) ? Color.yellow : null;
            setBackground(bg);
            setForeground(null);
        }
    }
}

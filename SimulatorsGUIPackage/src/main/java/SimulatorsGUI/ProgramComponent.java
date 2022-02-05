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

import Hack.Events.ErrorEvent;
import Hack.Events.ErrorEventListener;
import Hack.Events.ProgramEvent;
import Hack.Events.ProgramEventListener;
import Hack.VMEmulator.VMEmulatorInstruction;
import Hack.VMEmulator.VMProgramGUI;
import Hack.VirtualMachine.HVMInstruction;
import HackGUI.MouseOverJButton;
import HackGUI.Utilities;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.Vector;

/**
 * This class represents the gui of a Program.
 */
public class ProgramComponent extends JPanel implements VMProgramGUI {

    private static final String CARD_TOOLS = "tools";
    private static final String CARD_MESSAGE = "message";

    // A vector containing the listeners to this object.
    private final Vector<ProgramEventListener> listeners;

    // A vector containing the error listeners to this object.
    private final Vector<ErrorEventListener> errorEventListeners;

    // The table representing this program
    private final JTable programTable;

    // The HVMInstructions of this program.
    private VMEmulatorInstruction[] instructions;

    // Creating the icon of the button.
    private static final ImageIcon browseIcon = new ImageIcon(Utilities.imagesDir + "open2.gif");

    // The file chooser window.
    //private FileChooserWindow fileChooser = new FileChooserWindow(null);
    private final JFileChooser fileChooser = new JFileChooser();

    // The current instruction index (yellow background).
    private int instructionIndex;

    // The text field with the message (for example "Loading...").
    private final JTextField messageTxt = new JTextField();

    // Creating the icon for the search button.
    private static final ImageIcon searchIcon = new ImageIcon(Utilities.imagesDir + "find.gif");

    // The window of searching a specific location in memory.
    private final SearchProgramWindow searchWindow;

    // Creating the icon for the clear button.
    private static final ImageIcon clearIcon = new ImageIcon(Utilities.imagesDir + "smallnew.gif");

    private final JPanel toolbarAlt = new JPanel(new CardLayout());

    /**
     * Constructs a new ProgramComponent.
     */
    public ProgramComponent() {
        listeners = new Vector<>();
        errorEventListeners = new Vector<>();
        instructions = new VMEmulatorInstruction[0];
        // The model of the table;
        programTable = new JTable(new ProgramTableModel());
        // The cell renderer of this table.
        programTable.setDefaultRenderer(programTable.getColumnClass(0), new ColoredTableCellRenderer());
        searchWindow = new SearchProgramWindow(programTable);

        jbInit();
    }

    /**
     * Registers the given ProgramEventListener as a listener to this GUI.
     */
    public void addProgramListener(ProgramEventListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Notifies all the ProgramEventListeners on a change in the program by creating a
     * ProgramEvent (with the new event type and program's directory name) and sending it
     * using the programChanged method to all the listeners.
     */
    public void notifyProgramListeners(byte eventType, String programFileName) {
        ProgramEvent event = new ProgramEvent(this, eventType, programFileName);
        listeners.forEach(l -> l.programChanged(event));
    }

    /**
     * Registers the given ErrorEventListener as a listener to this GUI.
     */
    public void addErrorListener(ErrorEventListener listener) {
        errorEventListeners.addElement(listener);
    }

    /**
     * Notifies all the ErrorEventListener on an error in this gui by
     * creating an ErrorEvent (with the error message) and sending it
     * using the errorOccurred method to all the listeners.
     */
    public void notifyErrorListeners(String errorMessage) {
        ErrorEvent event = new ErrorEvent(this, errorMessage);
        errorEventListeners.forEach(l -> l.errorOccurred(event));
    }

    /**
     * Sets the working directory with the given directory File.
     */
    public void setWorkingDir(File file) {
        fileChooser.setCurrentDirectory(file);
    }

    /**
     * Sets the contents of the gui with the first instructionsLength
     * instructions from the given array of instructions.
     */
    public synchronized void setContents(VMEmulatorInstruction[] newInstructions,
                                         int newInstructionsLength) {
        instructions = new VMEmulatorInstruction[newInstructionsLength];
        System.arraycopy(newInstructions, 0, instructions, 0, newInstructionsLength);
        programTable.revalidate();
        try {
            wait(100);
        } catch (InterruptedException ignored) {
        }
        searchWindow.setInstructions(instructions);
    }

    /**
     * Sets the current instruction with the given instruction index.
     */
    public void setCurrentInstruction(int instructionIndex) {
        this.instructionIndex = instructionIndex;
        Utilities.tableCenterScroll(this, programTable, instructionIndex);
    }

    /**
     * Resets the contents of this ProgramComponent.
     */
    public void reset() {
        instructions = new VMEmulatorInstruction[0];
        programTable.clearSelection();
        repaint();
    }

    /**
     * Opens the program file chooser for loading a program.
     */
    public void loadProgram() {
        int returnVal = fileChooser.showDialog(this, "Load Program");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            notifyProgramListeners(ProgramEvent.LOAD,
                                   fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Hides the displayed message.
     */
    public void hideMessage() {
        ((CardLayout) toolbarAlt.getLayout()).show(toolbarAlt, CARD_TOOLS);
        messageTxt.setText("");
    }

    /**
     * Displays the given message.
     */
    public void showMessage(String message) {
        messageTxt.setText(message);
        ((CardLayout) toolbarAlt.getLayout()).show(toolbarAlt, CARD_MESSAGE);
    }

    // Determines the width of each column in the table.
    private void determineColumnWidth() {
        programTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        programTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        programTable.getColumnModel().getColumn(2).setPreferredWidth(100);
    }

    /**
     * Sets the number of visible rows.
     */
    public void setVisibleRows(int num) {
        int tableHeight = num * programTable.getRowHeight();
        setPreferredSize(new Dimension(getTableWidth(), tableHeight + 30));
    }

    /**
     * Returns the width of the table.
     */
    public int getTableWidth() {
        return 225;
    }

    // Initialization of this component.
    private void jbInit() {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new VMFileFilter());
        programTable.getTableHeader().setReorderingAllowed(false);
        programTable.getTableHeader().setResizingAllowed(false);

        final MouseOverJButton browseButton = new MouseOverJButton();
        browseButton.setToolTipText("Load Program");
        browseButton.setIcon(browseIcon);
        browseButton.addActionListener(e1 -> loadProgram());
        Utilities.fixSize(browseButton, new Dimension(31, 24));

        messageTxt.setBackground(SystemColor.info);
        messageTxt.setEnabled(false);
        messageTxt.setFont(Utilities.labelsFont);
        messageTxt.setPreferredSize(new Dimension(70, 20));
        messageTxt.setDisabledTextColor(Color.red);
        messageTxt.setEditable(false);

        final MouseOverJButton searchButton = new MouseOverJButton();
        searchButton.setToolTipText("Search");
        searchButton.setIcon(searchIcon);
        searchButton.addActionListener(e -> searchWindow.showWindow());
        Utilities.fixSize(searchButton, new Dimension(31, 24));

        final JLabel nameLbl = new JLabel();
        nameLbl.setText("Program");
        nameLbl.setFont(Utilities.labelsFont);

        final MouseOverJButton clearButton = new MouseOverJButton();
        clearButton.addActionListener(e -> {
            Object[] options = {"Yes", "No"};
            int pressedButtonValue = JOptionPane.showOptionDialog(this.getParent(),
                                                                  "Are you sure you want to clear the program?",
                                                                  "Warning Message",
                                                                  JOptionPane.YES_NO_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE,
                                                                  null,
                                                                  options,
                                                                  options[1]);

            if (pressedButtonValue == JOptionPane.YES_OPTION)
                notifyProgramListeners(ProgramEvent.CLEAR, null);
        });
        clearButton.setIcon(clearIcon);
        clearButton.setToolTipText("Clear");
        Utilities.fixSize(clearButton, new Dimension(31, 24));

        final JPanel moreTools = new JPanel();
        moreTools.setLayout(new BoxLayout(moreTools, BoxLayout.X_AXIS));
        moreTools.add(Box.createHorizontalGlue());
        moreTools.add(browseButton);
        moreTools.add(clearButton);
        moreTools.add(searchButton);

        toolbarAlt.add(moreTools, CARD_TOOLS);
        toolbarAlt.add(messageTxt, CARD_MESSAGE);

        final JPanel tools = new JPanel();
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.add(nameLbl);
        tools.add(Box.createHorizontalStrut(3));
        tools.add(toolbarAlt);

        final JScrollPane scrollPane = new JScrollPane(programTable);

        this.setForeground(Color.lightGray);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(tools);
        this.add(scrollPane);
        this.setBorder(BorderFactory.createEtchedBorder());

        determineColumnWidth();
        programTable.setTableHeader(null);
    }


    // An inner class representing the model of the CallStack table.
    class ProgramTableModel extends AbstractTableModel {

        /**
         * Returns the number of columns.
         */
        public int getColumnCount() {
            return 3;
        }

        /**
         * Returns the number of rows.
         */
        public int getRowCount() {
            return instructions.length;
        }

        /**
         * Returns the names of the columns.
         */
        public String getColumnName(int col) {
            return null;
        }

        /**
         * Returns the value at a specific row and column.
         */
        public Object getValueAt(int row, int col) {
            String[] formattedString = instructions[row].getFormattedStrings();

            switch (col) {
                case 0:
                    short index = instructions[row].getIndexInFunction();
                    if (index >= 0)
                        return index;
                    else
                        return "";
                case 1:
                    return formattedString[0];
                case 2:
                    return formattedString[1] + " " + formattedString[2];
                default:
                    return null;
            }
        }

        /**
         * Returns true of this table cells are editable, false -
         * otherwise.
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    // An inner class which implements the cell renderer of the program table, giving
    // the feature of coloring the background of a specific cell.
    class ColoredTableCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent
                (JTable table, Object value, boolean selected, boolean focused, int row, int column) {
            setEnabled(table == null || table.isEnabled());
            setBackground(null);
            setForeground(null);

            if (column == 0) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            if (row == instructionIndex)
                setBackground(Color.yellow);
            else {
                HVMInstruction currentInstruction = instructions[row];
                String op = (currentInstruction.getFormattedStrings())[0];
                if (op.equals("function") && (column == 1 || column == 2))
                    setBackground(new Color(190, 171, 210));
            }

            super.getTableCellRendererComponent(table, value, selected, focused, row, column);

            return this;
        }
    }

    /**
     * Displays a confirmation window asking the user permission to
     * use built-in vm functions
     */
    @Override
    public boolean confirmBuiltInAccess() {
        final String message =
                "No implementation was found for some functions which are called in the VM code.\n" +
                        "The VM Emulator provides built-in implementations for the OS functions.\n" +
                        "If available, should this built-in implementation be used for functions which were not " +
                        "implemented in the VM code?";

        final int response = JOptionPane.showConfirmDialog(this.getParent(),
                                                           message,
                                                           "Confirmation Message",
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.QUESTION_MESSAGE);

        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Displays a notification window with the given message.
     */
    @Override
    public void notify(String message) {
        JOptionPane.showMessageDialog(this.getParent(),
                                      message,
                                      "Information Message",
                                      JOptionPane.INFORMATION_MESSAGE);
    }
}

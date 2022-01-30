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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * This class represents a search window for the use of MemoryComponent, ROMComponent,
 * and ProgramComponent. The user must enter the location in hexadecimal format.
 */
public class SearchMemoryWindow extends JFrame {

    // Creating the label in this window.
    private final JLabel instructionLbl = new JLabel();

    // Creating the text field in this window.
    private final JTextField rowNumber = new JTextField();

    // Creating buttons.
    private final JButton okButton = new JButton();
    private final JButton cancelButton = new JButton();

    // Creating icons
    private static final ImageIcon okIcon = new ImageIcon(Utilities.imagesDir + "ok.gif");
    private static final ImageIcon cancelIcon = new ImageIcon(Utilities.imagesDir + "cancel.gif");

    // The table to search in.
    private final JTable table;

    // The container panel of the table
    private final JPanel tableContainer;

    /**
     * Constructs a new SearchWindow.
     */
    public SearchMemoryWindow(JPanel tableContainer, JTable table) {
        super("Go to");
        this.table = table;
        this.tableContainer = tableContainer;
        jbInit();
    }

    /**
     * Shows the memory window.
     */
    public void showWindow() {
        setVisible(true);
        rowNumber.requestFocus();
    }

    // Initialization of this component.
    private void jbInit() {
        instructionLbl.setFont(Utilities.thinLabelsFont);
        instructionLbl.setText("Enter Address :");
        instructionLbl.setBounds(new Rectangle(9, 22, 132, 23));
        this.getContentPane().setLayout(null);
        rowNumber.setBounds(new Rectangle(102, 25, 158, 18));
        rowNumber.addActionListener(this::okButton_actionPerformed);
        okButton.setToolTipText("Ok");
        okButton.setIcon(okIcon);
        okButton.setBounds(new Rectangle(49, 60, 63, 44));
        okButton.addActionListener(this::okButton_actionPerformed);
        cancelButton.setBounds(new Rectangle(176, 60, 63, 44));
        cancelButton.addActionListener(e -> setVisible(false));
        cancelButton.setToolTipText("Cancel");
        cancelButton.setIcon(cancelIcon);
        this.getContentPane().add(instructionLbl, null);
        this.getContentPane().add(rowNumber, null);
        this.getContentPane().add(okButton, null);
        this.getContentPane().add(cancelButton, null);

        setSize(300, 150);
        setLocation(250, 250);
    }

    /**
     * Implementing the action of pressing the OK button.
     */
    private void okButton_actionPerformed(ActionEvent e) {
        try {
            int row = Format.translateValueToShort(rowNumber.getText(), Format.DEC_FORMAT);
            table.setRowSelectionInterval(row, row);
            Utilities.tableCenterScroll(tableContainer, table, row);
            setVisible(false);
        } catch (IllegalArgumentException ignored) {
        }
    }
}

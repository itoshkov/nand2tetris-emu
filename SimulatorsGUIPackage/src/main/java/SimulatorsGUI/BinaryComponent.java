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

import HackGUI.*;
import Hack.Utilities.Conversions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.swing.border.*;

/**
 * This class represents a 16-bits binary number.
 */
public class BinaryComponent extends JPanel implements MouseListener, KeyListener {

    // An array containing all of the text fields.
    private final JTextField[] bits = new JTextField[16];

    // The value of this component in a String representation.
    private StringBuffer valueStr;

    // Creating buttons.
    private final JButton okButton = new JButton();
    private final JButton cancelButton = new JButton();

    // Creating icons.
    private final ImageIcon okIcon = new ImageIcon(Utilities.imagesDir + "smallok.gif");
    private final ImageIcon cancelIcon = new ImageIcon(Utilities.imagesDir + "smallcancel.gif");

    // A vector containing the listeners to this component.
    private final Vector<PinValueListener> listeners;

    // A boolean value which is true if the user pressed the ok button and
    // false otherwise.
    private boolean isOk = false;

    // The number of available bits.
    private int numberOfBits;

    /**
     * Constructs a new BinaryComponent.
     */
    public BinaryComponent() {
        listeners = new Vector<>();

        jbInit();
    }

    /**
     * Registers the given PinValueListener as a listener to this component.
     */
    public void addListener(PinValueListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Notify all the PinValueListeners on actions taken in it, by creating a
     * PinValueEvent and sending it using the pinValueChanged method to all
     * of the listeners.
     */
    public void notifyListeners() {
        PinValueEvent event = new PinValueEvent(this, valueStr.toString(), isOk);
        listeners.forEach(l -> l.pinValueChanged(event));
    }

    /**
     * Sets the number of bits of this component.
     */
    public void setNumOfBits(int num) {
        numberOfBits = num;
        for (int i = 0; i < bits.length; i++) {
            if (i < bits.length - num) {
                bits[i].setText("");
                bits[i].setBackground(Color.darkGray);
                bits[i].setEnabled(false);
            } else {
                bits[i].setBackground(Color.white);
                bits[i].setEnabled(true);
            }
        }
    }

    /**
     * Sets the value of this component.
     */
    public void setValue(short value) {
        valueStr = new StringBuffer(Conversions.decimalToBinary(value, 16));
        for (int i = 0; i < bits.length; i++) {
            bits[i].setText(String.valueOf(valueStr.charAt(i)));
        }
    }

    /**
     * Returns the value of this component.
     */
    public short getValue() {
        return (short) Conversions.binaryToInt(valueStr.toString());
    }

    // Updates the value of this component.
    private void updateValue() {
        valueStr = new StringBuffer(16);
        char currentChar;
        for (JTextField bit : bits) {
            if (bit.getText().equals(""))
                currentChar = '0';
            else
                currentChar = bit.getText().charAt(0);
            valueStr.append(currentChar);
        }
    }

    /**
     * Implementing the action of double-clicking the mouse on the text field.
     * "0" --> "1"
     * "1" --> "0"
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            JTextField t = (JTextField) e.getSource();
            if (t.getText().equals("0"))
                t.setText("1");
            else if (t.getText().equals("1"))
                t.setText("0");
        }
    }

    /**
     * Implementing the action of inserting a letter into the text field,
     * or pressing enter / escape.
     */
    public void keyTyped(KeyEvent e) {
        JTextField t = (JTextField) e.getSource();
        if (e.getKeyChar() == '0' || e.getKeyChar() == '1') {
            t.transferFocus();
            t.selectAll();
        } else if (e.getKeyChar() == Event.ENTER) {
            approve();
        } else if (e.getKeyChar() == Event.ESCAPE) {
            hideBinary();
        } else {
            t.selectAll();
            t.getToolkit().beep();
        }
    }

    // Empty implementations
    public void mouseExited(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    // Initialization of this component.
    private void jbInit() {
        // The border of this component.
        Border binaryBorder = BorderFactory.createLineBorder(Color.black, 3);
        this.setLayout(null);
        okButton.setHorizontalTextPosition(SwingConstants.CENTER);
        okButton.setIcon(okIcon);
        okButton.setBounds(new Rectangle(97, 29, 23, 20));
        okButton.addActionListener(this::okButton_actionPerformed);
        cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cancelButton.setIcon(cancelIcon);
        cancelButton.setBounds(new Rectangle(125, 29, 23, 20));
        cancelButton.addActionListener(this::cancelButton_actionPerformed);
        this.setBorder(binaryBorder);

        for (int i = 0; i < bits.length; i++) {
            final JTextField bit = new JTextField(1);
            bit.setFont(Utilities.valueFont);
            bit.setText("0");
            bit.setHorizontalAlignment(SwingConstants.RIGHT);
            bit.setBounds(new Rectangle(16 + 13 * (bits.length - i - 1), 8, 13, 19));
            bit.addMouseListener(this);
            bit.addKeyListener(this);

            bits[i] = bit;

            this.add(bit);
        }

        this.add(cancelButton, null);
        this.add(okButton, null);
    }

    /**
     * Approve the change (called when OK is pressed or ENTER is pressed
     */
    private void approve() {
        isOk = true;
        updateValue();
        notifyListeners();
        setVisible(false);
    }

    /**
     * Implementing the action of pressing the ok button.
     */
    public void okButton_actionPerformed(ActionEvent e) {
        approve();
    }

    /**
     * Implementing the action of pressing the cancel button.
     */
    public void cancelButton_actionPerformed(ActionEvent e) {
        hideBinary();
    }

    /**
     * Hides the binary component as though the cancel button was pressed
     */
    public void hideBinary() {
        isOk = false;
        notifyListeners();
        setVisible(false);
    }

    /**
     * Shows the Binary component and gives focus to the first available bit.
     */
    public void showBinary() {
        setVisible(true);
        bits[16 - numberOfBits].grabFocus();
    }
}

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

import Hack.Utilities.Conversions;
import HackGUI.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * This class represents a 16-bits binary number.
 */
public class BinaryComponent extends JPanel implements MouseListener, KeyListener {

    // An array containing all of the text fields.
    private final JTextField[] bits = new JTextField[16];
    private final BooleanSupplier isVisible;
    private final Consumer<Boolean> setVisible;

    // The value of this component in a String representation.
    private StringBuffer valueStr;

    // Creating icons.
    private static final ImageIcon okIcon = new ImageIcon(Utilities.imagesDir + "smallok.gif");
    private static final ImageIcon cancelIcon = new ImageIcon(Utilities.imagesDir + "smallcancel.gif");

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
    public BinaryComponent(BooleanSupplier isVisible, Consumer<Boolean> setVisible) {
        this.isVisible = isVisible;
        this.setVisible = setVisible;
        this.listeners = new Vector<>();

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
        for (JTextField bit : bits) {
            char currentChar;
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
        final JPanel bitsPanel = new JPanel();
        bitsPanel.setLayout(new BoxLayout(bitsPanel, BoxLayout.X_AXIS));
        bitsPanel.add(Box.createHorizontalGlue());
        for (int i = bits.length - 1; i >= 0; i--) {
            final JTextField bit = new JTextField(1);
            bit.setFont(Utilities.valueFont);
            bit.setText("0");
            bit.setHorizontalAlignment(SwingConstants.RIGHT);
            Utilities.fixSize(bit, new Dimension(20, 19));
            bit.addMouseListener(this);
            bit.addKeyListener(this);

            bits[i] = bit;

            bitsPanel.add(bit);
        }
        bitsPanel.add(Box.createHorizontalGlue());
        bitsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JButton okButton = new JButton();
        okButton.setHorizontalTextPosition(SwingConstants.CENTER);
        okButton.setIcon(okIcon);
        okButton.addActionListener(e -> approve());
        Utilities.fixSize(okButton, new Dimension(okIcon.getIconWidth(), okIcon.getIconHeight()));

        final JButton cancelButton = new JButton();
        cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cancelButton.setIcon(cancelIcon);
        cancelButton.addActionListener(e -> hideBinary());
        Utilities.fixSize(cancelButton, new Dimension(cancelIcon.getIconWidth(), cancelIcon.getIconHeight()));

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(Box.createVerticalGlue());
        this.add(bitsPanel);
        this.add(buttonsPanel);
        this.setBorder(BorderFactory.createLineBorder(Color.black, 3));
    }

    /**
     * Approve the change (called when OK is pressed or ENTER is pressed
     */
    private void approve() {
        isOk = true;
        updateValue();
        notifyListeners();
        setVisible.accept(false);
    }

    /**
     * Hides the binary component as though the cancel button was pressed
     */
    public void hideBinary() {
        isOk = false;
        notifyListeners();
        setVisible.accept(false);
    }

    /**
     * Shows the Binary component and gives focus to the first available bit.
     */
    public void showBinary() {
        setVisible.accept(true);
        bits[16 - numberOfBits].grabFocus();
    }

    public boolean isShown() {
        return isVisible.getAsBoolean();
    }
}

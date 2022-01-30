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

import Hack.ComputerParts.ComputerPartEvent;
import Hack.ComputerParts.ComputerPartEventListener;
import Hack.ComputerParts.RegisterGUI;
import Hack.Events.ErrorEvent;
import Hack.Events.ErrorEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

/**
 * This class represents the GUI of a register.
 */
public class RegisterComponent extends JPanel implements RegisterGUI {

    // The label with the name of this register.
    protected JLabel registerName = new JLabel();

    // The text field containing the value of this register.
    protected JTextField registerValue = new JTextField();

    // A vector containing the listeners to this object.
    private final Vector<ComputerPartEventListener> listeners;

    // A vector containing the error listeners to this object.
    private final Vector<ErrorEventListener> errorEventListeners;

    // The value of the register
    protected short value;

    // The old value of this component
    protected String oldValue;

    // The format in which the value is represented: decimal, hexadecimal or binary format.
    protected int dataFormat;

    // The null value of this component.
    protected short nullValue;

    // A boolean field specifying if the null value should be activated or not.
    protected boolean hideNullValue;

    /**
     * Constructs a new RegisterComponent.
     */
    public RegisterComponent() {
        dataFormat = Format.DEC_FORMAT;
        listeners = new Vector<>();
        errorEventListeners = new Vector<>();
        // initializes the register
        value = 0;
        registerValue.setText(translateValueToString(value));

        jbInit();
    }

    /**
     * Sets the null value of this component.
     */
    public void setNullValue(short newValue, boolean hideNullValue) {
        nullValue = newValue;
        this.hideNullValue = hideNullValue;
        if (value == nullValue && hideNullValue)
            oldValue = "";
    }

    public void addListener(ComputerPartEventListener listener) {
        listeners.addElement(listener);
    }

    public void removeListener(ComputerPartEventListener listener) {
        listeners.removeElement(listener);
    }

    public void notifyListeners(int address, short value) {
        ComputerPartEvent event = new ComputerPartEvent(this, 0, value);
        listeners.forEach(l -> l.valueChanged(event));
    }

    public void notifyListeners() {
        listeners.forEach(ComputerPartEventListener::guiGainedFocus);
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
     * Enables user input into the source.
     */
    public void enableUserInput() {
        registerValue.setEnabled(true);
    }

    /**
     * Disables user input into the source.
     */
    public void disableUserInput() {
        registerValue.setEnabled(false);
    }

    /**
     * Translates a given short to a string according to the current format.
     */
    protected String translateValueToString(short value) {
        if (value == nullValue && hideNullValue)
            return "";

        return Format.translateValueToString(value, dataFormat);
    }

    /**
     * Sets the value of the register with the given value.
     */
    public void setValueAt(int index, short value) {
        String data = translateValueToString(value);
        this.value = value;
        registerValue.setText(data);
    }

    /**
     * Resets the contents of this RegisterComponent.
     */
    public void reset() {
        value = nullValue;
        if (hideNullValue)
            oldValue = "";
        registerValue.setText(translateValueToString(nullValue));
        hideFlash();
        hideHighlight();
    }

    /**
     * Returns the coordinates of the top left corner of the value at the given index.
     */
    public Point getCoordinates(int index) {
        Point location = getLocation();
        return new Point(location.x + registerValue.getX(), location.y + registerValue.getY());
    }

    /**
     * Hides all highlights.
     */
    public void hideHighlight() {
        registerValue.setForeground(Color.black);
    }

    /**
     * Highlights the value at the given index.
     */
    public void highlight(int index) {
        registerValue.setForeground(Color.blue);
    }

    /**
     * flashes the value at the given index.
     */
    public void flash(int index) {
        registerValue.setBackground(Color.orange);
    }

    /**
     * hides the existing flash.
     */
    public void hideFlash() {
        registerValue.setBackground(Color.white);
    }

    /**
     * Sets the enabled range of this segment.
     * Any address outside this range will be disabled for user input.
     * If gray is true, addresses outside the range will be gray colored.
     */
    public void setEnabledRange(int start, int end, boolean gray) {
    }

    /**
     * Returns the value at the given index in its string representation.
     */
    public String getValueAsString(int index) {
        return registerValue.getText();
    }

    /**
     * Sets the numeric format with the given code (out of the format constants
     * in HackController).
     */
    public void setNumericFormat(int formatCode) {
        dataFormat = formatCode;
        registerValue.setText(Format.translateValueToString(value, formatCode));
    }

    // Implementing the action of changing the register's value.
    private void valueChanged() {
        String text = registerValue.getText();
        if (!text.equals(oldValue)) {
            try {
                value = Format.translateValueToShort(text, dataFormat);
                notifyListeners(0, value);
                oldValue = text;
            } catch (NumberFormatException nfe) {
                notifyErrorListeners("Illegal value");
                registerValue.setText(translateValueToString(value));
            }
        }
    }

    public void setName(String name) {
        registerName.setText(name);
    }

    // Initializes this register.
    private void jbInit() {
        registerName.setFont(Utilities.labelsFont);
        Utilities.fixSize(registerName, new Dimension(29, 18));

        registerValue.setFont(Utilities.valueFont);
        registerValue.setDisabledTextColor(Color.black);
        registerValue.setHorizontalAlignment(SwingConstants.RIGHT);
        Utilities.fixSize(registerValue, new Dimension(124, 18));
        registerValue.addActionListener(e -> valueChanged());
        registerValue.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                oldValue = registerValue.getText();
                notifyListeners();
            }

            public void focusLost(FocusEvent e) {
                valueChanged();
            }
        });

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(Box.createHorizontalStrut(7));
        this.add(registerName);
        this.add(registerValue);

        setBorder(BorderFactory.createEtchedBorder());
        Utilities.fixToPreferredSize(this);
    }
}

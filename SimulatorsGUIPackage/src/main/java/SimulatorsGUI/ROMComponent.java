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

import Hack.Assembler.AssemblerException;
import Hack.Assembler.HackAssemblerTranslator;
import Hack.CPUEmulator.ROM;
import Hack.CPUEmulator.ROMGUI;
import Hack.Events.ProgramEvent;
import Hack.Events.ProgramEventListener;
import HackGUI.Format;
import HackGUI.MouseOverJButton;
import HackGUI.PointedMemoryComponent;
import HackGUI.TranslationException;
import HackGUI.Utilities;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.Vector;

/**
 * This class represents the GUI of a ROM.
 */
public class ROMComponent extends PointedMemoryComponent implements ROMGUI {

    // A vector containing the listeners to this object.
    private final Vector<ProgramEventListener> programEventListeners;

    // The ASM format.
    private final static int ASM_FORMAT = ROM.ASM_FORMAT;

    // The load file button.
    private final MouseOverJButton loadButton = new MouseOverJButton();

    // The icon on the load file button.
    private static final ImageIcon loadIcon = new ImageIcon(Utilities.imagesDir + "open2.gif");

    // The file chooser component.
    private final JFileChooser fileChooser;

    // The hack assembler translator.
    private final HackAssemblerTranslator translator = HackAssemblerTranslator.getInstance();

    // The possible numeric formats.
    private static final String[] format = {"Asm", "Dec", "Hex", "Bin"};

    // The combo box for choosing the numeric format.
    private final JComboBox<String> romFormat = new JComboBox<>(format);

    public static ROMComponent create() {
        final ROMComponent component = new ROMComponent();
        component.init();
        return component;
    }

    /**
     * Constructs a new ROMComponent.
     */
    protected ROMComponent() {
        dataFormat = ASM_FORMAT;
        programEventListeners = new Vector<>();
        // The file filter of this component.
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ROMFileFilter());
    }


    public void setNumericFormat(int formatCode) {
        super.setNumericFormat(formatCode);
        switch (formatCode) {
            case ASM_FORMAT:
                romFormat.setSelectedIndex(0);
                break;
            case Format.DEC_FORMAT:
                romFormat.setSelectedIndex(1);
                break;
            case Format.HEX_FORMAT:
                romFormat.setSelectedIndex(2);
                break;
            case Format.BIN_FORMAT:
                romFormat.setSelectedIndex(3);
                break;
        }
    }

    /**
     * Registers the given ProgramEventListener as a listener to this GUI.
     */
    public void addProgramListener(ProgramEventListener listener) {
        programEventListeners.addElement(listener);
    }

    /**
     * Notifies all the ProgramEventListeners on a change in the ROM's program by
     * creating a ProgramEvent (with the new event type and program's file name) and sending it
     * using the programChanged method to all the listeners.
     */
    public void notifyProgramListeners(byte eventType, String programFileName) {
        ProgramEvent event = new ProgramEvent(this, eventType, programFileName);
        programEventListeners.forEach(l -> l.programChanged(event));
    }

    /**
     * Overrides to add the program clear functionality.
     */
    public void notifyClearListeners() {
        super.notifyClearListeners();
        notifyProgramListeners(ProgramEvent.CLEAR, null);
    }

    protected DefaultTableCellRenderer getCellRenderer() {
        return new ROMTableCellRenderer();
    }

    /**
     * Sets the current program file name with the given name.
     */
    public void setProgram(String programFileName) {
        // The name of the current program.
    }

    /**
     * Returns the value at the given index in its string representation.
     */
    public String getValueAsString(int index) {
        if (dataFormat != ASM_FORMAT)
            return super.getValueAsString(index);
        else
            return Format.translateValueToString(values[index], Format.DEC_FORMAT);
    }

    /**
     * Translates a given string to a short according to the current format.
     */
    protected short translateValueToShort(String data) throws TranslationException {
        short result;
        if (dataFormat != ASM_FORMAT)
            result = super.translateValueToShort(data);
        else {
            try {
                result = translator.textToCode(data);
            } catch (AssemblerException ae) {
                throw new TranslationException(ae.getMessage());
            }
        }
        return result;
    }

    /**
     * Translates a given short to a string according to the current format.
     */
    protected String translateValueToString(short value) {
        if (dataFormat != ASM_FORMAT)
            return super.translateValueToString(value);

        try {
            return translator.codeToText(value);
        } catch (AssemblerException ignored) {
            return null;
        }
    }

    // Initializes this rom.
    private void init() {
        loadButton.setIcon(loadIcon);
        loadButton.setBounds(new Rectangle(97, 2, 31, 25));
        loadButton.setToolTipText("Load Program");
        loadButton.addActionListener(e -> loadProgram());
        Utilities.fixSize(loadButton, new Dimension(31, 25));
        romFormat.setFont(Utilities.thinLabelsFont);
        romFormat.setToolTipText("Display Format");
        Utilities.fixToPreferredSize(romFormat);
        romFormat.addActionListener(e -> {
            String newFormat = (String) romFormat.getSelectedItem();
            if (format[0].equals(newFormat))
                setNumericFormat(ASM_FORMAT);
            else if (format[1].equals(newFormat))
                setNumericFormat(Format.DEC_FORMAT);
            else if (format[2].equals(newFormat))
                setNumericFormat(Format.HEX_FORMAT);
            else if (format[3].equals(newFormat))
                setNumericFormat(Format.BIN_FORMAT);
        });

        jbInit(romFormat, loadButton);
    }

    /**
     * Sets the current working dir.
     */
    public void setWorkingDir(File file) {
        fileChooser.setCurrentDirectory(file);
    }

    /**
     * Opens the file chooser for loading a new program.
     */
    public void loadProgram() {
        int returnVal = fileChooser.showDialog(this, "Load ROM");
        if (returnVal == JFileChooser.APPROVE_OPTION)
            notifyProgramListeners(ProgramEvent.LOAD,
                                   fileChooser.getSelectedFile().getAbsolutePath());
    }

    /**
     * An inner class which implements the cell renderer of the rom table,
     * giving the feature of coloring the background of a specific cell.
     */
    public class ROMTableCellRenderer extends PointedMemoryTableCellRenderer {
        public void setRenderer(int row, int column) {
            super.setRenderer(row, column);

            if (dataFormat == ASM_FORMAT && column == 1)
                setHorizontalAlignment(SwingConstants.LEFT);
        }
    }
}

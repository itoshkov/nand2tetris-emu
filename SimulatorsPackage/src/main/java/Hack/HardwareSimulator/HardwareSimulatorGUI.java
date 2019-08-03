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

package Hack.HardwareSimulator;

import Hack.Controller.*;
import Hack.Gates.*;
import Hack.ComputerParts.*;

/**
 * An interface for the GUI of the Hardware Simulator.
 */
public interface HardwareSimulatorGUI extends HackSimulatorGUI {

    /**
     * Returns the Gates panel.
     */
    GatesPanelGUI getGatesPanel();

    /**
     * Returns the HDLView.
     */
    TextFileGUI getHDLView();

    /**
     * Returns the GateInfo component.
     */
    GateInfoGUI getGateInfo();

    /**
     * Returns the input pins table.
     */
    PinsGUI getInputPins();

    /**
     * Returns the output pins table.
     */
    PinsGUI getOutputPins();

    /**
     * Returns the internal pins table.
     */
    PinsGUI getInternalPins();

    /**
     * Returns the part pins table.
     */
    PartPinsGUI getPartPins();

    /**
     * Returns the parts table.
     */
    PartsGUI getParts();

    /**
     * Displays the Internal pins table.
     */
    void showInternalPins();

    /**
     * Hides the Internal pins table.
     */
    void hideInternalPins();

    /**
     * Displays the Part pins table.
     */
    void showPartPins();

    /**
     * Hides the Part pins table.
     */
    void hidePartPins();

    /**
     * Displays the Parts table.
     */
    void showParts();

    /**
     * Hides the Parts table.
     */
    void hideParts();

}

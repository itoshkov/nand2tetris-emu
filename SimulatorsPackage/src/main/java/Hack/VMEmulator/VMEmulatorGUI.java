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

package Hack.VMEmulator;

import Hack.ComputerParts.*;
import Hack.Controller.*;
import Hack.CPUEmulator.ScreenGUI;
import Hack.CPUEmulator.KeyboardGUI;

/**
 * An interface for a GUI of the VM emulator.
 */
public interface VMEmulatorGUI extends HackSimulatorGUI {

    /**
     * Returns the bus GUI component.
     */
    BusGUI getBus();

    /**
     * Returns the screen GUI component.
     */
    ScreenGUI getScreen();

    /**
     * Returns the keyboard GUI component.
     */
    KeyboardGUI getKeyboard();

    /**
     * Returns the RAM GUI component.
     */
    LabeledPointedMemoryGUI getRAM();

    /**
     * Returns the Program GUI component.
     */
    VMProgramGUI getProgram();

    /**
     * Returns the call stack GUI component.
     */
    CallStackGUI getCallStack();

    /**
     * Returns the calculator GUI component.
     */
    CalculatorGUI getCalculator();

    /**
     * Returns the method stack GUI component.
     */
    PointedMemorySegmentGUI getWorkingStack();

    /**
     * Returns the Stack GUI component.
     */
    PointedMemorySegmentGUI getStack();

    /**
     * Returns the static memory segment component.
     */
    MemorySegmentGUI getStaticSegment();

    /**
     * Returns the local memory segment component.
     */
    MemorySegmentGUI getLocalSegment();

    /**
     * Returns the arg memory segment component.
     */
    MemorySegmentGUI getArgSegment();

    /**
     * Returns the this memory segment component.
     */
    MemorySegmentGUI getThisSegment();

    /**
     * Returns the that memory segment component.
     */
    MemorySegmentGUI getThatSegment();

    /**
     * Returns the temp memory segment component.
     */
    MemorySegmentGUI getTempSegment();

    /**
     * Sets the focus on the VMEmulator's frame.
     */
    void requestFocus();
}

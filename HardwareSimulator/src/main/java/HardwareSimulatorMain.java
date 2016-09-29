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

import Hack.Controller.HackController;
import Hack.HardwareSimulator.HardwareSimulator;
import Hack.HardwareSimulator.HardwareSimulatorApplication;
import Hack.HardwareSimulator.HardwareSimulatorControllerGUI;
import Hack.HardwareSimulator.HardwareSimulatorGUI;
import SimulatorsGUI.HardwareSimulatorComponent;
import SimulatorsGUI.HardwareSimulatorControllerComponent;

import javax.swing.*;

/**
 * The Hardware Simulator.
 */
public class HardwareSimulatorMain {
    /**
     * The command line Hardware Simulator program.
     */
    public static void main(String[] args) {
        switch (args.length) {
            case 0:
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (Exception ignored) {
                }

                final HardwareSimulatorGUI simulatorGUI = new HardwareSimulatorComponent();
                final HardwareSimulatorControllerGUI controllerGUI = new HardwareSimulatorControllerComponent();
                new HardwareSimulatorApplication(controllerGUI, simulatorGUI, "bin/scripts/defaultHW.txt");
                break;

            case 1:
                new HackController(new HardwareSimulator(), args[0]);
                break;

            default:
                System.err.printf("Usage: java %s [script name]%n", HardwareSimulatorMain.class.getName());
                System.exit(1);
        }
    }
}

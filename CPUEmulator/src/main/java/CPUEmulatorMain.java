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

import Hack.CPUEmulator.CPUEmulator;
import Hack.CPUEmulator.CPUEmulatorApplication;
import Hack.CPUEmulator.CPUEmulatorGUI;
import Hack.Controller.ControllerGUI;
import Hack.Controller.HackController;
import HackGUI.ControllerComponent;
import SimulatorsGUI.CPUEmulatorComponent;

import javax.swing.*;

/**
 * The CPU Emulator.
 */
public class CPUEmulatorMain {
    /**
     * The command line CPU Emulator program.
     */
    public static void main(String[] args) {
        switch (args.length) {
            case 0:
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (Exception ignored) {
                }

                final CPUEmulatorGUI simulatorGUI = new CPUEmulatorComponent();
                final ControllerGUI controllerGUI = new ControllerComponent();
                new CPUEmulatorApplication(controllerGUI, simulatorGUI, "bin/scripts/defaultCPU.txt");
                break;

            case 1:
                new HackController(new CPUEmulator(), args[0]);
                break;

            default:
                System.err.printf("Usage: java %s [script name]%n", CPUEmulatorMain.class.getName());
                System.exit(1);
        }
    }
}

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

package Hack.Translators;

import Hack.ComputerParts.*;
import java.io.*;

/**
 * The GUI of the HackTranslator.
 */
public interface HackTranslatorGUI {

    /**
     * Displays the given message, according to the given type.
     */
    void displayMessage(String message, boolean error);

    /**
     * Sets the title of the translator with the given title.
     */
    void setTitle(String title);

    /**
     * Returns the GUI of the Source file.
     */
    TextFileGUI getSource();

    /**
     * Returns the GUI of the Destination file.
     */
    TextFileGUI getDestination();

    /**
     * Sets the name of the Source file with the given name.
     */
    void setSourceName(String name);

    /**
     * Sets the name of the Destination file with the given name.
     */
    void setDestinationName(String name);

    /**
     * Sets the working dir name with the given one.
     */
    void setWorkingDir(File file);

    /**
     * Enables the single step action.
     */
    void enableSingleStep();

    /**
     * Disables the single step action.
     */
    void disableSingleStep();

    /**
     * Enables the fast forward action.
     */
    void enableFastForward();

    /**
     * Disables the fast forward action.
     */
    void disableFastForward();

    /**
     * Enables the stop action.
     */
    void enableStop();

    /**
     * Disables the stop action.
     */
    void disableStop();

    /**
     * Enables the rewind action.
     */
    void enableRewind();

    /**
     * Disables the rewind action.
     */
    void disableRewind();

    /**
     * Enables the full compilation action.
     */
    void enableFullCompilation();

    /**
     * Disables the full compilation action.
     */
    void disableFullCompilation();

    /**
     * Enables the save action.
     */
    void enableSave();

    /**
     * Disables the save action.
     */
    void disableSave();

    /**
     * Enables loading a new source file.
     */
    void enableLoadSource();

    /**
     * Disables loading a new source file.
     */
    void disableLoadSource();

    /**
     * Enables selecting a row in the source.
     */
    void enableSourceRowSelection();

    /**
     * Disables selecting a row in the source.
     */
    void disableSourceRowSelection();
}

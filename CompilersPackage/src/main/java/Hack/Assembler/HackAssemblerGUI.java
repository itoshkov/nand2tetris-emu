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

package Hack.Assembler;

import Hack.ComputerParts.*;
import Hack.Translators.*;

/**
 * The GUI of the HackAssembler.
 */
public interface HackAssemblerGUI extends HackTranslatorGUI {

    /**
     * Returns the GUI of the Comparison file.
     */
    TextFileGUI getComparison();

    /**
     * Sets the name of the Comparison file with the given name.
     */
    void setComparisonName(String name);

    /**
     * Enables loading a comparison file.
     */
    void enableLoadComparison();

    /**
     * Disables loading a comparison file.
     */
    void disableLoadComparison();

    /**
     * Shows the comparison file
     */
    void showComparison();

    /**
     * Hides the comparison file
     */
    void hideComparison();


}

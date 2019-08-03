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

package Hack.ComputerParts;

/**
 * An interface for the GUI of a text file.
 */
public interface TextFileGUI extends ComputerPartGUI {

    /**
     * Registers the given TextFileEventListener as a listener to this GUI.
     */
    void addTextFileListener(TextFileEventListener listener);

    /**
     * Notifies all the TextFileEventListeners on a change in the selected row by creating
     * an TextFileEvent (with the selected row string and index) and sending it using the
     * rowSelected method to all the listeners.
     */
    void notifyTextFileListeners(String rowString, int rowIndex);

    /**
     * Sets the TextFile's contents with the given file.
     */
    void setContents(String fileName);

    /**
     * Sets the contents of the text file with the given String array.
     */
    void setContents(String[] text);

    /**
     * Adds the given line at the end of the text file.
     */
    void addLine(String line);

    /**
     * Highlights the line with the given index. This adds to the current highlighted lines.
     * If clear is true, other highlights will be cleared.
     */
    void addHighlight(int index, boolean clear);

    /**
     * Clears all the current highlights.
     */
    void clearHighlights();

    /**
     * Returns the line at the given index (assumes a legal index).
     */
    String getLineAt(int index);

    /**
     * Returns the number of lines in the file.
     */
    int getNumberOfLines();

    /**
     * Selects the commands in the range fromIndex..toIndex
     */
    void select(int fromIndex, int toIndex);

    /**
     * Hides all selections.
     */
    void hideSelect();
}

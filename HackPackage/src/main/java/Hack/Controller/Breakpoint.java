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

package Hack.Controller;

/**
 * A controller script breakpoint: includes a variable and a desired value.
 */
public class Breakpoint implements Comparable<Breakpoint> {

    // The variable name
    private final String varName;

    // The desired value
    private final String value;

    // The status of the breakpoint
    private boolean reached;

    /**
     * Constructs a new Breakpoint with the given variable name and desired value.
     */
    public Breakpoint(String varName, String value) {
        this.varName = varName;
        this.value = value;
        reached = false;
    }

    /**
     * Returns the variable name.
     */
    public String getVarName() {
        return varName;
    }

    /**
     * Returns the breakpoint value.
     */
    public String getValue() {
        return value;
    }

    /**
     * sets the breakpoint "off" - puts it into "not reached" state.
     */
    public void off() {
        reached = false;
    }

    /**
     * Sets the breakpoint "on" - puts it into "reached" state.
     */
    public void on() {
        reached = true;
    }

    /**
     * Returns true if the breakpoint is reached.
     */
    public boolean isReached() {
        return reached;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Breakpoint that = (Breakpoint) o;

        return varName.equals(that.varName) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = varName.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public int compareTo(Breakpoint o) {
        final int c = varName.compareTo(o.varName);
        return c != 0? c : value.compareTo(o.value);
    }
}

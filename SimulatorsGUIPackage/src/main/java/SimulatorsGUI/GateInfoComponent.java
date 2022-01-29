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

import Hack.HardwareSimulator.GateInfoGUI;
import HackGUI.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * This class represents the GUI of a gate info.
 */
public class GateInfoComponent extends JPanel implements GateInfoGUI {

    // creating labels
    private final JLabel chipNameLbl;
    private final JLabel timeLbl;

    // creating text fields
    private final JTextField chipNameTxt;
    private final JTextField timeTxt;

    // true if the clock is currently up
    private boolean clockUp;

    // the name of the chip
    private String chipName;

    /**
     * Constructs a new GateInfoComponent.
     */
    public GateInfoComponent() {
        chipNameLbl = new JLabel();
        timeLbl = new JLabel();

        chipNameTxt = new JTextField();
        timeTxt = new JTextField();

        jbInit();
    }

    public void setChip (String chipName) {
        this.chipName = chipName;
        chipNameTxt.setText(chipName);
    }

    public void setClock (boolean up) {
        clockUp = up;
        if(up)
            timeTxt.setText(timeTxt.getText() + "+");
    }

    public void setClocked (boolean clocked) {
        if(clocked)
            chipNameTxt.setText(chipName + " (Clocked) ");
        else
            chipNameTxt.setText(chipName);
    }


    public void setTime (int time) {
         if (clockUp)
            timeTxt.setText(time + "+");
        else
            timeTxt.setText(String.valueOf(time));
    }


    public void reset() {
        chipNameTxt.setText("");
        timeTxt.setText("0");
    }

    public void enableTime() {
        timeLbl.setEnabled(true);
        timeTxt.setEnabled(true);
    }

    public void disableTime() {
        timeLbl.setEnabled(false);
        timeTxt.setEnabled(false);
    }

    // Initializes this component.
    private void jbInit() {
        chipNameLbl.setText("Chip Name :");
        Utilities.fixToPreferredSize(chipNameLbl);

        chipNameTxt.setBackground(SystemColor.info);
        chipNameTxt.setFont(Utilities.thinBigLabelsFont);
        chipNameTxt.setEditable(false);
        chipNameTxt.setHorizontalAlignment(SwingConstants.LEFT);
        Utilities.fixSize(chipNameTxt, new Dimension(231, 20));

        timeLbl.setText("Time :");
        Utilities.fixToPreferredSize(timeLbl);

        timeTxt.setBackground(SystemColor.info);
        timeTxt.setFont(Utilities.thinBigLabelsFont);
        timeTxt.setEditable(false);
        Utilities.fixSize(timeTxt, new Dimension(69, 20));

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.add(Box.createRigidArea(new Dimension(10, 37)));
        this.add(chipNameLbl);
        this.add(Box.createRigidArea(new Dimension(4, 37)));
        this.add(chipNameTxt);
        this.add(Box.createRigidArea(new Dimension(20, 37)));
        this.add(Box.createHorizontalGlue());
        this.add(timeLbl);
        this.add(Box.createRigidArea(new Dimension(4, 37)));
        this.add(timeTxt);
        this.add(Box.createRigidArea(new Dimension(10, 37)));

        setBorder(BorderFactory.createEtchedBorder());
    }
}

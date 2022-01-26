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

package HackGUI;

import Hack.Controller.Breakpoint;
import Hack.Controller.ControllerEvent;
import Hack.Controller.ControllerEventListener;
import Hack.Controller.ControllerGUI;
import Hack.Controller.HackController;
import Hack.Controller.HackSimulatorGUI;
import Hack.Controller.Profiler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class represents the GUI of the controller component.
 */
public class ControllerComponent extends JFrame
        implements ControllerGUI, FilesTypeListener, BreakpointsChangedListener {

    // The dimensions of the toolbar.
    protected static final int TOOLBAR_WIDTH = 1016;
    protected static final int TOOLBAR_HEIGHT = 55;

    // The dimensions of this window.
    private static final int CONTROLLER_WIDTH = 1024;
    private static final int CONTROLLER_HEIGHT = 741;

    // The dimensions of the toolbar's separator.
    protected static final Dimension separatorDimension = new Dimension(3, TOOLBAR_HEIGHT - 5);

    // The vector of listeners to this component.
    private final Vector<ControllerEventListener> listeners;

    // The fast forward button.
    protected MouseOverJButton ffwdButton;

    // The stop button.
    protected MouseOverJButton stopButton;

    // The rewind button.
    protected MouseOverJButton rewindButton;

    // The load script button.
    protected MouseOverJButton scriptButton;

    // The breakpoints button.
    protected MouseOverJButton breakButton;

    // The single step button.
    protected MouseOverJButton singleStepButton;

    protected MouseOverJButton stepOverButton;

    // The load program button.
    protected MouseOverJButton loadProgramButton;

    // Creating the file chooser window & the breakpoint window.
    private final JFileChooser fileChooser = new JFileChooser();
    private final BreakpointWindow breakpointWindow = new BreakpointWindow();
    private ProfilerWindow profilerWindow;

    // Creating the icons for the buttons.
    private static final ImageIcon rewindIcon = new ImageIcon(Utilities.imagesDir + "vcrrewind.gif");
    private static final ImageIcon ffwdIcon = new ImageIcon(Utilities.imagesDir + "vcrfastforward.gif");
    private static final ImageIcon singleStepIcon = new ImageIcon(Utilities.imagesDir + "vcrforward.gif");
    private static final ImageIcon stepOverIcon = new ImageIcon(Utilities.imagesDir + "vcrstepover.gif");
    private static final ImageIcon stopIcon = new ImageIcon(Utilities.imagesDir + "vcrstop.gif");
    private static final ImageIcon breakIcon = new ImageIcon(Utilities.imagesDir + "redflag.gif");
    private static final ImageIcon loadProgramIcon = new ImageIcon(Utilities.imagesDir + "opendoc.gif");
    private static final ImageIcon scriptIcon = new ImageIcon(Utilities.imagesDir + "scroll.gif");

    // The speed slider.
    protected JSlider speedSlider;

    // A combo box which controls the format of all the components.
    protected TitledComboBox formatCombo;

    // A combo box for choosing the additional display.
    protected TitledComboBox additionalDisplayCombo;

    // A combo box for choosing the animation type.
    protected TitledComboBox animationCombo;

    // The toolbar of the controller.
    protected JToolBar toolBar;

    // The components of the menu
    protected JMenuBar menuBar;
    protected JMenu fileMenu, viewMenu, runMenu, helpMenu;
    protected JMenuItem singleStepMenuItem, ffwdMenuItem, stopMenuItem, rewindMenuItem, exitMenuItem;
    protected JMenuItem usageMenuItem, aboutMenuItem;
    protected JMenu animationSubMenu, numericFormatSubMenu, additionalDisplaySubMenu;
    protected JMenuItem breakpointsMenuItem, scriptMenuItem, programMenuItem;
    protected JMenuItem profilerMenuItem;
    protected JRadioButtonMenuItem decMenuItem, hexaMenuItem, binMenuItem;
    protected JRadioButtonMenuItem scriptDisplayMenuItem, outputMenuItem, compareMenuItem, noAdditionalDisplayMenuItem;
    protected JRadioButtonMenuItem partAnimMenuItem, fullAnimMenuItem, noAnimMenuItem;

    // the message label (status line)
    private final JTextField messageLbl = new JTextField();

    // component for displaying the script, output file and comparison file.
    protected FileDisplayComponent scriptComponent;
    protected FileDisplayComponent outputComponent;
    protected FileDisplayComponent comparisonComponent;

    // HTML viewers for the usage and about windows.
    private HTMLViewFrame usageWindow, aboutWindow;

    /**
     * Constructs a new ControllerComponent.
     */
    public ControllerComponent() {
        messageLbl.setEditable(false);
        listeners = new Vector<>();
        formatCombo = new TitledComboBox("Format:", "Numeric display format",
                                         new String[]{"Decimal", "Hexa", "Binary"}, 75);
        additionalDisplayCombo = new TitledComboBox("View:", "View options",
                                                    new String[]{"Script", "Output", "Compare", "Screen"}, 80);
        animationCombo = new TitledComboBox("Animate:", "Animation type",
                                            new String[]{"Program flow", "Program & data flow",
                                                    "No animation"}, 135);
        scriptComponent = new FileDisplayComponent();
        outputComponent = new FileDisplayComponent();
        comparisonComponent = new FileDisplayComponent();

        init();
        jbInit();
    }

    public void setWorkingDir(File file) {
        fileChooser.setCurrentDirectory(file);
    }

    public void setSimulator(HackSimulatorGUI simulator) {
        ((JComponent) simulator).setLocation(0, TOOLBAR_HEIGHT);
        this.getContentPane().add((JComponent) simulator, null);
        ((JComponent) simulator).revalidate();
        repaint();

        usageWindow = new HTMLViewFrame(getClass().getResource("/usage.html"));
        usageWindow.setSize(450, 430);

        aboutWindow = new HTMLViewFrame(getClass().getResource("/about.html"));
        aboutWindow.setSize(450, 420);
    }


    public JComponent getComparisonComponent() {
        return comparisonComponent;
    }

    public JComponent getOutputComponent() {
        return outputComponent;
    }

    public JComponent getScriptComponent() {
        return scriptComponent;
    }

    // Initializes the buttons and speed slider
    protected void init() {
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, HackController.NUMBER_OF_SPEED_UNITS, 1);
        loadProgramButton = new MouseOverJButton();
        ffwdButton = new MouseOverJButton();
        stopButton = new MouseOverJButton();
        rewindButton = new MouseOverJButton();
        scriptButton = new MouseOverJButton();
        breakButton = new MouseOverJButton();
        singleStepButton = new MouseOverJButton();
        stepOverButton = new MouseOverJButton();
    }


    /**
     * Registers the given ControllerEventListener as a listener to this GUI.
     */
    public void addControllerListener(ControllerEventListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Notify all the ControllerEventListeners on actions taken in it, by creating a
     * ControllerEvent (with the action and supplied data) and sending it using the
     * actionPerformed method to all the listeners.
     */
    public void notifyControllerListeners(byte action, Object data) {
        final ControllerEvent event = new ControllerEvent(this, action, data);
        listeners.forEach(listener -> listener.actionPerformed(event));
    }

    /**
     * Sets the script file name with the given one.
     */
    public void setScriptFile(String fileName) {
        scriptComponent.setContents(fileName);
    }

    /**
     * Sets the output file name with the given one.
     */
    public void setOutputFile(String fileName) {
        outputComponent.setContents(fileName);
    }

    /**
     * Sets the comparison file name with the given one.
     */
    public void setComparisonFile(String fileName) {
        comparisonComponent.setContents(fileName);
    }

    /**
     * Sets the current script line.
     */
    public void setCurrentScriptLine(int line) {
        scriptComponent.setSelectedRow(line);
    }

    /**
     * Sets the current output line.
     */
    public void setCurrentOutputLine(int line) {
        outputComponent.setSelectedRow(line);
    }

    /**
     * Sets the current comparison line.
     */
    public void setCurrentComparisonLine(int line) {
        comparisonComponent.setSelectedRow(line);
    }

    /**
     * Shows the breakpoint panel.
     */
    public void showBreakpoints() {
        breakpointWindow.getTable().clearSelection();
        breakpointWindow.setVisible(true);
        if (breakpointWindow.getState() == Frame.ICONIFIED)
            breakpointWindow.setState(Frame.NORMAL);
    }

    public void showProfiler() {
        profilerWindow.setVisible(true);
        if (profilerWindow.getState() == Frame.ICONIFIED)
            profilerWindow.setState(Frame.NORMAL);
    }

    /**
     * Enables the single step action.
     */
    public void enableSingleStep() {
        singleStepButton.setEnabled(true);
        stepOverButton.setEnabled(true);
        singleStepMenuItem.setEnabled(true);
    }

    /**
     * Disables the single step action.
     */
    public void disableSingleStep() {
        singleStepButton.setEnabled(false);
        stepOverButton.setEnabled(false);
        singleStepMenuItem.setEnabled(false);
    }

    /**
     * Enables the fast forward action.
     */
    public void enableFastForward() {
        ffwdButton.setEnabled(true);
        ffwdMenuItem.setEnabled(true);
    }

    /**
     * Disables the fast forward action.
     */
    public void disableFastForward() {
        ffwdButton.setEnabled(false);
        ffwdMenuItem.setEnabled(false);
    }

    /**
     * Enables the stop action.
     */
    public void enableStop() {
        stopButton.setEnabled(true);
        stopMenuItem.setEnabled(true);
    }

    /**
     * Disables the stop action.
     */
    public void disableStop() {
        stopButton.setEnabled(false);
        stopMenuItem.setEnabled(false);
    }

    /**
     * Enables the eject action.
     */
    public void enableScript() {
        scriptButton.setEnabled(true);
        scriptMenuItem.setEnabled(true);
    }

    /**
     * Disables the eject action.
     */
    public void disableScript() {
        scriptButton.setEnabled(false);
        scriptMenuItem.setEnabled(false);
    }

    /**
     * Enables the rewind action.
     */
    public void enableRewind() {
        rewindButton.setEnabled(true);
        rewindMenuItem.setEnabled(true);
    }

    /**
     * Disables the rewind action.
     */
    public void disableRewind() {
        rewindButton.setEnabled(false);
        rewindMenuItem.setEnabled(false);
    }

    /**
     * Enables the load program action.
     */
    public void enableLoadProgram() {
        loadProgramButton.setEnabled(true);
    }

    /**
     * Disables the load program action.
     */
    public void disableLoadProgram() {
        loadProgramButton.setEnabled(false);
    }

    /**
     * Disables the speed slider.
     */
    public void disableSpeedSlider() {
        speedSlider.setEnabled(false);
    }

    /**
     * Enables the speed slider.
     */
    public void enableSpeedSlider() {
        speedSlider.setEnabled(true);
    }

    /**
     * Disables the animation mode buttons.
     */
    public void disableAnimationModes() {
        animationCombo.setEnabled(false);
        partAnimMenuItem.setEnabled(false);
        fullAnimMenuItem.setEnabled(false);
        noAnimMenuItem.setEnabled(false);
    }

    @Override
    public void setProfiler(Profiler profiler) {
        if (profiler != null) {
            profilerWindow = new ProfilerWindow(profiler);
            profilerMenuItem.setEnabled(true);
        } else {
            profilerWindow = null;
            profilerMenuItem.setEnabled(false);
        }
    }

    /**
     * Enables the animation mode buttons.
     */
    public void enableAnimationModes() {
        animationCombo.setEnabled(true);
        partAnimMenuItem.setEnabled(true);
        fullAnimMenuItem.setEnabled(true);
        noAnimMenuItem.setEnabled(true);
    }

    /**
     * Sets the breakpoints list with the given one.
     */
    public void setBreakpoints(Collection<Breakpoint> breakpoints) {
        // sending the given Vector to the breakpoint panel.
        breakpointWindow.setBreakpoints(breakpoints);
    }

    /**
     * Sets the speed (int code, between 1 and NUMBER_OF_SPEED_UNTIS)
     */
    public void setSpeed(int speed) {
        speedSlider.setValue(speed);
        repaint();
    }

    /**
     * Sets the list of recognized variables with the given one.
     */
    public void setVariables(String[] newVars) {
        breakpointWindow.setVariables(newVars);
    }

    /**
     * Called when the names of the files were changed.
     * The event contains the three strings representing the names of the
     * files.
     */
    public void filesNamesChanged(FilesTypeEvent event) {
        if (event.getFirstFile() != null) {
            scriptComponent.setContents(event.getFirstFile());
            notifyControllerListeners(ControllerEvent.SCRIPT_CHANGE, event.getFirstFile());
        }
        if (event.getSecondFile() != null) {
            outputComponent.setContents(event.getSecondFile());
        }
        if (event.getThirdFile() != null) {
            comparisonComponent.setContents(event.getThirdFile());
        }
    }

    /**
     * Called when there was a change in the breakpoints vector.
     * The event contains the vector of breakpoints.
     */
    public void breakpointsChanged(BreakpointsChangedEvent event) {
        notifyControllerListeners(ControllerEvent.BREAKPOINTS_CHANGE, event.getBreakpoints());
    }


    /**
     * Called when the output file is updated.
     */
    public void outputFileUpdated() {
        outputComponent.refresh();
    }

    /**
     * Sets the animation mode (int code, out of the possible animation constants in HackController)
     */
    public void setAnimationMode(int mode) {
        animationCombo.setSelectedIndex(mode);
    }

    public void setAdditionalDisplay(int display) {
        additionalDisplayCombo.setSelectedIndex(display);
    }

    /**
     * Sets the numeric format with the given code (out of the format constants
     * in HackController).
     */
    public void setNumericFormat(int formatCode) {
        formatCombo.setSelectedIndex(formatCode);
    }

    public void displayMessage(String message, boolean error) {
        if (error)
            messageLbl.setForeground(Color.red);
        else
            messageLbl.setForeground(UIManager.getColor("Label.foreground"));
        messageLbl.setText(message);
        messageLbl.setToolTipText(message);
    }

    /**
     * Sets the controller's size according to the size constants.
     */
    protected void setControllerSize() {
        setSize(new Dimension(CONTROLLER_WIDTH, CONTROLLER_HEIGHT));
    }

    /**
     * Adds the controls to the toolbar.
     */
    protected void arrangeToolBar() {
        toolBar.add(loadProgramButton);
        toolBar.addSeparator(separatorDimension);
        toolBar.add(singleStepButton);
        toolBar.add(stepOverButton);
        toolBar.add(ffwdButton);
        toolBar.add(stopButton);
        toolBar.add(rewindButton);
        toolBar.addSeparator(separatorDimension);
        toolBar.add(scriptButton);
        toolBar.add(breakButton);
        toolBar.addSeparator(separatorDimension);
        toolBar.add(speedSlider);
        toolBar.add(animationCombo);
        toolBar.add(additionalDisplayCombo);
        toolBar.add(formatCombo);
    }

    /**
     * Adds the menu items to the menuber.
     */
    protected void arrangeMenu() {

        // Build the first menu.
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        runMenu = new JMenu("Run");
        runMenu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(runMenu);

        //Build the second menu.
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        programMenuItem = new JMenuItem("Load Program", KeyEvent.VK_O);
        programMenuItem.addActionListener(e -> notifyControllerListeners(ControllerEvent.LOAD_PROGRAM, null));
        fileMenu.add(programMenuItem);

        scriptMenuItem = new JMenuItem("Load Script", KeyEvent.VK_P);
        scriptMenuItem.addActionListener(e -> scriptPressed());
        fileMenu.add(scriptMenuItem);
        fileMenu.addSeparator();

        exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK));
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        viewMenu.addSeparator();

        ButtonGroup animationRadioButtons = new ButtonGroup();

        animationSubMenu = new JMenu("Animate");
        animationSubMenu.setMnemonic(KeyEvent.VK_A);
        viewMenu.add(animationSubMenu);

        partAnimMenuItem = new JRadioButtonMenuItem("Program flow");
        partAnimMenuItem.setMnemonic(KeyEvent.VK_P);
        partAnimMenuItem.setSelected(true);
        partAnimMenuItem.addActionListener(e -> animationCombo.setSelectedIndex(HackController.DISPLAY_CHANGES));
        animationRadioButtons.add(partAnimMenuItem);
        animationSubMenu.add(partAnimMenuItem);

        fullAnimMenuItem = new JRadioButtonMenuItem("Program & data flow");
        fullAnimMenuItem.setMnemonic(KeyEvent.VK_D);
        fullAnimMenuItem.addActionListener(e -> animationCombo.setSelectedIndex(HackController.ANIMATION));
        animationRadioButtons.add(fullAnimMenuItem);
        animationSubMenu.add(fullAnimMenuItem);

        noAnimMenuItem = new JRadioButtonMenuItem("No Animation");
        noAnimMenuItem.setMnemonic(KeyEvent.VK_N);
        noAnimMenuItem.addActionListener(e -> animationCombo.setSelectedIndex(HackController.NO_DISPLAY_CHANGES));
        animationRadioButtons.add(noAnimMenuItem);
        animationSubMenu.add(noAnimMenuItem);


        ButtonGroup additionalDisplayRadioButtons = new ButtonGroup();

        additionalDisplaySubMenu = new JMenu("View");
        additionalDisplaySubMenu.setMnemonic(KeyEvent.VK_V);
        viewMenu.add(additionalDisplaySubMenu);

        scriptDisplayMenuItem = new JRadioButtonMenuItem("Script");
        scriptDisplayMenuItem.setMnemonic(KeyEvent.VK_S);
        scriptDisplayMenuItem.setSelected(true);
        scriptDisplayMenuItem.addActionListener(e -> additionalDisplayCombo
                .setSelectedIndex(HackController.SCRIPT_ADDITIONAL_DISPLAY));
        additionalDisplayRadioButtons.add(scriptDisplayMenuItem);
        additionalDisplaySubMenu.add(scriptDisplayMenuItem);

        outputMenuItem = new JRadioButtonMenuItem("Output");
        outputMenuItem.setMnemonic(KeyEvent.VK_O);
        outputMenuItem.addActionListener(e -> additionalDisplayCombo
                .setSelectedIndex(HackController.OUTPUT_ADDITIONAL_DISPLAY));
        additionalDisplayRadioButtons.add(outputMenuItem);
        additionalDisplaySubMenu.add(outputMenuItem);

        compareMenuItem = new JRadioButtonMenuItem("Compare");
        compareMenuItem.setMnemonic(KeyEvent.VK_C);
        compareMenuItem.addActionListener(e -> additionalDisplayCombo
                .setSelectedIndex(HackController.COMPARISON_ADDITIONAL_DISPLAY));
        additionalDisplayRadioButtons.add(compareMenuItem);
        additionalDisplaySubMenu.add(compareMenuItem);

        noAdditionalDisplayMenuItem = new JRadioButtonMenuItem("Screen");
        noAdditionalDisplayMenuItem.setMnemonic(KeyEvent.VK_N);
        noAdditionalDisplayMenuItem.addActionListener(e -> additionalDisplayCombo
                .setSelectedIndex(HackController.NO_ADDITIONAL_DISPLAY));
        additionalDisplayRadioButtons.add(noAdditionalDisplayMenuItem);
        additionalDisplaySubMenu.add(noAdditionalDisplayMenuItem);


        ButtonGroup formatRadioButtons = new ButtonGroup();

        numericFormatSubMenu = new JMenu("Format");
        numericFormatSubMenu.setMnemonic(KeyEvent.VK_F);
        viewMenu.add(numericFormatSubMenu);

        decMenuItem = new JRadioButtonMenuItem("Decimal");
        decMenuItem.setMnemonic(KeyEvent.VK_D);
        decMenuItem.setSelected(true);
        decMenuItem.addActionListener(e -> formatCombo.setSelectedIndex(HackController.DECIMAL_FORMAT));
        formatRadioButtons.add(decMenuItem);
        numericFormatSubMenu.add(decMenuItem);

        hexaMenuItem = new JRadioButtonMenuItem("Hexadecimal");
        hexaMenuItem.setMnemonic(KeyEvent.VK_H);
        hexaMenuItem.addActionListener(e -> formatCombo.setSelectedIndex(HackController.HEXA_FORMAT));
        formatRadioButtons.add(hexaMenuItem);
        numericFormatSubMenu.add(hexaMenuItem);

        binMenuItem = new JRadioButtonMenuItem("Binary");
        binMenuItem.setMnemonic(KeyEvent.VK_B);
        binMenuItem.addActionListener(e -> formatCombo.setSelectedIndex(HackController.BINARY_FORMAT));
        formatRadioButtons.add(binMenuItem);
        numericFormatSubMenu.add(binMenuItem);

        viewMenu.addSeparator();

        singleStepMenuItem = new JMenuItem("Single Step", KeyEvent.VK_S);
        singleStepMenuItem.setAccelerator(KeyStroke.getKeyStroke("F11"));
        singleStepMenuItem.addActionListener(e -> notifyControllerListeners(ControllerEvent.SINGLE_STEP, null));
        runMenu.add(singleStepMenuItem);

        ffwdMenuItem = new JMenuItem("Run", KeyEvent.VK_F);
        ffwdMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        ffwdMenuItem.addActionListener(e -> notifyControllerListeners(ControllerEvent.FAST_FORWARD, null));
        runMenu.add(ffwdMenuItem);

        stopMenuItem = new JMenuItem("Stop", KeyEvent.VK_T);
        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift F5"));
        stopMenuItem.addActionListener(e -> notifyControllerListeners(ControllerEvent.STOP, null));
        runMenu.add(stopMenuItem);


        rewindMenuItem = new JMenuItem("Reset", KeyEvent.VK_R);
        rewindMenuItem.addActionListener(e -> notifyControllerListeners(ControllerEvent.REWIND, null));
        runMenu.add(rewindMenuItem);

        runMenu.addSeparator();

        breakpointsMenuItem = new JMenuItem("Breakpoints", KeyEvent.VK_B);
        breakpointsMenuItem.addActionListener(e -> showBreakpoints());
        runMenu.add(breakpointsMenuItem);

        profilerMenuItem = new JMenuItem("Profiler", KeyEvent.VK_I);
        profilerMenuItem.addActionListener(e -> showProfiler());
        profilerMenuItem.setEnabled(false);
        runMenu.add(profilerMenuItem);

        usageMenuItem = new JMenuItem("Usage", KeyEvent.VK_U);
        usageMenuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        usageMenuItem.addActionListener(e -> {
            if (usageWindow != null)
                usageWindow.setVisible(true);
        });
        helpMenu.add(usageMenuItem);

        aboutMenuItem = new JMenuItem("About ...", KeyEvent.VK_A);
        aboutMenuItem.addActionListener(e -> {
            if (aboutWindow != null)
                aboutWindow.setVisible(true);
        });
        helpMenu.add(aboutMenuItem);

    }

    // called when the load script button is pressed.
    private void scriptPressed() {
        int returnVal = fileChooser.showDialog(this, "Load Script");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            notifyControllerListeners(ControllerEvent.SCRIPT_CHANGE, fileChooser.getSelectedFile().getAbsoluteFile());
            scriptComponent.setContents(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    // Initializes this component.
    private void jbInit() {
        fileChooser.setFileFilter(new ScriptFileFilter());
        this.getContentPane().setLayout(null);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        JLabel slowLabel = new JLabel("Slow");
        slowLabel.setFont(Utilities.thinLabelsFont);
        JLabel fastLabel = new JLabel("Fast");
        fastLabel.setFont(Utilities.thinLabelsFont);
        labelTable.put(1, slowLabel);
        labelTable.put(5, fastLabel);

        speedSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                int speed = source.getValue();
                notifyControllerListeners(ControllerEvent.SPEED_CHANGE, speed);
            }
        });
        speedSlider.setLabelTable(labelTable);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        speedSlider.setPreferredSize(new Dimension(95, 50));
        speedSlider.setMinimumSize(new Dimension(95, 50));
        speedSlider.setToolTipText("Speed");
        speedSlider.setMaximumSize(new Dimension(95, 50));

        final Dimension buttonSize = new Dimension(39, 39);

        loadProgramButton.addActionListener(e -> notifyControllerListeners(ControllerEvent.LOAD_PROGRAM, null));
        loadProgramButton.setMaximumSize(buttonSize);
        loadProgramButton.setMinimumSize(buttonSize);
        loadProgramButton.setPreferredSize(buttonSize);
        loadProgramButton.setSize(buttonSize);
        loadProgramButton.setToolTipText("Load Program");
        loadProgramButton.setIcon(loadProgramIcon);

        ffwdButton.setMaximumSize(buttonSize);
        ffwdButton.setMinimumSize(buttonSize);
        ffwdButton.setPreferredSize(buttonSize);
        ffwdButton.setToolTipText("Run");
        ffwdButton.setIcon(ffwdIcon);
        ffwdButton.addActionListener(e -> notifyControllerListeners(ControllerEvent.FAST_FORWARD, null));

        stopButton.addActionListener(e -> notifyControllerListeners(ControllerEvent.STOP, null));
        stopButton.setMaximumSize(buttonSize);
        stopButton.setMinimumSize(buttonSize);
        stopButton.setPreferredSize(buttonSize);
        stopButton.setToolTipText("Stop");
        stopButton.setIcon(stopIcon);

        rewindButton.setMaximumSize(buttonSize);
        rewindButton.setMinimumSize(buttonSize);
        rewindButton.setPreferredSize(buttonSize);
        rewindButton.setToolTipText("Reset");
        rewindButton.setIcon(rewindIcon);
        rewindButton.addActionListener(e -> notifyControllerListeners(ControllerEvent.REWIND, null));

        scriptButton.setMaximumSize(buttonSize);
        scriptButton.setMinimumSize(buttonSize);
        scriptButton.setPreferredSize(buttonSize);
        scriptButton.setToolTipText("Load Script");
        scriptButton.setIcon(scriptIcon);
        scriptButton.addActionListener(e -> scriptPressed());

        breakButton.addActionListener(e -> showBreakpoints());
        breakButton.setMaximumSize(buttonSize);
        breakButton.setMinimumSize(buttonSize);
        breakButton.setPreferredSize(buttonSize);
        breakButton.setToolTipText("Open breakpoint panel");
        breakButton.setIcon(breakIcon);

        breakpointWindow.addBreakpointListener(this);

        singleStepButton.addActionListener(e -> notifyControllerListeners(ControllerEvent.SINGLE_STEP, null));
        singleStepButton.setMaximumSize(buttonSize);
        singleStepButton.setMinimumSize(buttonSize);
        singleStepButton.setPreferredSize(buttonSize);
        singleStepButton.setSize(buttonSize);
        singleStepButton.setToolTipText("Single Step");
        singleStepButton.setIcon(singleStepIcon);

        stepOverButton.addActionListener(e -> notifyControllerListeners(ControllerEvent.STEP_OVER, null));
        stepOverButton.setMaximumSize(buttonSize);
        stepOverButton.setMinimumSize(buttonSize);
        stepOverButton.setPreferredSize(buttonSize);
        stepOverButton.setSize(buttonSize);
        stepOverButton.setToolTipText("Step Over");
        stepOverButton.setIcon(stepOverIcon);

        animationCombo.addActionListener(e -> {
            int selectedIndex = animationCombo.getSelectedIndex();
            switch (selectedIndex) {
                case HackController.DISPLAY_CHANGES:
                    if (!partAnimMenuItem.isSelected())
                        partAnimMenuItem.setSelected(true);
                    break;

                case HackController.ANIMATION:
                    if (!fullAnimMenuItem.isSelected())
                        fullAnimMenuItem.setSelected(true);
                    break;

                case HackController.NO_DISPLAY_CHANGES:
                    if (!noAnimMenuItem.isSelected())
                        noAnimMenuItem.setSelected(true);
                    break;
            }

            notifyControllerListeners(ControllerEvent.ANIMATION_MODE_CHANGE, selectedIndex);
        });

        formatCombo.addActionListener(e -> {
            int selectedIndex = formatCombo.getSelectedIndex();
            switch (selectedIndex) {
                case HackController.DECIMAL_FORMAT:
                    if (!decMenuItem.isSelected())
                        decMenuItem.setSelected(true);
                    break;

                case HackController.HEXA_FORMAT:
                    if (!hexaMenuItem.isSelected())
                        hexaMenuItem.setSelected(true);
                    break;

                case HackController.BINARY_FORMAT:
                    if (!binMenuItem.isSelected())
                        binMenuItem.setSelected(true);
                    break;
            }

            notifyControllerListeners(ControllerEvent.NUMERIC_FORMAT_CHANGE, selectedIndex);
        });

        additionalDisplayCombo.addActionListener(e -> {
            int selectedIndex = additionalDisplayCombo.getSelectedIndex();
            switch (selectedIndex) {
                case HackController.SCRIPT_ADDITIONAL_DISPLAY:
                    if (!scriptMenuItem.isSelected())
                        scriptMenuItem.setSelected(true);
                    break;

                case HackController.OUTPUT_ADDITIONAL_DISPLAY:
                    if (!outputMenuItem.isSelected())
                        outputMenuItem.setSelected(true);
                    break;

                case HackController.COMPARISON_ADDITIONAL_DISPLAY:
                    if (!compareMenuItem.isSelected())
                        compareMenuItem.setSelected(true);
                    break;

                case HackController.NO_ADDITIONAL_DISPLAY:
                    if (!noAdditionalDisplayMenuItem.isSelected())
                        noAdditionalDisplayMenuItem.setSelected(true);
                    break;
            }

            notifyControllerListeners(ControllerEvent.ADDITIONAL_DISPLAY_CHANGE, selectedIndex);
        });

        messageLbl.setFont(Utilities.statusLineFont);
        messageLbl.setBorder(BorderFactory.createLoweredBevelBorder());
        messageLbl.setBounds(new Rectangle(0, 667, CONTROLLER_WIDTH - 8, 25));

        toolBar = new JToolBar();
        toolBar.setSize(new Dimension(TOOLBAR_WIDTH, TOOLBAR_HEIGHT));
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
        toolBar.setFloatable(false);
        toolBar.setLocation(0, 0);
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        arrangeToolBar();
        this.getContentPane().add(toolBar, null);
        toolBar.revalidate();
        toolBar.repaint();
        repaint();

        // Creating the menu bar
        menuBar = new JMenuBar();
        arrangeMenu();
        setJMenuBar(menuBar);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().add(messageLbl, null);

        setControllerSize();

        // sets the frame to be visible.
        setVisible(true);
    }
}

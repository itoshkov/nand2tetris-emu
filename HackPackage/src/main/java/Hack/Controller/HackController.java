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

import Hack.Events.ProgramEvent;
import Hack.Events.ProgramEventListener;
import Hack.Utilities.Conversions;
import Hack.Utilities.Definitions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A Controller for HackSimulators. Executes scripts written in a special scripting language
 * that controls the features of the simulators.
 * Constructed with a GUI that enables the execution control of the script.
 */
public class HackController
 implements ControllerEventListener, ActionListener, ProgramEventListener {

    /**
     * The number of speed units.
     */
    public static final int NUMBER_OF_SPEED_UNITS = 5;

    /**
     * The speed function for data flow animation.
     */
    public static final float[] SPEED_FUNCTION = {0f, 0.35f, 0.63f, 0.87f, 1f};

    /**
     * The speed function for fast forward mode.
     */
    private static final int[] FASTFORWARD_SPEED_FUNCTION = {500, 1000, 2000, 4000, 15000};

    // ANIMATION MODES:

    /**
     * Animation mode: Specifies using static display changes - displays value changes staticaly
     */
    public static final int DISPLAY_CHANGES = 0;

    /**
     * Animation mode: Specifies using dynamic animation - fully animates value changes
     */
    public static final int ANIMATION = 1;

    /**
     * Animation mode: Specifies using no display changes.
     * In this mode, the speed has no meening.
     */
    public static final int NO_DISPLAY_CHANGES = 2;


    // NUMERIC FORMATS:

    /**
     * Decimal numeric format
     */
    public static final int DECIMAL_FORMAT = 0;

    /**
     * Hexadecimal numeric format
     */
    public static final int HEXA_FORMAT = 1;

    /**
     * Binary numeric format
     */
    public static final int BINARY_FORMAT = 2;


    // ADDITIONAL DISPLAYS

    /**
     * Specifies the additional display of the script file component.
     */
    public static final int SCRIPT_ADDITIONAL_DISPLAY = 0;

    /**
     * Specifies the additional display of the output file component.
     */
    public static final int OUTPUT_ADDITIONAL_DISPLAY = 1;

    /**
     * Specifies the additional display of the comparison file component.
     */
    public static final int COMPARISON_ADDITIONAL_DISPLAY = 2;

    /**
     * Specifies no additional display.
     */
    public static final int NO_ADDITIONAL_DISPLAY = 3;

    // Minimum and maximum mili-seconds per script command execution
    private static final int MAX_MS = 2500;
    private static final int MIN_MS = 25;

    // Initial speed unit
    private static final int INITIAL_SPEED_UNIT = 3;

    // A helper string with spaces
    private static final String SPACES = "                                        ";
    private static final String DIRECTORY = "directory";
    private static final String SPEED = "speed";
    private static final String ANIMATION_MODE = "animation_mode";
    private static final String NUMERIC_FORMAT = "numeric_format";
    private final Preferences preferences;

    // The controller's GUI
    protected ControllerGUI gui;

    // The file of the current script
    private File currentScriptFile;

    // The names of the output and comparison files
    private String currentOutputName;
    private String currentComparisonName;

    // The script commands
    private Script script;

    // The controlled simulator
    protected HackSimulator simulator;

    // The current speed unit.
    private int currentSpeedUnit;

    // The current animation mode.
    private int animationMode;

    // The program counter
    private int currentCommandIndex;

    // The output destination
    private PrintWriter output;

    // The comparison source
    private BufferedReader comparisonFile;

    // Index of repeat or while start command
    private int loopCommandIndex;

    // Number of repeats left
    private int repeatCounter;

    // The condition of the current while loop.
    private ScriptCondition whileCondition;

    // The current variable printing list
    private VariableFormat[] varList;

    // The current breakpoints list
    private final Set<Breakpoint> breakpoints = Collections.synchronizedSet(new LinkedHashSet<Breakpoint>());

    private final Set<Breakpoint> tempBreakpoints = Collections.synchronizedSet(new LinkedHashSet<Breakpoint>());

    // The current compared and output lines
    private int compareLinesCounter, outputLinesCounter;

    // times the fast forward process
    private Timer timer;

    // locked when single step in process
    protected boolean singleStepLocked;

    // The single step task object
    private SingleStepTask singleStepTask;

    // The fast forward task object
    private FastForwardTask fastForwardTask;

    // The set animation mode task object
    private SetAnimationModeTask setAnimationModeTask;

    // The set numeric format task object
    private SetNumericFormatTask setNumericFormatTask;

    // True if the system is in fast forward.
    private boolean fastForwardRunning;

    // True if the system is in Single Step.
    private boolean singleStepRunning;

    // True if the script ended.
    private boolean scriptEnded;

    // True if the program was halted.
    private boolean programHalted;

    // The speed delays.
    private int[] delays;

    // true if the comparison failed at some point in the script
    private boolean comparisonFailed;

    // The number of the line in which the comparison failed (if it failed).
    private int comparisonFailureLine;

    // The echo that was displayed (if any) when single step was stopped in the middle.
    private String lastEcho;

    // The default script file object
    private File defaultScriptFile;

    /**
     * Constructs a new HackController with the given script file name.
     * The script will be executed and the final result will be printed.
     */
    public HackController(HackSimulator simulator, String scriptFileName) {
        this.preferences = Preferences.userNodeForPackage(simulator.getClass());
        File file = new File(scriptFileName);
        if (!file.exists())
            displayMessage(scriptFileName + " doesn't exist", true);

        this.simulator = simulator;
        animationMode = NO_DISPLAY_CHANGES;
        simulator.setAnimationMode(animationMode);
        simulator.addListener(this);

        try {
            loadNewScript(file, false);
            saveWorkingDir(file);
        } catch (ScriptException | ControllerException se) {
            displayMessage(se.getMessage(), true);
        }

        fastForwardRunning = true;

        while (fastForwardRunning)
            singleStep();
    }

    /**
     * Constructs a new HackController with the given GUI component, hack simulator
     * and the default script file for this simulator.
     */
    public HackController(ControllerGUI gui, HackSimulator simulator, String defaultScriptName)
     throws ScriptException, ControllerException {

        this.preferences = Preferences.userNodeForPackage(simulator.getClass());
        this.gui = gui;
        this.simulator = simulator;
        singleStepTask = new SingleStepTask();
        fastForwardTask = new FastForwardTask();
        setAnimationModeTask = new SetAnimationModeTask();
        setNumericFormatTask = new SetNumericFormatTask();
        simulator.addListener(this);
        simulator.addProgramListener(this);

        defaultScriptFile = new File(defaultScriptName);
        loadNewScript(defaultScriptFile, false);

        delays = new int[NUMBER_OF_SPEED_UNITS];
        for (int i = 0; i < NUMBER_OF_SPEED_UNITS; i++)
            delays[i] = (int)(MAX_MS - SPEED_FUNCTION[i] * (float)(MAX_MS - MIN_MS));

        currentSpeedUnit = preferences.getInt(SPEED, INITIAL_SPEED_UNIT);
        animationMode = preferences.getInt(ANIMATION_MODE, simulator.getInitialAnimationMode());
        simulator.setAnimationMode(animationMode);
        simulator.setAnimationSpeed(currentSpeedUnit);
        final int numericFormat = preferences.getInt(NUMERIC_FORMAT, simulator.getInitialNumericFormat());
        simulator.setNumericFormat(numericFormat);
        timer = new Timer(delays[currentSpeedUnit - 1], this);

        // adds the simulator component to the controller component
        gui.setSimulator(simulator.getGUI());
        gui.setTitle(simulator.getName() + getVersionString());

        // load and set working dir
        File file = new File(preferences.get(DIRECTORY, "."));
        simulator.setWorkingDir(file);
        gui.setWorkingDir(file);

        gui.addControllerListener(this);
        gui.setSpeed(currentSpeedUnit);
        gui.setAnimationMode(animationMode);
        gui.setNumericFormat(numericFormat);
        gui.setAdditionalDisplay(simulator.getInitialAdditionalDisplay());
        gui.setVariables(simulator.getVariables());

        stopMode();

        gui.setProfiler(simulator.getProfiler());

        simulator.prepareGUI(); // prepares the gui after it is displayed
    }

    // Restarts the current script from the beginning.
    private void rewind() {
        try {
            if (scriptEnded || programHalted) {
                gui.enableSingleStep();
                gui.enableFastForward();
            }
            scriptEnded = false;
            programHalted = false;

            int oldAnimationMode = animationMode;
            setAnimationMode(DISPLAY_CHANGES);
            simulator.restart();
            refreshSimulator();
            setAnimationMode(oldAnimationMode);

            if (output != null)
                resetOutputFile();
            if (comparisonFile != null)
                resetComparisonFile();

            lastEcho = "";
            currentCommandIndex = 0;
            gui.setCurrentScriptLine(script.getLineNumberAt(0));

        } catch (ControllerException e) {
            displayMessage(e.getMessage(), true);
        }
    }

    // Puts the controller into stop mode
    private void stopMode() {
        if (fastForwardRunning) {
            if (gui != null) {
                timer.stop();
                gui.enableLoadProgram();
                gui.enableSpeedSlider();
            }
            fastForwardRunning = false;
        }
        singleStepRunning = false;

        if (gui != null) {
            gui.enableSingleStep();
            gui.enableFastForward();
            gui.enableScript();
            gui.enableRewind();
            gui.disableStop();
            gui.enableAnimationModes();

            if (animationMode == NO_DISPLAY_CHANGES)
                gui.setCurrentScriptLine(script.getLineNumberAt(currentCommandIndex));

            refreshSimulator();
        }
    }

    // Executes all the script unless a breakpoint, comparison failure, stop flag
    private void fastForward() {
        gui.enableStop();
        gui.disableSingleStep();
        gui.disableRewind();
        gui.disableScript();
        gui.disableFastForward();
        gui.disableAnimationModes();
        gui.disableLoadProgram();

        fastForwardRunning = true;
        simulator.prepareFastForward();

        if (animationMode != NO_DISPLAY_CHANGES)
            timer.start();
        else {
            displayMessage("Running...", false);
            gui.disableSpeedSlider();
            Thread t = new Thread(fastForwardTask);
            t.start();
        }
    }

    // Executes a single step from the script, checks for a breakpoint and
    // sets the status of the system accordingly.
    private synchronized void singleStep() {

        singleStepLocked = true;

        try {
            byte terminatorType;
            singleStepRunning = true;

            do {
                terminatorType = miniStep();
            } while (terminatorType == Command.MINI_STEP_TERMINATOR && singleStepRunning);

            singleStepRunning = false;

            if (terminatorType == Command.STOP_TERMINATOR) {
                displayMessage("Script reached a '!' terminator", false);
                stopMode();
            }

            boolean breakpointReached = false;

            // Check Breakpoints
            for (Breakpoint breakpoint : breakpoints) {
                String currentValue = simulator.getValue(breakpoint.getVarName());
                if (currentValue.equals(breakpoint.getValue())) {
                    // if value is equal and the breakpoint wasn't reached before, turn it on
                    if (!breakpoint.isReached()) {
                        breakpointReached = true;
                        breakpoint.on();
                        gui.setBreakpoints(breakpoints);
                        displayMessage("Breakpoint reached", false);
                        gui.showBreakpoints();
                        stopMode();
                    }
                }
                // if the value is not equal and the breakpoint was reached before, turn it off
                else if (breakpoint.isReached()) {
                    breakpoint.off();
                    gui.setBreakpoints(breakpoints);
                }
            }

            // Check temp breakpoints
            if (!breakpointReached)
                for (Breakpoint breakpoint : tempBreakpoints) {
                    String currentValue = simulator.getValue(breakpoint.getVarName());
                    if (currentValue.equals(breakpoint.getValue())) {
                        breakpointReached = true;
                        stopMode();
                    }
                }

            if (breakpointReached)
                tempBreakpoints.clear();

        } catch (ControllerException | ProgramException | CommandException | VariableException ce) {
            stopWithError(ce);
        }

        singleStepLocked = false;
        notifyAll();
    }

    // Displays the message of the given exception and stops the script's execution.
    private void stopWithError(Exception e) {
        displayMessage(e.getMessage(), true);
        stopMode();
    }

    // Executes one command from the script and advances to the next.
    // Returns the command's terminator.
    private byte miniStep()
     throws ControllerException, ProgramException, CommandException, VariableException {
        Command command;
        boolean redo;

        do {
            command = script.getCommandAt(currentCommandIndex);
            redo = false;

            switch (command.getCode()) {
            case Command.SIMULATOR_COMMAND:
                simulator.doCommand((String[])command.getArg());
                break;
            case Command.OUTPUT_FILE_COMMAND:
                doOutputFileCommand(command);
                break;
            case Command.COMPARE_TO_COMMAND:
                doCompareToCommand(command);
                break;
            case Command.OUTPUT_LIST_COMMAND:
                doOutputListCommand(command);
                break;
            case Command.OUTPUT_COMMAND:
                doOutputCommand();
                break;
            case Command.ECHO_COMMAND:
                doEchoCommand(command);
                break;
            case Command.CLEAR_ECHO_COMMAND:
                doClearEchoCommand();
                break;
            case Command.BREAKPOINT_COMMAND:
                doBreakpointCommand(command);
                break;
            case Command.CLEAR_BREAKPOINTS_COMMAND:
                doClearBreakpointsCommand();
                break;
            case Command.REPEAT_COMMAND:
                repeatCounter = (Integer) command.getArg();
                loopCommandIndex = currentCommandIndex + 1;
                redo = true;
                break;
            case Command.WHILE_COMMAND:
                whileCondition = (ScriptCondition)command.getArg();
                loopCommandIndex = currentCommandIndex + 1;
                if (!whileCondition.compare(simulator)) {
                    // advance till the nearest end while command.
                    while (script.getCommandAt(currentCommandIndex).getCode() != Command.END_WHILE_COMMAND)
                        currentCommandIndex++;
				}
                redo = true; // whether the test was successful or not,
							 // the while command doesn't count
                break;
            case Command.END_SCRIPT_COMMAND:
                scriptEnded = true;
                stopMode();

                if (gui != null) {
                    gui.disableSingleStep();
                    gui.disableFastForward();
                }

                try {
                    if (output != null)
                        output.close();

                    if (comparisonFile != null) {
                        if (comparisonFailed)
                            displayMessage("End of script - Comparison failure at line "
                                               + comparisonFailureLine, true);
                        else
                            displayMessage("End of script - Comparison ended successfully",
                                               false);

                        comparisonFile.close();
                    }
                    else
                        displayMessage("End of script", false);
                } catch (IOException ioe) {
                    throw new ControllerException("Could not read comparison file");
                }

                break;
            }

            // advance script line pointer
            if (command.getCode() != Command.END_SCRIPT_COMMAND) {
                currentCommandIndex++;
                Command nextCommand = script.getCommandAt(currentCommandIndex);
                if (nextCommand.getCode() == Command.END_REPEAT_COMMAND) {
                    if (repeatCounter == 0 || --repeatCounter > 0)
                        currentCommandIndex = loopCommandIndex;
                    else
                        currentCommandIndex++;
                }
                else if (nextCommand.getCode() == Command.END_WHILE_COMMAND) {
                    if (whileCondition.compare(simulator))
                        currentCommandIndex = loopCommandIndex;
                    else
                        currentCommandIndex++;
                }

                if (animationMode != NO_DISPLAY_CHANGES)
                    gui.setCurrentScriptLine(script.getLineNumberAt(currentCommandIndex));
            }

        } while (redo);

        return command.getTerminator();
    }

    // Executes the controller's output-file command.
    private void doOutputFileCommand(Command command) throws ControllerException {
        currentOutputName = currentScriptFile.getParent() + "/" + command.getArg();
        resetOutputFile();
        if (gui != null)
            gui.setOutputFile(currentOutputName);
    }

    // Executes the controller's compare-to command.
    private void doCompareToCommand(Command command) throws ControllerException {
        currentComparisonName = currentScriptFile.getParent() + "/" + command.getArg();
        resetComparisonFile();
        if (gui != null)
            gui.setComparisonFile(currentComparisonName);
    }

    // Executes the controller's output-list command.
    private void doOutputListCommand(Command command) throws ControllerException {
        if (output == null)
            throw new ControllerException("No output file specified");

        varList = (VariableFormat[])command.getArg();
        StringBuilder line = new StringBuilder("|");

        for (VariableFormat aVarList : varList) {
            int space = aVarList.padL + aVarList.padR + aVarList.len;
            String varName = aVarList.varName.length() > space ?
                    aVarList.varName.substring(0, space) : aVarList.varName;
            int leftSpace = (space - varName.length()) / 2;
            int rightSpace = space - leftSpace - varName.length();

            line.append(SPACES.substring(0, leftSpace)).append(varName)
                    .append(SPACES.substring(0, rightSpace)).append('|');
        }

        outputAndCompare(line.toString());
    }

    // Executes the controller's output command.
    private void doOutputCommand() throws ControllerException, VariableException {
        if (output == null)
            throw new ControllerException("No output file specified");

        StringBuilder line = new StringBuilder("|");

        for (VariableFormat aVarList : varList) {
            // find value string (convert to require format if necessary)
            String value = simulator.getValue(aVarList.varName);
            if (aVarList.format != VariableFormat.STRING_FORMAT) {
                int numValue;
                try {
                    numValue = Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    throw new VariableException("Variable is not numeric", aVarList.varName);
                }
                if (aVarList.format == VariableFormat.HEX_FORMAT)
                    value = Conversions.decimalToHex(numValue, 4);
                else if (aVarList.format == VariableFormat.BINARY_FORMAT)
                    value = Conversions.decimalToBinary(numValue, 16);
            }

            if (value.length() > aVarList.len)
                value = value.substring(value.length() - aVarList.len);

            int leftSpace = aVarList.padL +
                    (aVarList.format == VariableFormat.STRING_FORMAT ?
                            0 : (aVarList.len - value.length()));
            int rightSpace = aVarList.padR +
                    (aVarList.format == VariableFormat.STRING_FORMAT ?
                            (aVarList.len - value.length()) : 0);
            line.append(SPACES.substring(0, leftSpace)).append(value)
                    .append(SPACES.substring(0, rightSpace)).append('|');
        }

        outputAndCompare(line.toString());
    }

    // Executes the controller's echo command.
    private void doEchoCommand(Command command) throws ControllerException {
        lastEcho = (String)command.getArg();
        if (gui != null)
            gui.displayMessage(lastEcho, false);
    }

    // Executes the controller's Clear-echo command.
    private void doClearEchoCommand() throws ControllerException {
        lastEcho = "";
        if (gui != null)
            gui.displayMessage("", false);
    }

    // Executes the controller's breakpoint command.
    private void doBreakpointCommand(Command command) throws ControllerException {
        Breakpoint breakpoint = (Breakpoint)command.getArg();

        if (breakpoints.add(breakpoint))
            gui.setBreakpoints(breakpoints);
    }

    // Executes the controller's clear-breakpoints command.
    private void doClearBreakpointsCommand() throws ControllerException {
        breakpoints.clear();
        gui.setBreakpoints(breakpoints);
    }

    // Compares an output line with a template line from a compare file.
    // The template must match exactly except for '*' which may match any
    // single character.
    private static boolean compareLineWithTemplate(String out, String cmp) {
        if (out.length() != cmp.length()) {
            return false;
        }
        StringCharacterIterator outi = new StringCharacterIterator(out);
        StringCharacterIterator cmpi = new StringCharacterIterator(cmp);
        for (outi.first(), cmpi.first();
             outi.current() != CharacterIterator.DONE;
             outi.next(), cmpi.next()) {
            if (cmpi.current() != '*' && outi.current() != cmpi.current()) {
                return false;
            }
        }
        return true;
    }

    // Ouputs the given line into the output file and compares it to the current
    // compare file (if exists)
    private void outputAndCompare(String line) throws ControllerException {
        output.println(line);
        output.flush();

        if (gui != null) {
            gui.outputFileUpdated();
            gui.setCurrentOutputLine(outputLinesCounter);
        }

        outputLinesCounter++;

        if (comparisonFile != null) {
            try {
                String compareLine = comparisonFile.readLine();

                if (gui != null)
                    gui.setCurrentComparisonLine(compareLinesCounter);

                compareLinesCounter++;

                if (!compareLineWithTemplate(line, compareLine)) {
                    comparisonFailed = true;
                    comparisonFailureLine = compareLinesCounter;
                    displayMessage("Comparison failure at line " + comparisonFailureLine,
                                       true);
                    stopMode();
                }
            } catch (IOException ioe) {
                throw new ControllerException("Could not read comparison file");
            }
        }
    }

    // loads the given script file and restarts the GUI.
    private void loadNewScript(File file, boolean displayMessage)
     throws ControllerException, ScriptException {
        currentScriptFile = file;
        script = new Script(file.getPath());
        breakpoints.clear();
        currentCommandIndex = 0;
        output = null;
        currentOutputName = "";
        comparisonFile = null;
        currentComparisonName = "";

        if (gui != null) {
            gui.setOutputFile("");
            gui.setComparisonFile("");
            gui.setBreakpoints(breakpoints);
            gui.setScriptFile(file.getPath());
            gui.setCurrentScriptLine(script.getLineNumberAt(0));
        }

        if (displayMessage)
            displayMessage("New script loaded: " + file.getPath(), false);
    }

    // Resets the output file.
    private void resetOutputFile() throws ControllerException {
        try {
            output = new PrintWriter(new FileWriter(currentOutputName));
            outputLinesCounter = 0;
            if (gui != null)
                gui.setCurrentOutputLine(-1);
        } catch (IOException ioe) {
            throw new ControllerException("Could not create output file " + currentOutputName);
        }

        if (gui != null)
            gui.setOutputFile(currentOutputName);
    }

    // Resets the comparison file.
    private void resetComparisonFile() throws ControllerException {
        try {
            comparisonFile = new BufferedReader(new FileReader(currentComparisonName));
            compareLinesCounter = 0;
            comparisonFailed = false;
            if (gui != null)
                gui.setCurrentComparisonLine(-1);
        } catch (IOException ioe) {
            throw new ControllerException("Could not open comparison file " +
                                          currentComparisonName);
        }
    }

    // Sets the speed delay according to the given speed unit.
    private void setSpeed(int newSpeedUnit) {
        currentSpeedUnit = newSpeedUnit;
        timer.setDelay(delays[currentSpeedUnit - 1]);
        simulator.setAnimationSpeed(newSpeedUnit);
        preferences.putInt(SPEED, newSpeedUnit);
        savePreferences();
    }

    // Sets the animation mode with the given one.
    private void setAnimationMode(int newAnimationMode) {
        simulator.setAnimationMode(newAnimationMode);

        if (animationMode == NO_DISPLAY_CHANGES && newAnimationMode != NO_DISPLAY_CHANGES) {
            simulator.refresh();
            gui.setCurrentScriptLine(script.getLineNumberAt(currentCommandIndex));
        }

        gui.setAnimationMode(newAnimationMode);
        animationMode = newAnimationMode;
        preferences.putInt(ANIMATION_MODE, newAnimationMode);
        savePreferences();
    }

    // Sets the numeric format with the given code.
    private void setNumericFormat(int formatCode) {
        simulator.setNumericFormat(formatCode);
        gui.setNumericFormat(formatCode);
        preferences.putInt(NUMERIC_FORMAT, formatCode);
        savePreferences();
    }

    // Sets the additional display with the given code.
    private void setAdditionalDisplay(int additionalDisplayCode) {

        switch (additionalDisplayCode) {
            case NO_ADDITIONAL_DISPLAY:
                simulator.getGUI().setAdditionalDisplay(null);
                break;
            case SCRIPT_ADDITIONAL_DISPLAY:
                simulator.getGUI().setAdditionalDisplay(gui.getScriptComponent());
                break;
            case OUTPUT_ADDITIONAL_DISPLAY:
                simulator.getGUI().setAdditionalDisplay(gui.getOutputComponent());
                break;
            case COMPARISON_ADDITIONAL_DISPLAY:
                simulator.getGUI().setAdditionalDisplay(gui.getComparisonComponent());
                break;
        }

        gui.setAdditionalDisplay(additionalDisplayCode);
    }

    // Sets the breakpoints list with the given one.
    private void setBreakpoints(Vector<Breakpoint> newBreakpoints) {
        breakpoints.clear();
        breakpoints.addAll(newBreakpoints);
    }

    // Refreshes the simulator display
    private void refreshSimulator() {
        if (animationMode == NO_DISPLAY_CHANGES) {
            simulator.setAnimationMode(DISPLAY_CHANGES);
            simulator.refresh();
            simulator.setAnimationMode(NO_DISPLAY_CHANGES);
        }
    }

    // Displays the given message with the given type (error or not)
    private void displayMessage(String message, boolean error) {
        if (gui != null)
            gui.displayMessage(message, error);
        else {
            if (error) {
                System.err.println(message);
                System.exit(-1);
            }
            else {
                System.out.println(message);
            }
        }
    }

    // Saves the given working dir into the data file and gui's.
    private void saveWorkingDir(File file) {
        final File parent = file.getParentFile();

        if (gui != null)
            gui.setWorkingDir(parent);

        simulator.setWorkingDir(file);

        final File dir = file.isDirectory() ? file : parent;

        preferences.put(DIRECTORY, dir.toString());
        savePreferences();
    }

    private void savePreferences() {
        try {
            preferences.sync();
        } catch (BackingStoreException ignored) {
        }
    }

    // Returns the version string
    private static String getVersionString() {
        return " (" + Definitions.version + ")";
    }

    // load default script file (if not already loaded)
    // and switches to Screen display
    protected void reloadDefaultScript() {
        if (!currentScriptFile.equals(defaultScriptFile)) {
            gui.setAdditionalDisplay(NO_ADDITIONAL_DISPLAY);
            try {
                loadNewScript(defaultScriptFile, false);
                rewind();
            } catch (ScriptException | ControllerException ignored) {
            }
        }
    }

    // Updates the current program file name in the gui's title and saves its dir
    // as the current working dir.
    protected void updateProgramFile(String programFileName) {
        gui.setTitle(simulator.getName() + getVersionString()+ " - " + programFileName);
        File file = new File(programFileName);
        saveWorkingDir(file);
    }

    public void actionPerformed(ActionEvent e) {
        if (!singleStepLocked) {
            Thread t = new Thread(singleStepTask);
            t.start();
        }
    }

    public void programChanged(ProgramEvent event) {
        switch (event.getType()) {
            case ProgramEvent.SAVE:
                updateProgramFile(event.getProgramFileName());
                break;
            case ProgramEvent.LOAD:
                updateProgramFile(event.getProgramFileName());
                if (!singleStepLocked) // new program was loaded manually
                    reloadDefaultScript();
                break;
            case ProgramEvent.CLEAR:
                gui.setTitle(simulator.getName() + getVersionString());
                break;
        }
    }

    public void actionPerformed(ControllerEvent event) {
        try {
            switch (event.getAction()) {
                case ControllerEvent.STEP_OVER:
                    final Breakpoint stepOverBreakpoint = simulator.genStepOverBreakpoint();
                    if (stepOverBreakpoint != null) {
                        tempBreakpoints.add(stepOverBreakpoint);
                        displayMessage(lastEcho, true);
                        fastForward();
                        break;
                    }
                    // Otherwise fallback to SINGLE_STEP
                case ControllerEvent.SINGLE_STEP:
                    displayMessage(lastEcho, true);
                    gui.disableSingleStep();
                    gui.disableFastForward();
                    gui.disableScript();
                    gui.disableRewind();
                    gui.enableStop();
                    Thread t = new Thread(singleStepTask);
                    t.start();
                    break;
                case ControllerEvent.FAST_FORWARD:
                    displayMessage(lastEcho, true);
                    fastForward();
                    break;
                case ControllerEvent.STOP:
                    if (animationMode == NO_DISPLAY_CHANGES)
                        displayMessage("", false);
                    stopMode();
                    break;
                case ControllerEvent.REWIND:
                    displayMessage("Script restarted", false);
                    rewind();
                    break;
                case ControllerEvent.SPEED_CHANGE:
                    setSpeed((Integer) event.getData());
                    break;
                case ControllerEvent.BREAKPOINTS_CHANGE:
                    //noinspection unchecked
                    setBreakpoints((Vector<Breakpoint>) event.getData());
                    break;
                case ControllerEvent.SCRIPT_CHANGE:
                    File file = (File)event.getData();
                    loadNewScript(file, true);
                    setAdditionalDisplay(SCRIPT_ADDITIONAL_DISPLAY);
                    saveWorkingDir(file);
                    rewind();
                    break;
                case ControllerEvent.ANIMATION_MODE_CHANGE:
                    setAnimationModeTask.setMode((Integer) event.getData());
                    t = new Thread(setAnimationModeTask);
                    t.start();
                    break;
                case ControllerEvent.NUMERIC_FORMAT_CHANGE:
                    setNumericFormatTask.setFormat((Integer) event.getData());
                    t = new Thread(setNumericFormatTask);
                    t.start();
                    break;
                case ControllerEvent.ADDITIONAL_DISPLAY_CHANGE:
                    setAdditionalDisplay((Integer) event.getData());
                    break;
                case ControllerEvent.DISABLE_ANIMATION_MODE_CHANGE:
                    gui.disableAnimationModes();
                    break;
                case ControllerEvent.ENABLE_ANIMATION_MODE_CHANGE:
                    gui.enableAnimationModes();
                    break;
                case ControllerEvent.DISABLE_SINGLE_STEP:
                    gui.disableSingleStep();
                    break;
                case ControllerEvent.ENABLE_SINGLE_STEP:
                    gui.enableSingleStep();
                    break;
                case ControllerEvent.DISABLE_FAST_FORWARD:
                    gui.disableFastForward();
                    break;
                case ControllerEvent.ENABLE_FAST_FORWARD:
                    gui.enableFastForward();
                    break;
                case ControllerEvent.LOAD_PROGRAM:
                    simulator.loadProgram();
                    break;
                case ControllerEvent.HALT_PROGRAM:
                    displayMessage("End of program", false);
                    programHalted = true;
                    if (fastForwardRunning)
                        stopMode();
                    gui.disableSingleStep();
                    gui.disableFastForward();
                    break;
                case ControllerEvent.CONTINUE_PROGRAM:
                    if (programHalted) {
                        programHalted = false;
                        gui.enableSingleStep();
                        gui.enableFastForward();
                    }
                    break;
                case ControllerEvent.DISABLE_MOVEMENT:
                    gui.disableSingleStep();
                    gui.disableFastForward();
                    gui.disableRewind();
                    break;
                case ControllerEvent.ENABLE_MOVEMENT:
                    gui.enableSingleStep();
                    gui.enableFastForward();
                    gui.enableRewind();
                    break;
                case ControllerEvent.DISPLAY_MESSAGE:
                    displayMessage((String)event.getData(), false);
                    break;
                case ControllerEvent.DISPLAY_ERROR_MESSAGE:
                    if (timer.isRunning())
                        stopMode();
                    displayMessage((String)event.getData(), true);
                    break;
                default:
                    doUnknownAction(event.getAction(), event.getData());
                    break;
            }
        } catch (ScriptException | ControllerException e) {
            displayMessage(e.getMessage(), true);
            stopMode();
        }
    }

    /**
     * Executes an unknown controller action event.
     */
    protected void doUnknownAction(byte action, Object data) throws ControllerException {
    }

    // Performs the single step task
    private class SingleStepTask implements Runnable {

        public void run() {
            singleStep();

            if (!fastForwardRunning) {
                if (!scriptEnded && !programHalted) {
                    gui.enableSingleStep();
                    gui.enableFastForward();
                    gui.disableStop();
                }
                gui.enableScript();
                gui.enableRewind();
            }

            if (animationMode == NO_DISPLAY_CHANGES) {
                refreshSimulator();
                gui.setCurrentScriptLine(script.getLineNumberAt(currentCommandIndex));
            }
        }
    }

    // Performs the fast forward task
    private class FastForwardTask implements Runnable {
        public synchronized void run() {
            try {
                System.runFinalization();
                System.gc();
                wait(300);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            int count = 0;
            int rounds = FASTFORWARD_SPEED_FUNCTION[currentSpeedUnit - 1];

            while (fastForwardRunning) {
                singleStep();

                // waits for 1 ms each constant amount of commands
                if (count == rounds) {
                    count = 0;
                    try {
                        wait(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

                count++;
            }
        }
    }

    // Sets the animation mode
    private class SetAnimationModeTask implements Runnable {

        private int animationMode;

        public void setMode(int animationMode) {
            this.animationMode = animationMode;
        }

        public void run() {
            setAnimationMode(animationMode);
        }
    }

    // Sets the numeric format
    private class SetNumericFormatTask implements Runnable {

        private int numericFormat;

        public void setFormat(int numericFormat) {
            this.numericFormat = numericFormat;
        }

        public void run() {
            setNumericFormat(numericFormat);
        }
    }
}

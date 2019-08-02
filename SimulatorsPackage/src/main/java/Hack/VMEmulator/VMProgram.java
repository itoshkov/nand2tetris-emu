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

import Hack.ComputerParts.ComputerPartGUI;
import Hack.ComputerParts.InteractiveComputerPart;
import Hack.Controller.ProgramException;
import Hack.Events.ProgramEvent;
import Hack.Events.ProgramEventListener;
import Hack.Utilities.Definitions;
import Hack.Utilities.HackFileFilter;
import Hack.VirtualMachine.HVMInstructionSet;

import java.io.*;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A list of VM instructions, with a program counter.
 */
class VMProgram extends InteractiveComputerPart implements ProgramEventListener {

	// pseudo address for returning to built-in functions
	static final short BUILTIN_FUNCTION_ADDRESS = -1;

	// Possible values for the current status - has the user allowed
	// access to built-in vm functions?
	private static final int BUILTIN_ACCESS_UNDECIDED = 0;
	private static final int BUILTIN_ACCESS_AUTHORIZED = 1;
	private static final int BUILTIN_ACCESS_DENIED = 2;

    // listeners to program changes
    private Vector<ProgramEventListener> listeners;

    // The list of VM instructions
    private VMEmulatorInstruction[] instructions;
	private int instructionsLength;
	private int visibleInstructionsLength;

    // The program counter - points to the next instruction that should be executed.
    private short nextPC;
    private short currentPC;
    private short prevPC;

    // The gui of the program.
    private VMProgramGUI gui;

    // The address of the initial instruction
    private short startAddress;

    // Mapping from file names to an array of two elements, containing the start and
    // end addresses of the corresponding static segment.
    private Hashtable<String, Object> staticRange;

	// Addresses of functions by name
    private Hashtable<String, Short> functions;
    private short infiniteLoopForBuiltInsAddress;

    // The largest static variable index found in the current file.
    private int largestStaticIndex;

	// Has the user allowed access to built-in vm functions?
	private int builtInAccessStatus;

	// Is the program currently being read in the middle of a /* */ comment?
	private boolean isSlashStar;

    /**
     * Constructs a new empty program with the given GUI.
     */
    VMProgram(VMProgramGUI gui) {
        super(gui != null);
        this.gui = gui;
        listeners = new Vector<>();
        staticRange = new Hashtable<>();
        functions = new Hashtable<>();

        if (hasGUI) {
            assert gui != null;
            gui.addProgramListener(this);
            gui.addErrorListener(this);
        }

        reset();
    }

    /**
     * Creates a vm program. If the given file is a dir, creates a program composed of the vm
     * files in the dir.
     * The vm files are scanned twice: in the first scan a symbol table (that maps
     * function & label names into addresses) is built. In the second scan, the instructions
     * array is built.
     * Throws ProgramException if an error occurs while loading the program.
     */
    public void loadProgram(String fileName) throws ProgramException {
        File file = new File(fileName);
        if (!file.exists())
            throw new ProgramException("cannot find " + fileName);

        File[] files;

        if (file.isDirectory()) {
            files = file.listFiles(new HackFileFilter(".vm"));
            if (files == null || files.length == 0)
                throw new ProgramException("No vm files found in " + fileName);
        }
        else
            files = new File[]{file};

        if (displayChanges)
            gui.showMessage("Loading...");

        // First scan
		staticRange.clear();
		functions.clear();
		builtInAccessStatus = BUILTIN_ACCESS_UNDECIDED;
        Hashtable<String, Short> symbols = new Hashtable<>();
        nextPC = 0;
        for (File f : files) {
            String name = f.getName();
            String className = fileNameToClassName(name);
            // put some dummy into static range - just to tell the function
            // getAddress in the second pass which classes exist
            staticRange.put(className, true);
            try {
                updateSymbolTable(f, symbols, functions);
            } catch (ProgramException pe) {
                if (displayChanges)
                    gui.hideMessage();
                throw new ProgramException(name + ": " + pe.getMessage());
            }
        }
		boolean addCallBuiltInSysInit = false;
		if ((file.isDirectory() || symbols.get("Main.main") != null) &&
			symbols.get("Sys.init") == null) {
			// If the program is in multiple files or there's a Main.main
			// function it is assumed that it should be run by calling Sys.init.
			// If no Sys.init is found, add an invisible line with a call
			// to Sys.init to start on - the builtin version will be called.
			addCallBuiltInSysInit = true;
			getAddress("Sys.init"); // confirm calling the built-in Sys.init
			++nextPC; // A "call Sys.init 0" line will be added
		}

        instructions = new VMEmulatorInstruction[nextPC+4];

        // Second scan
        nextPC = 0;
        int currentStaticIndex = Definitions.VAR_START_ADDRESS;
        for (File f : files) {
            String name = f.getName();
            String className = fileNameToClassName(name);

            largestStaticIndex = -1;
            int[] range = new int[2];
            range[0] = currentStaticIndex;

            try {
                // functions is not passed as an argument since it is accessed
                // through getAddress()
                buildProgram(f, symbols);
            } catch (ProgramException pe) {
                if (displayChanges)
                    gui.hideMessage();
                throw new ProgramException(name + ": " + pe.getMessage());
            }

            currentStaticIndex += largestStaticIndex + 1;
            range[1] = currentStaticIndex - 1;
            staticRange.put(className, range);
        }
		instructionsLength = visibleInstructionsLength = nextPC;
		if (builtInAccessStatus == BUILTIN_ACCESS_AUTHORIZED) {
			// Add some "invisible" code in the end to make everything work
			instructionsLength += 4;
			if (addCallBuiltInSysInit) {
				instructionsLength += 1;
			}
			short indexInInvisibleCode = 0;
			// Add a jump to the end (no-one should get here since
			// both calls to built-in functions indicate that
			// that this is a function-based program and not a script
			// a-la proj7, but just to be on the safe side...).
			instructions[nextPC] =
				new VMEmulatorInstruction(HVMInstructionSet.GOTO_CODE,
										  (short)instructionsLength,
										  indexInInvisibleCode);
			instructions[nextPC].setStringArg("afterInvisibleCode");
			nextPC++;
			// Add a small infinite loop for built-in
			// methods to call (for example when Sys.halt is
			// called it must call a non-built-in infinite loop
			// because otherwise the current script would not
			// finish running - a problem for the OS tests.
			instructions[nextPC] =
				new VMEmulatorInstruction(HVMInstructionSet.LABEL_CODE,
										  (short)-1);
			instructions[nextPC].setStringArg("infiniteLoopForBuiltIns");
			nextPC++;
			infiniteLoopForBuiltInsAddress = nextPC;
			instructions[nextPC] =
				new VMEmulatorInstruction(HVMInstructionSet.GOTO_CODE,
										  nextPC, ++indexInInvisibleCode);
			instructions[nextPC].setStringArg("infiniteLoopForBuiltIns");
			nextPC++;
			if (addCallBuiltInSysInit) { // Add a call to the built-in Sys.init
				instructions[nextPC] =
					new VMEmulatorInstruction(HVMInstructionSet.CALL_CODE,
											  getAddress("Sys.init"), (short)0,
											  ++indexInInvisibleCode);
				instructions[nextPC].setStringArg("Sys.init");
				startAddress = nextPC;
				nextPC++;
			}
			// Add the label that the first invisible code line jumps to
			instructions[nextPC] =
				new VMEmulatorInstruction(HVMInstructionSet.LABEL_CODE,
										  (short)-1);
			instructions[nextPC].setStringArg("afterInvisibleCode");
			nextPC++;
		}

		if (!addCallBuiltInSysInit) {
            Short sysInitAddress = symbols.get("Sys.init");
            if (sysInitAddress == null) // Single file, no Sys.init - start at 0
                startAddress = 0;
            else // Implemented Sys.init - start there
                startAddress = sysInitAddress;
        }

        if (displayChanges)
            gui.hideMessage();

		nextPC = startAddress;
        setGUIContents();

        notifyProgramListeners(ProgramEvent.LOAD, fileName);
    }

    private String fileNameToClassName(String name) throws ProgramException {
        final int index = name.indexOf(".");
        if (index == -1)
            throw new ProgramException("File name without extension: " + name);

        return name.substring(0, index);
    }

    private String functionNameToClassName(String name) throws ProgramException {
        final int index = name.indexOf(".");
        if (index == -1)
            throw new ProgramException("Incorrect function name. Should be <ClassName>.<FunctionName>: " + name);

        return name.substring(0, index);
    }

    // Scans the given file and creates symbols for its functions & label names.
    private void updateSymbolTable(File file, Hashtable<String, Short> symbols, Hashtable<String, Short> functions) throws ProgramException {
        final BufferedReader reader = openFile(file);

        String line;
        String currentFunction = null;
        String label;
        int lineNumber = 0;

		isSlashStar = false;
        try {
            while ((line = unCommentLine(reader.readLine())) != null) {
                lineNumber++;
                if (!line.trim().equals("")) {
                    if (line.startsWith("function ")) {
                        StringTokenizer tokenizer = new StringTokenizer(line);
                        tokenizer.nextToken();
                        currentFunction = tokenizer.nextToken();
                        if (symbols.containsKey(currentFunction))
                            throw new ProgramException("subroutine " + currentFunction +
                                                       " already exists");
                        functions.put(currentFunction, nextPC);
                        symbols.put(currentFunction, nextPC);
                    }
                    else if (line.startsWith("label ")) {
                        StringTokenizer tokenizer = new StringTokenizer(line);
                        tokenizer.nextToken();
                        label = currentFunction + "$" + tokenizer.nextToken();
                        symbols.put(label, (short) (nextPC + 1));
                    }

                    nextPC++;
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new ProgramException("Error while reading from file");
        } catch (NoSuchElementException e) {
            throw new ProgramException("In line " + lineNumber + ": unexpected end of command");
        }
		if (isSlashStar) {
			throw new ProgramException("Unterminated /* comment at end of file");
		}
    }

    private BufferedReader openFile(File file) throws ProgramException {
        try {
            return new BufferedReader(new FileReader(file.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            throw new ProgramException("file " + file.getName() + " does not exist");
        }
    }

    // Scans the given file and creates symbols for its functions & label names.
    private void buildProgram(File file, Hashtable<String, Short> symbols) throws ProgramException {

        final BufferedReader reader = openFile(file);

        int lineNumber = 0;
        String line;
        String currentFunction = null;
        short indexInFunction = 0;
        short pc = nextPC;
        final HVMInstructionSet instructionSet = HVMInstructionSet.getInstance();

		isSlashStar = false;
        try {
            while ((line = unCommentLine(reader.readLine())) != null) {
                lineNumber++;

                if (!line.trim().equals("")) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    final String instructionName = tokenizer.nextToken();

                    final byte opCode = instructionSet.instructionStringToCode(instructionName);
                    if (opCode == HVMInstructionSet.UNKNOWN_INSTRUCTION)
                        throw new ProgramException("in line " + lineNumber +
                                                   ": unknown instruction - " + instructionName);

                    switch (opCode) {
                        case HVMInstructionSet.PUSH_CODE:
                        case HVMInstructionSet.POP_CODE:
                            buildPushPop(opCode, pc, indexInFunction, tokenizer, line, lineNumber);
                            break;

                        case HVMInstructionSet.FUNCTION_CODE:
                            currentFunction = buildFunction(lineNumber, line, pc, tokenizer);
                            indexInFunction = 0;
                            break;

                        case HVMInstructionSet.CALL_CODE:
                            buildCall(lineNumber, line, indexInFunction, pc, tokenizer);
                            break;

                        case HVMInstructionSet.LABEL_CODE:
                            buildLabel(currentFunction, pc, tokenizer);
                            indexInFunction--; // since Label is not a "physical" instruction
                            break;

                        case HVMInstructionSet.GOTO_CODE:
                            buildGoto(symbols, lineNumber, line, currentFunction, indexInFunction, pc, tokenizer);
                            break;

                        case HVMInstructionSet.IF_GOTO_CODE:
                            buildIfGoto(symbols, lineNumber, line, currentFunction, indexInFunction, pc, tokenizer);
                            break;

                        // All other instructions have either 1 or 0 arguments and require no
                        // special treatment
                        default:
                            buildOther(lineNumber, line, indexInFunction, opCode, pc, tokenizer);
                            break;
                    }

                    // check end of command
                    if (tokenizer.hasMoreTokens())
                        throw new ProgramException("in line " + lineNumber +
                                                   ": Too many arguments - " + line);

                    pc++;
                    indexInFunction++;
                }

                nextPC = pc;
            }
            reader.close();
        } catch (IOException ioe) {
            throw new ProgramException("Error while reading from file");
        } catch (NumberFormatException nfe) {
            throw new ProgramException("Illegal 16-bit value");
        } catch (NoSuchElementException nsee) {
            throw new ProgramException("In line " + lineNumber + ": unexpected end of command");
        }
		if (isSlashStar) {
			throw new ProgramException("Unterminated /* comment at end of file");
		}
    }

    private void buildPushPop(byte opCode, short pc, short indexInFunction, StringTokenizer tokenizer, String line,
                              int lineNumber) throws ProgramException {
        final short arg0 = translateSegment(tokenizer.nextToken(), lineNumber);
        final short arg1 = Short.parseShort(tokenizer.nextToken());

        if (arg1 < 0)
            throw new ProgramException("in line " + lineNumber + ": Illegal argument - " + line);

        if (arg0 == HVMInstructionSet.STATIC_SEGMENT_CODE && arg1 > largestStaticIndex)
            largestStaticIndex = arg1;

        instructions[pc] = new VMEmulatorInstruction(opCode, arg0, arg1, indexInFunction);
    }

    private short translateSegment(String segment, int lineNumber) throws ProgramException {
        byte code = HVMInstructionSet.getInstance().segmentVMStringToCode(segment);
        if (code == HVMInstructionSet.UNKNOWN_SEGMENT)
            throw new ProgramException("in line " + lineNumber + ": Illegal memory segment - " + segment);

        return code;
    }

    private String buildFunction(int lineNumber, String line, short pc, StringTokenizer tokenizer)
            throws ProgramException {
        final String currentFunction = tokenizer.nextToken();
        final short arg0 = Short.parseShort(tokenizer.nextToken());

        if (arg0 < 0)
            throw new ProgramException("in line " + lineNumber + ": Illegal argument - " + line);

        instructions[pc] = new VMEmulatorInstruction(HVMInstructionSet.FUNCTION_CODE, arg0, (short) 0);
        instructions[pc].setStringArg(currentFunction);
        return currentFunction;
    }

    private void buildCall(int lineNumber, String line, short indexInFunction, short pc, StringTokenizer tokenizer)
            throws ProgramException {
        final String functionName = tokenizer.nextToken();
        final short arg0;
        try {
            arg0 = getAddress(functionName);
        } catch (ProgramException pe) {
            throw new ProgramException("in line " + lineNumber + ": " + pe.getMessage());
        }

        final short arg1 = Short.parseShort(tokenizer.nextToken());

        if (arg1 < 0 || ((arg0 < 0) && arg0 != BUILTIN_FUNCTION_ADDRESS))
            throw new ProgramException("in line " + lineNumber + ": Illegal argument - " + line);

        instructions[pc] = new VMEmulatorInstruction(HVMInstructionSet.CALL_CODE, arg0, arg1, indexInFunction);
        instructions[pc].setStringArg(functionName);
    }

    private void buildLabel(String currentFunction, short pc, StringTokenizer tokenizer) {
        final String label = currentFunction + "$" + tokenizer.nextToken();
        instructions[pc] = new VMEmulatorInstruction(HVMInstructionSet.LABEL_CODE, (short) (-1));
        instructions[pc].setStringArg(label);
    }

    private void buildGoto(Hashtable<String, Short> symbols, int lineNumber, String line, String currentFunction,
                           short indexInFunction, short pc, StringTokenizer tokenizer)
            throws ProgramException {
        final String label = currentFunction + "$" + tokenizer.nextToken();
        final Short labelAddress = symbols.get(label);
        if (labelAddress == null)
            throw new ProgramException("in line " + lineNumber + ": Unknown label - " + label);

        if (labelAddress < 0)
            throw new ProgramException("in line " + lineNumber + ": Illegal argument - " + line);

        instructions[pc] = new VMEmulatorInstruction(HVMInstructionSet.GOTO_CODE, labelAddress, indexInFunction);
        instructions[pc].setStringArg(label);
    }

    private void buildIfGoto(Hashtable<String, Short> symbols, int lineNumber, String line, String currentFunction,
                             short indexInFunction, short pc, StringTokenizer tokenizer)
            throws ProgramException {
        final String label = currentFunction + "$" + tokenizer.nextToken();
        final Short labelAddress = symbols.get(label);
        if (labelAddress == null)
            throw new ProgramException("in line " + lineNumber + ": Unknown label - " + label);

        if (labelAddress < 0)
            throw new ProgramException("in line " + lineNumber + ": Illegal argument - " + line);

        instructions[pc] = new VMEmulatorInstruction(HVMInstructionSet.IF_GOTO_CODE, labelAddress, indexInFunction);
        instructions[pc].setStringArg(label);
    }

    private void buildOther(int lineNumber, String line, short indexInFunction, byte opCode, short pc,
                            StringTokenizer tokenizer) throws ProgramException {
        if (tokenizer.countTokens() == 0) {
            instructions[pc] = new VMEmulatorInstruction(opCode, indexInFunction);
        } else {
            short arg0 = Short.parseShort(tokenizer.nextToken());

            if (arg0 < 0)
                throw new ProgramException("in line " + lineNumber + ": Illegal argument - " + line);

            instructions[pc] = new VMEmulatorInstruction(opCode, arg0, indexInFunction);
        }
    }

    // Returns the "un-commented" version of the given line.
	// Comments can be either with // or /*.
	// The field isSlashStar holds the current /* comment state.
    private String unCommentLine(String line) {
        String result = line;

        if (line != null) {
			if (isSlashStar) {
				int posStarSlash = line.indexOf("*/");
				if (posStarSlash >= 0) {
					isSlashStar = false;
					result = unCommentLine(line.substring(posStarSlash+2));
				} else {
					result = "";
				}
			} else {
				int posSlashSlash = line.indexOf("//");
				int posSlashStar = line.indexOf("/*");
				if (posSlashSlash >= 0 &&
					(posSlashStar < 0 || posSlashStar > posSlashSlash)) {
					result = line.substring(0, posSlashSlash);
				} else if (posSlashStar >= 0) {
					isSlashStar = true;
					result = line.substring(0, posSlashStar) +
							 unCommentLine(line.substring(posSlashStar+2));
				}
			}
        }

        return result;
    }

    /**
     * Returns the static variable address range of the given class name, in the
     * form of a 2-elements array {startAddress, endAddress}.
     * If unknown class name, returns null.
     */
    int[] getStaticRange(String className) {
        return (int[])staticRange.get(className);
    }

    /**
     * Returns the size of the program.
     */
    public int getSize() {
        return instructionsLength;
    }

	short getAddress(String functionName) throws ProgramException {
        Short address = functions.get(functionName);
        if (address != null) {
            return address;
        } else {
            String className = functionNameToClassName(functionName);
            if (staticRange.get(className) == null) {
                // The class is not implemented by a VM file - search for a
                // built-in implementation later. Display a popup to confirm
                // this as this is not a feature from the book but a later
                // addition.
				if (builtInAccessStatus == BUILTIN_ACCESS_UNDECIDED) {
                    final String envUseBuiltIns = System.getenv("N2T_VM_USE_BUILTINS");
                    final boolean useBuiltIns = "yes".equalsIgnoreCase(envUseBuiltIns)
                            || (!"no".equalsIgnoreCase(envUseBuiltIns) && hasGUI && gui.confirmBuiltInAccess());
                    builtInAccessStatus = useBuiltIns ? BUILTIN_ACCESS_AUTHORIZED : BUILTIN_ACCESS_DENIED;
                }
				if (builtInAccessStatus == BUILTIN_ACCESS_AUTHORIZED) {
					return BUILTIN_FUNCTION_ADDRESS;
				}
			}
			// Either:
			// 1.The class is implemented by a VM file and no implementation
			//     for the function is found - don't override with built-in
			// - or -
			// 2.The user did not authorize using built-in implementations.
			throw new ProgramException(className + ".vm not found " +
									   "or function " + functionName +
									   " not found in " + className + ".vm");
		}
	}

    /**
     * Returns the next program counter.
     */
    short getPC() {
        return nextPC;
    }

    /**
     * Returns the current value of the program counter.
     */
    short getCurrentPC() {
        return currentPC;
    }

    /**
     * Returns the previous value of the program counter.
     */
    short getPreviousPC() {
        return prevPC;
    }

    /**
     * Sets the program counter with the given address.
     */
    void setPC(short address) {
        prevPC = currentPC;
        currentPC = nextPC;
        nextPC = address;
        setGUIPC();
    }

    /**
     * Sets the program counter to a specially created infinite loop in the
	 * end of the programs for access by built-in functions, de-facto halting
	 * the program.
	 * important so that tests and other scripts finish counting
	 * (since a built-in infinite loop doesn't count as steps).
	 * also needed because there is no good way to use the stop button to
	 * stop an infinite loop in a built-in jack class.
	 * A message containing information may be provided (can be null).
     */
    void setPCToInfiniteLoopForBuiltIns(String message) {
		if (hasGUI) {
			gui.notify(message);
		}
		setPC(infiniteLoopForBuiltInsAddress);
    }

    /**
     * Returns the next VMEmulatorInstruction and increments the PC by one.
     * The PC will be incremented by more if the next instruction is a label.
     */
    VMEmulatorInstruction getNextInstruction() {
        VMEmulatorInstruction result = null;

        if (nextPC < instructionsLength) {
            result = instructions[nextPC];
            prevPC = currentPC;
            currentPC = nextPC;

            nextPC = getNextInstructionAddress(nextPC);

            setGUIPC();
        }

        return result;
    }

    short getNextInstructionAddress(short pc) {
        do {
            pc++;
        } while (pc < instructionsLength && instructions[pc].getOpCode() == HVMInstructionSet.LABEL_CODE);
        return pc;
    }

    /**
     * Restarts the program from the beginning.
     */
    void restartProgram() {
        currentPC = -999;
        prevPC = -999;
        nextPC = startAddress;
        setGUIPC();
    }

    /**
     * Resets the program (erases all commands).
     */
    public void reset() {
        instructions = new VMEmulatorInstruction[0];
		visibleInstructionsLength = instructionsLength = 0;
        currentPC = -999;
        prevPC = -999;
        nextPC = -1;
        setGUIContents();
    }

    /**
     * Returns the GUI of the computer part.
     */
    public ComputerPartGUI getGUI() {
        return gui;
    }

    /**
     * Called when the current program file/directory is changed.
     * The event contains the source object, the event type and the program's file/dir (if any).
     */
    public void programChanged(ProgramEvent event) {
        switch (event.getType()) {
            case ProgramEvent.LOAD:
                LoadProgramTask task = new LoadProgramTask(event.getProgramFileName());
                Thread t = new Thread(task);
                t.start();
                break;
            case ProgramEvent.CLEAR:
                reset();
                notifyProgramListeners(ProgramEvent.CLEAR, null);
                break;
        }
    }

    // Sets the gui's contents (if a gui exists)
    private void setGUIContents() {
        if (displayChanges) {
            gui.setContents(instructions, visibleInstructionsLength);
            gui.setCurrentInstruction(nextPC);
        }
    }

    // Sets the GUI's current instruction index
    private void setGUIPC() {
        if (displayChanges)
            gui.setCurrentInstruction(nextPC);
    }

    VMEmulatorInstruction getInstructionAt(short pc) {
        return pc >= 0 && pc < instructions.length ? instructions[pc] : null;
    }

    // The task that loads a new program into the emulator
    private class LoadProgramTask implements Runnable {

        private String fileName;

        LoadProgramTask(String fileName) {
            this.fileName = fileName;
        }

        public void run() {
            clearErrorListeners();
            try {
                loadProgram(fileName);
            } catch (ProgramException pe) {
                notifyErrorListeners(pe.getMessage());
            }
        }
    }

    public void refreshGUI() {
        if (displayChanges) {
            gui.setContents(instructions, visibleInstructionsLength);
            gui.setCurrentInstruction(nextPC);
        }
    }

    /**
     * Registers the given ProgramEventListener as a listener to this GUI.
     */
    void addProgramListener(ProgramEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all the ProgramEventListeners on a change in the VM's program by creating
     * a ProgramEvent (with the new event type and program's file name) and sending it using the
     * programChanged function to all the listeners.
     */
    private void notifyProgramListeners(byte eventType, String programFileName) {
        ProgramEvent event = new ProgramEvent(this, eventType, programFileName);

        for (ProgramEventListener listener : listeners)
            listener.programChanged(event);
    }
}

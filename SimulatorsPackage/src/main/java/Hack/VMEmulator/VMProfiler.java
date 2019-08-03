package Hack.VMEmulator;

import Hack.Controller.Profiler;
import Hack.VirtualMachine.HVMInstructionSet;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VMProfiler implements Profiler {

    private final Stack<String> callStack = new Stack<>();
    private final Map<String, AtomicInteger> calls = new TreeMap<>();
    private final Map<String, AtomicInteger> instructionsPerFunction = new TreeMap<>();
    private final Map<String, AtomicInteger> instructionCounts = new TreeMap<>();
    private String currentFunction;
    private boolean enabled = false;

    public VMProfiler() {
        reset();
    }

    @Override
    public void reset() {
        currentFunction = "__init__";
        callStack.clear();
        callStack.push(currentFunction);
        calls.clear();
        instructionsPerFunction.clear();
        instructionCounts.clear();
    }

    @Override
    public String[] getTabNames() {
        return new String[] {
                "Calls", "Instruction per function", "Instruction counts"
        };
    }

    @Override
    public String[] getTableHeaders(int tab) {
        switch (tab) {
            case 0:
                return new String[] {"Function name", "Called count"};
            case 1:
                return new String[] {"Function name", "# executed instructions"};
            case 2:
                return new String[] {"Instruction address", "Execution count"};
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Map<String, AtomicInteger> getData(int tab) {
        switch (tab) {
            case 0:
                return Collections.unmodifiableMap(calls);
            case 1:
                return Collections.unmodifiableMap(instructionsPerFunction);
            case 2:
                return Collections.unmodifiableMap(instructionCounts);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void mark(VMEmulatorInstruction instruction) {
        if (!enabled)
            return;

        markInstruction(currentFunction, instruction);
        switch (instruction.getOpCode()) {
            case HVMInstructionSet.CALL_CODE:
                final String functionName = instruction.getStringArg();
                markCall(functionName);
                callStack.push(functionName);
                currentFunction = functionName;
                break;

            case HVMInstructionSet.RETURN_CODE:
                callStack.pop();
                currentFunction = callStack.peek();
                break;
        }
    }

    private void markInstruction(String functionName, VMEmulatorInstruction instruction) {
        mark(instructionsPerFunction, functionName);
        mark(instructionCounts, functionName + ":" + instruction.getIndexInFunction());
    }

    private void markCall(String functionName) {
        mark(calls, functionName);
    }

    private void mark(Map<String, AtomicInteger> map, String functionName) {
        if (!map.containsKey(functionName))
            map.put(functionName, new AtomicInteger(0));
        map.get(functionName).incrementAndGet();
    }
}

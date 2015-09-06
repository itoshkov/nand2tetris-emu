package Hack.Gates;

public class GateClassUnderLoad extends GateClass {
    protected GateClassUnderLoad(String gateName) {
        super(gateName, new PinInfo[0], new PinInfo[0]);
    }

    @Override
    public Gate newInstance() throws InstantiationException {
        throw new InstantiationException();
    }
}

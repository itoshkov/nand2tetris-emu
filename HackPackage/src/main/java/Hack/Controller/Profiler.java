package Hack.Controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface Profiler {

    void reset();

    String[] getTabNames();

    String[] getTableHeaders(int tab);

    Map<String, AtomicInteger> getData(int tab);

    boolean isEnabled();

    void setEnabled(boolean enabled);
}

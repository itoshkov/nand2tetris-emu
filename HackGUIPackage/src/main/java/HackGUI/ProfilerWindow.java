package HackGUI;

import Hack.Controller.Profiler;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Map.Entry;

public class ProfilerWindow extends JFrame {
    public static final Comparator<Entry<String, AtomicInteger>> ENTRY_COMPARATOR =
            new Comparator<Entry<String, AtomicInteger>>() {
                @Override
                public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
                    return o2.getValue().get() - o1.getValue().get();
                }
            };
    private final Profiler profiler;
    private JTable[] tables;

    public ProfilerWindow(final Profiler profiler) {
        super("Profiler");
        this.profiler = profiler;

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        final String[] tabNames = profiler.getTabNames();
        tables = new JTable[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            final JTable table = new JTable();
            JScrollPane scrollPane = new JScrollPane(table);
            table.setFillsViewportHeight(true);
            tabbedPane.add(tabNames[i], scrollPane);
            tables[i] = table;
        }

        final JCheckBox enableCheckBox = new JCheckBox("Enable");
        enableCheckBox.setSelected(profiler.isEnabled());
        enableCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                profiler.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        final JButton update = new JButton("Update");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTables();
            }
        });

        final JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                profiler.reset();
                updateTables();
            }
        });

        final JPanel controls = new JPanel(new FlowLayout());
        controls.add(enableCheckBox);
        controls.add(update);
        controls.add(reset);
        add(controls, BorderLayout.PAGE_END);

        updateTables();

        pack();
    }

    private void updateTables() {
        for (int i = 0; i < tables.length; i++)
            tables[i].setModel(asTableModel(i));
    }

    private TableModel asTableModel(int tab) {
        final String[] headers = profiler.getTableHeaders(tab);
        final List<Entry<String, AtomicInteger>> data =
                new ArrayList<Entry<String, AtomicInteger>>(profiler.getData(tab).entrySet());

        Collections.sort(data, ENTRY_COMPARATOR);

        return new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                final Entry<String, AtomicInteger> entry = data.get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return entry.getKey();
                    case 1:
                        return entry.getValue().get();
                    default:
                        throw new IllegalArgumentException();
                }
            }

            @Override
            public String getColumnName(int column) {
                return headers[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        return Integer.class;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        };
    }
}

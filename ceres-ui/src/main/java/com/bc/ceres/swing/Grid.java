package com.bc.ceres.swing;

import com.bc.ceres.core.Assert;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Grid extends JPanel {

    public interface SelectionListener {
        void selectionStateChanged(Grid grid);
    }

    private final boolean showSelectionColumn;
    private final List<List<JComponent>> componentRows;
    private final List<SelectionListener> selectionListeners;
    private int dataRowSelectorStateChangeCount;

    public Grid(int columnCount, boolean showSelectionColumn) {
        this(new TableLayout(columnCount), showSelectionColumn);
    }

    public Grid(TableLayout tableLayout, boolean showSelectionColumn) {
        super(tableLayout);
        this.showSelectionColumn = showSelectionColumn;
        this.componentRows = new ArrayList<>();
        this.componentRows.add(new ArrayList<>(Arrays.asList(new JComponent[tableLayout.getColumnCount()])));
        this.selectionListeners = new ArrayList<>();
    }

    @Override
    public TableLayout getLayout() {
        return (TableLayout) super.getLayout();
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        TableLayout oldLayout = getLayout();
        if (oldLayout == mgr) {
            return;
        }
        if (!(mgr instanceof TableLayout)) {
            throw new IllegalArgumentException();
        }
        TableLayout tableLayout = (TableLayout) mgr;
        if (oldLayout != null) {
            if (oldLayout.getColumnCount() != tableLayout.getColumnCount() && getRowCount() > 0) {
                throw new IllegalArgumentException();
            }
        }
        super.setLayout(tableLayout);
    }

    public int getColumnCount() {
        return getLayout().getColumnCount();
    }

    public int getRowCount() {
        return componentRows.size();
    }

    public int getDataRowCount() {
        return componentRows.size() - 1;
    }

    public JComponent getComponent(int rowIndex, int colIndex) {
        return componentRows.get(rowIndex).get(colIndex);
    }

    public JComponent setComponent(int rowIndex, int colIndex, JComponent component) {
        List<JComponent> componentRow = componentRows.get(rowIndex);
        JComponent oldComponent = componentRow.get(colIndex);
        if (oldComponent != null) {
            remove(oldComponent);
        }
        if (component != null) {
            add(component, TableLayout.cell(rowIndex, colIndex));
        }
        componentRow.set(colIndex, component);
        fireComponentsChanged();
        return oldComponent;
    }

    public void setHeaderRow(JComponent... components) {
        checkColumnCount(components);
        List<JComponent> headerRow = new ArrayList<>(components.length + 1);
        if (showSelectionColumn) {
            JCheckBox headerRowSelector = createHeaderRowSelector();
            if (headerRowSelector != null) {
                headerRowSelector.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onHeaderRowSelectorStateChange(e);
                    }
                });
            }
            headerRow.add(headerRowSelector);
        } else {
            headerRow.add(null);
        }
        Collections.addAll(headerRow, components);
        addComponentRowIntern(headerRow, 0);
        componentRows.set(0, headerRow);
        fireComponentsChanged();
        adjustHeaderRowSelector();
    }

    public void addDataRow(JComponent... components) {
        checkColumnCount(components);

        //if (componentRows.get(0).get(0) == null) {
        //    setHeaderRow(new JComponent[getColumnCount() - 1]);
        //}

        List<JComponent> dataRow = new ArrayList<>(components.length + 1);
        if (showSelectionColumn) {
            JCheckBox dataRowSelector = createDataRowSelector();
            if (dataRowSelector != null) {
                dataRowSelector.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onDataRowSelectorStateChange(e);
                    }
                });
            }
            dataRow.add(dataRowSelector);
        } else {
            dataRow.add(null);
        }
        Collections.addAll(dataRow, components);

        addComponentRowIntern(dataRow, componentRows.size());
        componentRows.add(dataRow);
        fireComponentsChanged();
        adjustHeaderRowSelector();
    }

    public void removeDataRow(int rowIndex) {
        Assert.argument(rowIndex >= 1, "rowIndex");
        boolean rowSelected = isRowSelected(rowIndex);
        List<JComponent> componentRow = componentRows.get(rowIndex);
        removeComponentRowIntern(componentRow);
        componentRows.remove(rowIndex);
        // Re-add remaining components, so that they are inserted at correct row positions
        for (int i = rowIndex; i < componentRows.size(); i++) {
            addComponentRowIntern(componentRows.get(i), i);
        }
        fireComponentsChanged();
        if (rowSelected) {
            fireSelectionChange();
        }
    }

    public void removeDataRows(List<Integer> rowIndexes) {
        if (rowIndexes.isEmpty()) {
            return;
        }

        int offset = 0;
        int selectedCount = 0;
        for (int i : rowIndexes) {
            Assert.argument(i >= 1, "rowIndexes");
            int rowIndex = i - offset;
            Assert.state(rowIndex >= 1, "rowIndex");
            selectedCount += isRowSelected(rowIndex) ? 1 : 0;
            List<JComponent> componentRow = componentRows.get(rowIndex);
            removeComponentRowIntern(componentRow);
            componentRows.remove(rowIndex);
            offset++;
        }

        int rowIndex = rowIndexes.get(0);
        // Re-add remaining components, so that they are inserted at correct row positions
        for (int i = rowIndex; i < componentRows.size(); i++) {
            addComponentRowIntern(componentRows.get(i), i);
        }

        fireComponentsChanged();
        if (selectedCount > 0) {
            fireSelectionChange();
        }
    }

    public void moveDataRowUp(int rowIndex) {
        Assert.argument(rowIndex >= 2, "rowIndex");
        List<JComponent> componentRow = componentRows.remove(rowIndex);
        componentRows.add(rowIndex - 1, componentRow);
        for (int i = rowIndex - 1; i < componentRows.size(); i++) {
            List<JComponent> componentRowToUpdate = componentRows.get(i);
            removeComponentRowIntern(componentRowToUpdate);
            addComponentRowIntern(componentRowToUpdate, i);
        }
        fireComponentsChanged();
        fireSelectionChange();
    }

    public void moveDataRowDown(int rowIndex) {
        Assert.argument(rowIndex < componentRows.size() - 1, "rowIndex");
        List<JComponent> componentRow = componentRows.remove(rowIndex);
        componentRows.add(rowIndex + 1, componentRow);
        for (int i = rowIndex; i < componentRows.size(); i++) {
            List<JComponent> componentRowToUpdate = componentRows.get(i);
            removeComponentRowIntern(componentRowToUpdate);
            addComponentRowIntern(componentRowToUpdate, i);
        }
        fireComponentsChanged();
        fireSelectionChange();
    }

    public boolean isRowSelected(int rowIndex) {
        Component component = componentRows.get(rowIndex).get(0);
        if (component instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) component;
            return checkBox.isSelected();
        }
        return false;
    }

    public int getSelectedDataRowCount() {
        int count = 0;
        for (int rowIndex = 1; rowIndex < componentRows.size(); rowIndex++) {
            if (isRowSelected(rowIndex)) {
                count++;
            }
        }
        return count;
    }

    public int getSelectedDataRowIndex() {
        for (int rowIndex = 1; rowIndex < componentRows.size(); rowIndex++) {
            if (isRowSelected(rowIndex)) {
                return rowIndex;
            }
        }
        return -1;
    }

    public List<Integer> getSelectedDataRowIndexes() {
        List<Integer> rowIndices = new ArrayList<>(componentRows.size());
        for (int rowIndex = 1; rowIndex < componentRows.size(); rowIndex++) {
            if (isRowSelected(rowIndex)) {
                rowIndices.add(rowIndex);
            }
        }
        return rowIndices;
    }

    public void setSelectedDataRowIndexes(List<Integer> selectedRowIndexes) {
        List<Integer> oldSelectedRowIndexes = getSelectedDataRowIndexes();
        if (oldSelectedRowIndexes.size() != selectedRowIndexes.size()
            || !oldSelectedRowIndexes.equals(selectedRowIndexes)) {
            dataRowSelectorStateChangeCount = 0;
            for (int rowIndex = 1; rowIndex < componentRows.size(); rowIndex++) {
                setSelectionIntern(rowIndex, selectedRowIndexes.contains(rowIndex));
            }
            if (dataRowSelectorStateChangeCount > 0) {
                fireSelectionChange();
            }
        }
    }

    public void addSelectionListener(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    public void removeSelectionListener(SelectionListener selectionListener) {
        selectionListeners.remove(selectionListener);
    }

    protected void fireSelectionChange() {
        adjustHeaderRowSelector();
        for (SelectionListener selectionListener : selectionListeners) {
            selectionListener.selectionStateChanged(this);
        }
    }

    protected void adjustHeaderRowSelector(JCheckBox headerRowSelector, int selectedDataRowCount) {
        headerRowSelector.setSelected(getDataRowCount() > 0 && selectedDataRowCount == getDataRowCount());
        headerRowSelector.setEnabled(getDataRowCount() > 0);
    }

    protected Border createHeaderCellBorder() {
        return new HeaderBorder();
    }

    protected JCheckBox createHeaderRowSelector() {
        return new JCheckBox();
    }

    protected JCheckBox createDataRowSelector() {
        return new JCheckBox();
    }

    private void onHeaderRowSelectorStateChange(ActionEvent e) {
        JCheckBox checkBox = (JCheckBox) componentRows.get(0).get(0);
        setAllDataRowsSelected(checkBox.isSelected());
    }

    private void onDataRowSelectorStateChange(ActionEvent e) {
        fireSelectionChange();
    }

    private void setAllDataRowsSelected(boolean selected) {
        dataRowSelectorStateChangeCount = 0;
        for (int rowIndex = 1; rowIndex < componentRows.size(); rowIndex++) {
            setSelectionIntern(rowIndex, selected);
        }
        if (dataRowSelectorStateChangeCount > 0) {
            fireSelectionChange();
        }
    }

    private void adjustHeaderRowSelector() {
        JComponent component = componentRows.get(0).get(0);
        if (component instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) component;
            adjustHeaderRowSelector(checkBox, getSelectedDataRowCount());
        }
    }

    private void addComponentRowIntern(List<JComponent> componentRow, int rowIndex) {
        for (int colIndex = 0; colIndex < componentRow.size(); colIndex++) {
            JComponent component = componentRow.get(colIndex);
            if (component != null) {
                remove(component);
                if (rowIndex == 0) {
                    addHeaderBorder(component);
                }
                add(component, TableLayout.cell(rowIndex, colIndex));
                System.out.println("added at (" + rowIndex + "," + colIndex + "): " + component.getClass().getSimpleName());
            }
        }
    }

    private void removeComponentRowIntern(List<JComponent> componentRow) {
        for (JComponent component : componentRow) {
            if (component != null) {
                remove(component);
            }
        }
    }

    private void setSelectionIntern(Integer rowIndex, boolean selected) {
        Component component = componentRows.get(rowIndex).get(0);
        if (component instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) component;
            if (checkBox.isSelected() != selected) {
                checkBox.setSelected(selected);
                dataRowSelectorStateChangeCount++;
            }
        }
    }

    private void fireComponentsChanged() {
        invalidate();
        revalidate();
        validate();
        repaint();
    }

    private void checkColumnCount(JComponent[] components) {
        if (components.length != getColumnCount() - 1) {
            throw new IllegalArgumentException("components");
        }
    }

    private void addHeaderBorder(JComponent component) {
        Border oldBorder = component.getBorder();
        Border newBorder = createHeaderCellBorder();
        if (oldBorder != null) {
            newBorder = BorderFactory.createCompoundBorder(newBorder, oldBorder);
        }
        component.setBorder(newBorder);
    }

    private static class HeaderBorder implements Border {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(c.getForeground());
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
}
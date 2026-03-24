package com.qlsv.view.common;

import com.qlsv.utils.DialogUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCrudPanel<T> extends BasePanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<T> currentItems = new ArrayList<>();

    protected AbstractCrudPanel(String title) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));

        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        JButton addButton = new JButton("Them");
        JButton editButton = new JButton("Sua");
        JButton deleteButton = new JButton("Xoa");
        JButton reloadButton = new JButton("Tai lai");
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(reloadButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addButton.addActionListener(event -> handleAdd());
        editButton.addActionListener(event -> handleEdit());
        deleteButton.addActionListener(event -> handleDelete());
        reloadButton.addActionListener(event -> refreshData());
    }

    protected abstract String[] getColumnNames();

    protected abstract List<T> loadItems();

    protected abstract Object[] toRow(T item);

    protected abstract T promptForEntity(T existingItem);

    protected abstract void saveEntity(T item);

    protected abstract void deleteEntity(T item);

    protected final void refreshData() {
        try {
            currentItems = new ArrayList<>(loadItems());
            tableModel.setRowCount(0);
            // Load du lieu tu service/DAO len bang Swing tai mot diem duy nhat.
            for (T item : currentItems) {
                tableModel.addRow(toRow(item));
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    protected final T getSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentItems.size()) {
            return null;
        }
        return currentItems.get(selectedRow);
    }

    private void handleAdd() {
        try {
            T item = promptForEntity(null);
            if (item != null) {
                saveEntity(item);
                refreshData();
                DialogUtil.showInfo(this, "Luu du lieu thanh cong.");
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void handleEdit() {
        T selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hay chon dong can sua.");
            return;
        }
        try {
            T item = promptForEntity(selectedItem);
            if (item != null) {
                saveEntity(item);
                refreshData();
                DialogUtil.showInfo(this, "Cap nhat du lieu thanh cong.");
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void handleDelete() {
        T selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hay chon dong can xoa.");
            return;
        }
        if (!DialogUtil.confirm(this, "Ban co chac chan muon xoa ban ghi nay?")) {
            return;
        }
        try {
            deleteEntity(selectedItem);
            refreshData();
            DialogUtil.showInfo(this, "Xoa du lieu thanh cong.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}

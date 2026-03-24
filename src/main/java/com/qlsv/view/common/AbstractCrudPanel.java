package com.qlsv.view.common;

import com.qlsv.utils.DialogUtil;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractCrudPanel<T> extends BasePanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private List<T> allItems = new ArrayList<>();
    private List<T> currentItems = new ArrayList<>();

    protected AbstractCrudPanel(String title) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));

        JPanel searchPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        searchField = new JTextField(24);
        JButton searchButton = new JButton("Tim");
        searchPanel.add(new JLabel("Tu khoa"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

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
        topPanel.add(searchPanel, BorderLayout.CENTER);
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
        searchButton.addActionListener(event -> applySearch());
        searchField.addActionListener(event -> applySearch());
    }

    protected abstract String[] getColumnNames();

    protected abstract List<T> loadItems();

    protected abstract Object[] toRow(T item);

    protected abstract T promptForEntity(T existingItem);

    protected abstract void saveEntity(T item);

    protected abstract void deleteEntity(T item);

    protected boolean matchesSearch(T item, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        for (Object cell : toRow(item)) {
            if (cell != null && String.valueOf(cell).toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    protected final void refreshData() {
        try {
            allItems = new ArrayList<>(loadItems());
            bindRows(allItems);
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

    private void applySearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        if (keyword.isBlank()) {
            bindRows(allItems);
            return;
        }
        List<T> filteredItems = new ArrayList<>();
        for (T item : allItems) {
            if (matchesSearch(item, keyword)) {
                filteredItems.add(item);
            }
        }
        bindRows(filteredItems);
    }

    private void bindRows(List<T> items) {
        currentItems = new ArrayList<>(items);
        tableModel.setRowCount(0);
        // Load du lieu tu service/DAO len JTable tai mot diem duy nhat de de debug va bao tri.
        for (T item : currentItems) {
            tableModel.addRow(toRow(item));
        }
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

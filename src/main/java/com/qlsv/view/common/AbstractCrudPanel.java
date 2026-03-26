package com.qlsv.view.common;

import com.qlsv.utils.DialogUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractCrudPanel<T> extends BasePanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JPanel extraTopPanel = new JPanel(new BorderLayout());
    private final JPanel mainContentPanel = new JPanel(new BorderLayout(0, 12));
    private final JPanel tableCardPanel = new JPanel(new CardLayout());
    private final JLabel emptyStateLabel = new JLabel("", SwingConstants.CENTER);

    private final CardLayout tableCardLayout = (CardLayout) tableCardPanel.getLayout();
    private final JScrollPane tableScrollPane;
    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;
    private final JButton reloadButton;

    private JSplitPane splitPane;
    private JComponent detailPanel;
    private List<T> allItems = new ArrayList<>();
    private List<T> currentItems = new ArrayList<>();

    protected AbstractCrudPanel(String title) {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JPanel searchPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(24);
        searchField.setToolTipText("Nhập từ khóa để tìm nhanh trong dữ liệu đang hiển thị.");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        JButton searchButton = new JButton("Tìm");
        styleFilledButton(searchButton, AppColors.BUTTON_PRIMARY);
        searchPanel.add(new JLabel("Từ khóa"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        reloadButton = new JButton("Tải lại");
        styleFilledButton(addButton, AppColors.BUTTON_SUCCESS);
        styleFilledButton(editButton, AppColors.BUTTON_WARNING);
        styleFilledButton(deleteButton, AppColors.BUTTON_DANGER);
        styleFilledButton(reloadButton, AppColors.BUTTON_NEUTRAL);
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(reloadButton);

        JPanel topHeaderPanel = new JPanel(new BorderLayout(12, 8));
        topHeaderPanel.setOpaque(false);
        topHeaderPanel.add(titleLabel, BorderLayout.WEST);
        topHeaderPanel.add(searchPanel, BorderLayout.CENTER);
        topHeaderPanel.add(actionPanel, BorderLayout.EAST);

        extraTopPanel.setOpaque(false);
        JPanel northPanel = createSectionCard(new BorderLayout(0, 10));
        northPanel.add(topHeaderPanel, BorderLayout.NORTH);
        northPanel.add(extraTopPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        configureTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onSelectionChanged(getSelectedItem());
            }
        });
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        tableScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);

        JPanel emptyStatePanel = new JPanel(new BorderLayout());
        emptyStatePanel.setOpaque(true);
        emptyStatePanel.setBackground(AppColors.CARD_BACKGROUND);
        emptyStatePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(48, 24, 48, 24)
        ));
        emptyStateLabel.setFont(emptyStateLabel.getFont().deriveFont(Font.ITALIC, 15f));
        emptyStateLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        emptyStatePanel.add(emptyStateLabel, BorderLayout.CENTER);

        tableCardPanel.setOpaque(false);
        mainContentPanel.setOpaque(false);
        tableCardPanel.add(tableScrollPane, "table");
        tableCardPanel.add(emptyStatePanel, "empty");
        mainContentPanel.add(tableCardPanel, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);

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

    protected String getEmptyStateMessage() {
        return "Không có dữ liệu để hiển thị.";
    }

    protected void onSelectionChanged(T selectedItem) {
    }

    protected final void setFilterPanel(JComponent filterPanel) {
        extraTopPanel.removeAll();
        if (filterPanel != null) {
            filterPanel.setOpaque(false);
            styleNestedButtons(filterPanel);
            extraTopPanel.add(filterPanel, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    protected final void setDetailPanel(JComponent detailPanel) {
        this.detailPanel = detailPanel;
        rebuildContentLayout();
    }

    protected final JTable getTable() {
        return table;
    }

    protected final List<T> getCurrentItems() {
        return new ArrayList<>(currentItems);
    }

    protected final void configureActionButtons(boolean showAdd, boolean showEdit, boolean showDelete, boolean showReload) {
        addButton.setVisible(showAdd);
        editButton.setVisible(showEdit);
        deleteButton.setVisible(showDelete);
        reloadButton.setVisible(showReload);
    }

    @Override
    public void reloadData() {
        refreshData();
    }

    protected final void refreshData() {
        try {
            allItems = new ArrayList<>(loadItems());
            applySearch();
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

        for (T item : currentItems) {
            tableModel.addRow(toRow(item));
        }

        table.clearSelection();
        onSelectionChanged(null);

        if (currentItems.isEmpty()) {
            emptyStateLabel.setText("<html><div style='text-align:center;'>" + getEmptyStateMessage() + "</div></html>");
            tableCardLayout.show(tableCardPanel, "empty");
            return;
        }

        tableCardLayout.show(tableCardPanel, "table");
    }

    private void handleAdd() {
        try {
            T item = promptForEntity(null);
            if (item != null) {
                saveEntity(item);
                refreshData();
                DialogUtil.showInfo(this, "Lưu dữ liệu thành công.");
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void handleEdit() {
        T selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hãy chọn dòng cần sửa.");
            return;
        }
        try {
            T item = promptForEntity(selectedItem);
            if (item != null) {
                saveEntity(item);
                refreshData();
                DialogUtil.showInfo(this, "Cập nhật dữ liệu thành công.");
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void handleDelete() {
        T selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hãy chọn dòng cần xóa.");
            return;
        }
        if (!DialogUtil.confirm(this, "Bạn có chắc chắn muốn xóa bản ghi này không?")) {
            return;
        }
        try {
            deleteEntity(selectedItem);
            refreshData();
            DialogUtil.showInfo(this, "Xóa dữ liệu thành công.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void rebuildContentLayout() {
        mainContentPanel.removeAll();
        if (detailPanel == null) {
            mainContentPanel.add(tableCardPanel, BorderLayout.CENTER);
        } else {
            JScrollPane detailScrollPane = buildDetailScrollPane(detailPanel);

            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableCardPanel, detailScrollPane);
            splitPane.setResizeWeight(0.7);
            splitPane.setBorder(null);
            splitPane.setContinuousLayout(true);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerSize(10);
            splitPane.setOpaque(false);
            splitPane.setBackground(AppColors.CONTENT_BACKGROUND);

            tableCardPanel.setMinimumSize(new Dimension(0, 240));
            detailScrollPane.setMinimumSize(new Dimension(0, 180));
            mainContentPanel.add(splitPane, BorderLayout.CENTER);

            SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.7));
        }
        revalidate();
        repaint();
    }

    private JScrollPane buildDetailScrollPane(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(content.getBackground());
        return scrollPane;
    }

    private void configureTable() {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(AppColors.CARD_VALUE_TEXT);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
    }

    private JPanel createSectionCard(LayoutManager layoutManager) {
        JPanel panel = new JPanel(layoutManager);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private void styleFilledButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    private void styleNestedButtons(Component component) {
        if (component instanceof JButton button) {
            styleFilledButton(button, AppColors.BUTTON_NEUTRAL);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                styleNestedButtons(child);
            }
        }
    }
}

/**
 * Khung CRUD dùng chung cho các màn hình quản trị.
 */
package com.qlsv.view.common;

import com.qlsv.utils.DialogUtil;
import com.qlsv.utils.SearchDebouncer;
import com.qlsv.view.dialog.BaseDetailDialog;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private JComponent detailPanel;
    private BaseDetailDialog detailDialog;
    private List<T> allItems = new ArrayList<>();
    private List<T> currentItems = new ArrayList<>();
    private SwingWorker<?, ?> activeWorker;
    private Object selectedItemId;
    private SearchDebouncer searchDebouncer;
    private static final String LOADING_CARD = "loading";
    private static final String DATA_CARD = "table";
    private static final String EMPTY_CARD = "empty";

    /**
     * Khởi tạo abstract crud.
     */
    protected AbstractCrudPanel(String title) {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppFonts.H2);
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JPanel searchPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, AppSpacing.COMPONENT_GAP, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(24);
        searchField.setToolTipText("Nhập từ khóa để tìm nhanh trong dữ liệu đang hiển thị.");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        JButton searchButton = new JButton("Tìm");
        styleCompactFilledButton(searchButton, AppColors.BUTTON_PRIMARY);
        searchPanel.add(new JLabel("Từ khóa"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, AppSpacing.COMPONENT_GAP, 0));
        actionPanel.setOpaque(false);
        addButton = new JButton("Thêm");
        editButton = new JButton("Sửa");
        deleteButton = new JButton("Xóa");
        reloadButton = new JButton("Tải lại");
        styleFilledButton(addButton, AppColors.BUTTON_SUCCESS);
        styleFilledButton(editButton, AppColors.BUTTON_WARNING);
        styleFilledButton(deleteButton, AppColors.BUTTON_DANGER);
        styleFilledButton(reloadButton, AppColors.BUTTON_NEUTRAL);
        configureCustomActionButtons(actionPanel);
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(reloadButton);

        JPanel topHeaderPanel = new JPanel(new BorderLayout(AppSpacing.MD, AppSpacing.COMPONENT_GAP));
        topHeaderPanel.setOpaque(false);
        topHeaderPanel.add(titleLabel, BorderLayout.WEST);
        if (isHeaderSearchVisible()) {
            topHeaderPanel.add(searchPanel, BorderLayout.CENTER);
        }
        topHeaderPanel.add(actionPanel, BorderLayout.EAST);

        extraTopPanel.setOpaque(false);
        JPanel northPanel = createSectionCard(new BorderLayout(0, 10));
        northPanel.add(topHeaderPanel, BorderLayout.NORTH);
        northPanel.add(extraTopPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(getColumnNames(), 0) {
            /**
             * Xác định ô có cho phép chỉnh sửa hay không.
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new ResponsiveTable(tableModel);
        configureTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                T selectedItem = getSelectedItem();
                onSelectionChanged(selectedItem);
                handleDetailSelection(selectedItem);
            }
        });
        table.addMouseListener(new MouseAdapter() {
            /**
             * Xử lý thao tác nhấp chuột trên giao diện.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && getSelectedItem() != null) {
                    showDetailDialog();
                }
            }
        });
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        tableScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        tableScrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);

        JPanel emptyStatePanel = new EmptyStatePanel();
        emptyStatePanel.setOpaque(true);
        emptyStatePanel.setBackground(AppColors.CARD_BACKGROUND);

        tableCardPanel.setOpaque(false);
        mainContentPanel.setOpaque(false);
        tableCardPanel.add(tableScrollPane, DATA_CARD);
        tableCardPanel.add(emptyStatePanel, EMPTY_CARD);
        tableCardPanel.add(new LoadingPanel(), LOADING_CARD);
        mainContentPanel.add(tableCardPanel, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);

        addButton.addActionListener(event -> handleAdd());
        editButton.addActionListener(event -> handleEdit());
        deleteButton.addActionListener(event -> handleDelete());
        reloadButton.addActionListener(event -> refreshData());
        searchButton.addActionListener(event -> handleSearch());
        searchField.addActionListener(event -> handleSearch());
        
        // Setup search debounce
        searchDebouncer = new SearchDebouncer(this::handleSearch, 300);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchDebouncer.trigger(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchDebouncer.trigger(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchDebouncer.trigger(); }
        });
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
    }

    /**
     * Trả về column names.
     */
    protected abstract String[] getColumnNames();

    /**
     * Nạp items.
     */
    protected abstract List<T> loadItems();

    /**
     * Xử lý to row.
     */
    protected abstract Object[] toRow(T item);

    /**
     * Xử lý prompt for entity.
     */
    protected abstract T promptForEntity(T existingItem);

    /**
     * Lưu entity.
     */
    protected abstract void saveEntity(T item);

    /**
     * Xóa entity.
     */
    protected abstract void deleteEntity(T item);

    /**
     * Trả về ID của item để preserve selection (override nếu cần).
     */
    protected Object getItemId(T item) {
        return null;
    }

    /**
     * Kiểm tra xem một đối tượng dữ liệu có khớp với từ khóa tìm kiếm hay không.
     */
    protected boolean matchesSearch(T item, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        for (Object cell : toRow(item)) {
            if (cell != null && String.valueOf(cell).toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Trả về thông báo mặc định khi danh sách dữ liệu trống.
     */
    protected String getEmptyStateMessage() {
        return "Không có dữ liệu để hiển thị.";
    }

    /**
     * Thực hiện lọc danh sách các đối tượng dựa trên từ khóa tìm kiếm.
     */
    protected List<T> performSearch(String keyword, List<T> loadedItems) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>(loadedItems);
        }
        List<T> filteredItems = new ArrayList<>();
        for (T item : loadedItems) {
            if (matchesSearch(item, keyword)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    /**
     * Phương thức callback được gọi khi dòng được chọn trên bảng thay đổi.
     */
    protected void onSelectionChanged(T selectedItem) {
    }

    /**
     * Thiết lập tùy biến thao tác nút.
     */
    protected void configureCustomActionButtons(JPanel actionPanel) {
    }

    /**
     * Kiểm tra đầu trang tìm kiếm visible.
     */
    protected boolean isHeaderSearchVisible() {
        return true;
    }

    /**
     * Gắn một panel chứa các bộ lọc bổ sung vào thanh tiêu đề.
     */
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

    /**
     * Cập nhật panel chi tiết.
     */
    protected final void setDetailPanel(JComponent detailPanel) {
        this.detailPanel = detailPanel;
        disposeDetailDialog();
        rebuildContentLayout();
    }

    /**
     * Trả về bảng.
     */
    protected final JTable getTable() {
        return table;
    }

    /**
     * Trả về items hiện tại.
     */
    protected final List<T> getCurrentItems() {
        return new ArrayList<>(currentItems);
    }

    /**
     * Thiết lập thao tác nút.
     */
    protected final void configureActionButtons(boolean showAdd, boolean showEdit, boolean showDelete, boolean showReload) {
        addButton.setVisible(showAdd);
        editButton.setVisible(showEdit);
        deleteButton.setVisible(showDelete);
        reloadButton.setVisible(showReload);
    }

    /**
     * Tạo hộp thoại chi tiết.
     */
    protected BaseDetailDialog createDetailDialog(JComponent detailPanel) {
        return new BaseDetailDialog("Chi tiết bản ghi", detailPanel);
    }

    /**
     * Kiểm tra hộp thoại chi tiết visible.
     */
    protected final boolean isDetailDialogVisible() {
        return detailDialog != null && detailDialog.isVisible();
    }

    /**
     * Hiển thị hộp thoại chi tiết cho bản ghi hiện đang được chọn.
     */
    protected final void showDetailDialog() {
        if (detailPanel == null) {
            return;
        }
        if (detailDialog == null) {
            detailDialog = createDetailDialog(detailPanel);
        }
        detailDialog.openDialog();
    }

    /**
     * Ẩn hộp thoại chi tiết.
     */
    protected final void hideDetailDialog() {
        if (detailDialog != null) {
            detailDialog.setVisible(false);
        }
    }

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        refreshData();
    }

    /**
     * Giải phóng tài nguyên khi thành phần bị gỡ.
     */
    @Override
    public void removeNotify() {
        disposeDetailDialog();
        if (searchDebouncer != null) {
            searchDebouncer.dispose();
        }
        super.removeNotify();
    }

    /**
     * Làm mới dữ liệu bảng bằng cách tải lại từ nguồn dữ liệu (service/dao) bất đồng bộ.
     */
    protected final void refreshData() {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }

        // Lưu selection hiện tại
        T currentSelection = getSelectedItem();
        if (currentSelection != null) {
            selectedItemId = getItemId(currentSelection);
        }

        setLoadingState(true);
        activeWorker = new SwingWorker<List<T>, Void>() {
            @Override
            protected List<T> doInBackground() {
                return loadItems();
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) return;
                    allItems = new ArrayList<>(get());
                    applySearch();
                } catch (Exception exception) {
                    NotificationToast.showError(AbstractCrudPanel.this, "Lỗi tải dữ liệu: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        };
        activeWorker.execute();
    }

    /**
     * Lấy đối tượng dữ liệu tương ứng với dòng đang được chọn trên bảng.
     */
    protected final T getSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentItems.size()) {
            return null;
        }
        return currentItems.get(selectedRow);
    }

    /**
     * Áp dụng từ khóa từ ô tìm kiếm vào danh sách dữ liệu hiện tại.
     */
    private void applySearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        bindRows(performSearch(keyword, allItems));
    }

    /**
     * Xử lý tìm kiếm.
     */
    private void handleSearch() {
        try {
            applySearch();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Cập nhật dữ liệu vào bảng và xử lý trạng thái hiển thị (danh sách trống/có dữ liệu).
     */
    private void bindRows(List<T> items) {
        currentItems = new ArrayList<>(items);
        tableModel.setRowCount(0);

        for (T item : currentItems) {
            tableModel.addRow(toRow(item));
        }

        // Restore selection nếu có
        restoreSelection();

        if (currentItems.isEmpty()) {
            emptyStateLabel.setText("<html><div style='text-align:center;'>" + getEmptyStateMessage() + "</div></html>");
            tableCardLayout.show(tableCardPanel, "empty");
            return;
        }

        tableCardLayout.show(tableCardPanel, "table");
    }

    /**
     * Khôi phục selection sau khi refresh data.
     */
    private void restoreSelection() {
        if (selectedItemId == null) {
            table.clearSelection();
            onSelectionChanged(null);
            hideDetailDialog();
            return;
        }

        // Tìm item có cùng ID
        for (int i = 0; i < currentItems.size(); i++) {
            T item = currentItems.get(i);
            Object itemId = getItemId(item);
            if (itemId != null && itemId.equals(selectedItemId)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                selectedItemId = null;
                return;
            }
        }

        // Không tìm thấy, clear selection
        table.clearSelection();
        onSelectionChanged(null);
        hideDetailDialog();
        selectedItemId = null;
    }

    /**
     * Xử lý sự kiện khi nhấn nút Thêm mới bản ghi.
     */
    private void handleAdd() {
        try {
            T item = promptForEntity(null);
            if (item != null) {
                performSaveAsync(item, "Lưu dữ liệu thành công.");
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Thực hiện lưu entity bất đồng bộ.
     */
    private void performSaveAsync(T item, String successMessage) {
        setLoadingState(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                saveEntity(item);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    NotificationToast.showSuccess(AbstractCrudPanel.this, successMessage);
                    refreshData();
                } catch (Exception exception) {
                    NotificationToast.showError(AbstractCrudPanel.this, "Lỗi lưu dữ liệu: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    /**
     * Xử lý sự kiện khi nhấn nút Chỉnh sửa bản ghi đang chọn.
     */
    private void handleEdit() {
        T selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hãy chọn dòng cần sửa.");
            return;
        }
        try {
            T item = promptForEntity(selectedItem);
            if (item != null) {
                performSaveAsync(item, "Cập nhật dữ liệu thành công.");
            }
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút Xóa bản ghi đang chọn kèm xác nhận.
     */
    private void handleDelete() {
        T selectedItem = getSelectedItem();
        if (selectedItem == null) {
            DialogUtil.showError(this, "Hãy chọn dòng cần xóa.");
            return;
        }
        if (!DialogUtil.confirm(this, "Bạn có chắc chắn muốn xóa bản ghi này không?")) {
            return;
        }
        setLoadingState(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                deleteEntity(selectedItem);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    NotificationToast.showSuccess(AbstractCrudPanel.this, "Xóa dữ liệu thành công.");
                    refreshData();
                } catch (Exception exception) {
                    NotificationToast.showError(AbstractCrudPanel.this, "Lỗi xóa dữ liệu: " + exception.getMessage());
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    /**
     * Xử lý rebuild content layout.
     */
    private void rebuildContentLayout() {
        mainContentPanel.removeAll();
        mainContentPanel.add(tableCardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Xử lý chi tiết selection.
     */
    private void handleDetailSelection(T selectedItem) {
        if (selectedItem == null) {
            hideDetailDialog();
            return;
        }
        showDetailDialog();
    }

    /**
     * Giải phóng hộp thoại chi tiết.
     */
    private void disposeDetailDialog() {
        if (detailDialog != null) {
            detailDialog.dispose();
            detailDialog = null;
        }
    }

    /**
     * Thiết lập bảng.
     */
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

    /**
     * Tạo card phần.
     */
    private JPanel createSectionCard(LayoutManager layoutManager) {
        JPanel panel = new JPanel(layoutManager);
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(
                        AppSpacing.PADDING_NORMAL,
                        AppSpacing.PADDING_NORMAL + 2,
                        AppSpacing.PADDING_NORMAL,
                        AppSpacing.PADDING_NORMAL + 2
                )
        ));
        return panel;
    }

    /**
     * Thiết lập kiểu dáng (màu sắc, viền, con trỏ) cho các nút bấm hành động.
     */
    private void styleFilledButton(JButton button, Color background) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    /**
     * Áp dụng kiểu cho nút compact filled.
     */
    private void styleCompactFilledButton(JButton button, Color background) {
        styleFilledButton(button, background);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    /**
     * Áp dụng kiểu cho nested nút.
     */
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

    /**
     * Cập nhật trạng thái loading.
     */
    protected void setLoadingState(boolean loading) {
        addButton.setEnabled(!loading);
        editButton.setEnabled(!loading);
        deleteButton.setEnabled(!loading);
        reloadButton.setEnabled(!loading);
        searchField.setEnabled(!loading);
        table.setEnabled(!loading);

        if (loading) {
            tableCardLayout.show(tableCardPanel, LOADING_CARD);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
            // Hiển thị lại table hoặc empty state
            if (currentItems.isEmpty()) {
                tableCardLayout.show(tableCardPanel, EMPTY_CARD);
            } else {
                tableCardLayout.show(tableCardPanel, DATA_CARD);
            }
        }
    }

    /**
     * Setup keyboard shortcuts cho CRUD operations.
     */
    private void setupKeyboardShortcuts() {
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getActionMap();

        // Ctrl+N: Thêm mới
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "add");
        actionMap.put("add", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addButton.isVisible() && addButton.isEnabled()) {
                    handleAdd();
                }
            }
        });

        // Ctrl+E hoặc Enter: Sửa
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "edit");
        actionMap.put("edit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editButton.isVisible() && editButton.isEnabled() && getSelectedItem() != null) {
                    handleEdit();
                }
            }
        });

        // Delete: Xóa
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (deleteButton.isVisible() && deleteButton.isEnabled() && getSelectedItem() != null) {
                    handleDelete();
                }
            }
        });

        // F5: Tải lại
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "reload");
        actionMap.put("reload", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (reloadButton.isVisible() && reloadButton.isEnabled()) {
                    refreshData();
                }
            }
        });

        // Ctrl+F: Focus search field
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "focusSearch");
        actionMap.put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchField.isVisible() && searchField.isEnabled()) {
                    searchField.requestFocusInWindow();
                    searchField.selectAll();
                }
            }
        });
    }

    private static final class ResponsiveTable extends JTable {

        /**
         * Xử lý bảng responsive.
         */
        private ResponsiveTable(DefaultTableModel model) {
            super(model);
        }

        /**
         * Trả về scrollable tracks viewport width.
         */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getAutoResizeMode() != AUTO_RESIZE_OFF
                    || getParent() == null
                    || getPreferredSize().width < getParent().getWidth();
        }
    }
}

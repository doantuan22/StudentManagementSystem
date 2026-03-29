/**
 * Thiết lập theme giao diện dùng chung.
 */
package com.qlsv.view.common;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;

public final class AppTheme {

    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font UI_FONT_BOLD = UI_FONT.deriveFont(Font.BOLD, 13f);
    private static final Font SECTION_TITLE_FONT = UI_FONT.deriveFont(Font.BOLD, 17f);
    private static final int CONTROL_HEIGHT = 38;
    private static final int COMBO_BASE_WIDTH = 220;
    private static final int COMBO_MIN_WIDTH = 180;
    private static final int COMBO_MAX_WIDTH = 280;
    private static final String BUTTON_STYLED = "appTheme.buttonStyled";
    private static final String INPUT_STYLED = "appTheme.inputStyled";
    private static final String COMBO_STYLED = "appTheme.comboStyled";
    private static final String COMBO_EXPLICIT_WIDTH = "appTheme.comboExplicitWidth";
    private static final String TABLE_STYLED = "appTheme.tableStyled";
    private static final String PANEL_STYLED = "appTheme.panelStyled";
    private static final String LABEL_STYLED = "appTheme.labelStyled";
    private static final String TABLE_HOVER_ROW = "appTheme.tableHoverRow";
    private static boolean installed;

    /**
     * Khởi tạo app theme.
     */
    private AppTheme() {
    }

    /**
     * Xử lý install.
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }

        UIManager.put("Label.font", UI_FONT);
        UIManager.put("Button.font", UI_FONT_BOLD);
        UIManager.put("TextField.font", UI_FONT);
        UIManager.put("PasswordField.font", UI_FONT);
        UIManager.put("ComboBox.font", UI_FONT);
        UIManager.put("Table.font", UI_FONT);
        UIManager.put("TableHeader.font", UI_FONT_BOLD);
        UIManager.put("Panel.background", AppColors.CONTENT_BACKGROUND);

        Toolkit.getDefaultToolkit().addAWTEventListener(new ThemeEventListener(),
                AWTEvent.WINDOW_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
        installed = true;
    }

    /**
     * Xử lý apply tree.
     */
    public static void applyTree(Component root) {
        if (root == null) {
            return;
        }
        applyComponent(root);
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyTree(child);
            }
        }
    }

    /**
     * Áp dụng kiểu cho phần title.
     */
    public static void styleSectionTitle(JLabel label) {
        if (label == null) {
            return;
        }
        label.setFont(SECTION_TITLE_FONT);
        label.setForeground(AppColors.CARD_VALUE_TEXT);
    }

    /**
     * Tạo card border.
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        );
    }

    /**
     * Xử lý apply component.
     */
    private static void applyComponent(Component component) {
        if (component instanceof AbstractButton button) {
            styleButton(button);
            return;
        }
        if (component instanceof JComboBox<?> comboBox) {
            styleComboBox(comboBox);
            return;
        }
        if (component instanceof JPasswordField passwordField) {
            styleInput(passwordField);
            return;
        }
        if (component instanceof JTextField textField) {
            styleInput(textField);
            return;
        }
        if (component instanceof JTable table) {
            styleTable(table);
            return;
        }
        if (component instanceof JScrollPane scrollPane) {
            styleScrollPane(scrollPane);
            return;
        }
        if (component instanceof JLabel label) {
            styleLabel(label);
            return;
        }
        if (component instanceof JPanel panel) {
            stylePanel(panel);
        }
    }

    /**
     * Áp dụng kiểu cho nút.
     */
    private static void styleButton(AbstractButton button) {
        if (Boolean.TRUE.equals(button.getClientProperty(BUTTON_STYLED))) {
            return;
        }

        if (isSidebarButton(button)) {
            button.putClientProperty(BUTTON_STYLED, Boolean.TRUE);
            return;
        }

        Color semanticColor = resolveButtonColor(button.getText(), button.getBackground());
        button.setUI(new AppButtonUI());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(UI_FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setBackground(semanticColor);
        button.setForeground(resolveTextColor(semanticColor));
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

        Dimension preferredSize = button.getPreferredSize();
        int preferredWidth = Math.max(preferredSize.width, 88);
        button.setPreferredSize(new Dimension(preferredWidth, CONTROL_HEIGHT));
        if (button.getMinimumSize() == null || button.getMinimumSize().height < CONTROL_HEIGHT) {
            button.setMinimumSize(new Dimension(Math.min(preferredWidth, 72), CONTROL_HEIGHT));
        }

        button.putClientProperty(BUTTON_STYLED, Boolean.TRUE);
    }

    /**
     * Áp dụng kiểu cho input.
     */
    private static void styleInput(JTextField field) {
        if (Boolean.TRUE.equals(field.getClientProperty(INPUT_STYLED))) {
            return;
        }

        field.setFont(UI_FONT);
        field.setOpaque(true);
        field.setBackground(Color.WHITE);
        field.setForeground(AppColors.CARD_VALUE_TEXT);
        field.setCaretColor(AppColors.CARD_VALUE_TEXT);
        field.setSelectionColor(AppColors.TABLE_SELECTION_BACKGROUND);
        field.setSelectedTextColor(AppColors.CARD_VALUE_TEXT);
        field.setBorder(new FocusBorder());

        Dimension preferredSize = field.getPreferredSize();
        field.setPreferredSize(new Dimension(preferredSize.width, CONTROL_HEIGHT));
        if (field.getMinimumSize() == null || field.getMinimumSize().height < CONTROL_HEIGHT) {
            field.setMinimumSize(new Dimension(Math.max(preferredSize.width, 120), CONTROL_HEIGHT));
        }

        field.addFocusListener(new FocusAdapter() {
            /**
             * Xử lý focus gained.
             */
            @Override
            public void focusGained(FocusEvent event) {
                field.repaint();
            }

            /**
             * Xử lý focus lost.
             */
            @Override
            public void focusLost(FocusEvent event) {
                field.repaint();
            }
        });

        field.putClientProperty(INPUT_STYLED, Boolean.TRUE);
    }

    /**
     * Áp dụng kiểu cho chọn box.
     */
    private static void styleComboBox(JComboBox<?> comboBox) {
        if (Boolean.TRUE.equals(comboBox.getClientProperty(COMBO_STYLED))) {
            return;
        }

        comboBox.setFont(UI_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(AppColors.CARD_VALUE_TEXT);
        comboBox.setBorder(new FocusBorder());
        comboBox.setFocusable(true);
        comboBox.setUI(new AppComboBoxUI());
        comboBox.setRenderer(new AppComboBoxRenderer((ListCellRenderer<Object>) comboBox.getRenderer()));
        if (comboBox.isPreferredSizeSet() && comboBox.getPreferredSize() != null && comboBox.getPreferredSize().width > 0) {
            comboBox.putClientProperty(COMBO_EXPLICIT_WIDTH, comboBox.getPreferredSize().width);
        }
        normalizeComboBoxSize(comboBox);

        ComboBoxEditor editor = comboBox.getEditor();
        if (editor != null && editor.getEditorComponent() instanceof JTextField editorField) {
            styleInput(editorField);
        }

        comboBox.addFocusListener(new FocusAdapter() {
            /**
             * Xử lý focus gained.
             */
            @Override
            public void focusGained(FocusEvent event) {
                comboBox.repaint();
            }

            /**
             * Xử lý focus lost.
             */
            @Override
            public void focusLost(FocusEvent event) {
                comboBox.repaint();
            }
        });
        comboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(() -> normalizeComboBoxSize(comboBox));
            }
        });
        comboBox.addPropertyChangeListener("model",
                event -> SwingUtilities.invokeLater(() -> normalizeComboBoxSize(comboBox)));
        comboBox.addPropertyChangeListener("prototypeDisplayValue",
                event -> SwingUtilities.invokeLater(() -> normalizeComboBoxSize(comboBox)));
        SwingUtilities.invokeLater(() -> normalizeComboBoxSize(comboBox));

        comboBox.putClientProperty(COMBO_STYLED, Boolean.TRUE);
    }

    /**
     * Chuẩn hóa chọn box size.
     */
    private static void normalizeComboBoxSize(JComboBox<?> comboBox) {
        Integer explicitWidth = (Integer) comboBox.getClientProperty(COMBO_EXPLICIT_WIDTH);
        int normalizedWidth;
        if (explicitWidth != null && explicitWidth > 0) {
            normalizedWidth = Math.max(COMBO_MIN_WIDTH, Math.min(COMBO_MAX_WIDTH, explicitWidth));
        } else {
            int measuredWidth = measureComboBoxContentWidth(comboBox);
            int preferredWidth = comboBox.getPreferredSize() == null ? 0 : comboBox.getPreferredSize().width;
            normalizedWidth = Math.max(COMBO_BASE_WIDTH, Math.max(preferredWidth, measuredWidth));
            normalizedWidth = Math.max(COMBO_MIN_WIDTH, Math.min(COMBO_MAX_WIDTH, normalizedWidth));
        }
        comboBox.setPreferredSize(new Dimension(normalizedWidth, CONTROL_HEIGHT));

        Dimension minimumSize = comboBox.getMinimumSize();
        int minimumWidth = minimumSize == null ? COMBO_MIN_WIDTH : minimumSize.width;
        comboBox.setMinimumSize(new Dimension(Math.max(COMBO_MIN_WIDTH, Math.min(minimumWidth, normalizedWidth)), CONTROL_HEIGHT));
    }

    /**
     * Xử lý measure chọn box content width.
     */
    private static int measureComboBoxContentWidth(JComboBox<?> comboBox) {
        if (comboBox.getFont() == null) {
            return COMBO_BASE_WIDTH;
        }

        java.awt.FontMetrics metrics = comboBox.getFontMetrics(comboBox.getFont());
        int maxTextWidth = 0;
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem != null) {
            maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(String.valueOf(selectedItem)));
        }

        int itemCount = Math.min(comboBox.getItemCount(), 30);
        for (int index = 0; index < itemCount; index++) {
            Object item = comboBox.getItemAt(index);
            if (item != null) {
                maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(String.valueOf(item)));
            }
        }

        return maxTextWidth + 54;
    }

    /**
     * Áp dụng kiểu cho bảng.
     */
    private static void styleTable(JTable table) {
        if (Boolean.TRUE.equals(table.getClientProperty(TABLE_STYLED))) {
            return;
        }

        table.setFont(UI_FONT);
        table.setRowHeight(34);
        table.setGridColor(AppColors.CARD_BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setBackground(Color.WHITE);
        table.setForeground(AppColors.CARD_VALUE_TEXT);
        table.setSelectionBackground(AppColors.TABLE_SELECTION_BACKGROUND);
        table.setSelectionForeground(AppColors.CARD_VALUE_TEXT);
        table.setIntercellSpacing(new Dimension(0, 1));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            /**
             * Trả về bảng cell renderer component.
             */
            @Override
            public Component getTableCellRendererComponent(JTable currentTable, Object value, boolean selected,
                                                           boolean hasFocus, int row, int column) {
                Component renderer = super.getTableCellRendererComponent(currentTable, value, selected, hasFocus, row, column);
                if (!selected) {
                    Integer hoverRow = (Integer) currentTable.getClientProperty(TABLE_HOVER_ROW);
                    if (hoverRow != null && hoverRow == row) {
                        renderer.setBackground(AppColors.TABLE_HOVER_BACKGROUND);
                    } else if (row % 2 == 0) {
                        renderer.setBackground(Color.WHITE);
                    } else {
                        renderer.setBackground(AppColors.TABLE_ZEBRA_BACKGROUND);
                    }
                    renderer.setForeground(AppColors.CARD_VALUE_TEXT);
                }
                if (renderer instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return renderer;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setBackground(AppColors.TABLE_HEADER_BACKGROUND);
            header.setForeground(AppColors.CARD_TITLE_TEXT);
            header.setFont(UI_FONT_BOLD);
            header.setReorderingAllowed(false);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.CARD_BORDER));
            header.setPreferredSize(new Dimension(0, 34));
            DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
            headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            headerRenderer.setBackground(AppColors.TABLE_HEADER_BACKGROUND);
            headerRenderer.setForeground(AppColors.CARD_TITLE_TEXT);
            headerRenderer.setFont(UI_FONT_BOLD);
            headerRenderer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.CARD_BORDER),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
            ));
            table.getTableHeader().setDefaultRenderer(headerRenderer);
        }

        table.addMouseMotionListener(new MouseMotionAdapter() {
            /**
             * Xử lý mouse moved.
             */
            @Override
            public void mouseMoved(MouseEvent event) {
                int hoverRow = table.rowAtPoint(event.getPoint());
                if (!Integer.valueOf(hoverRow).equals(table.getClientProperty(TABLE_HOVER_ROW))) {
                    table.putClientProperty(TABLE_HOVER_ROW, hoverRow);
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            /**
             * Xử lý mouse exited.
             */
            @Override
            public void mouseExited(MouseEvent event) {
                table.putClientProperty(TABLE_HOVER_ROW, -1);
                table.repaint();
            }
        });

        table.putClientProperty(TABLE_STYLED, Boolean.TRUE);
    }

    /**
     * Áp dụng kiểu cho scroll pane.
     */
    private static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        JViewport viewport = scrollPane.getViewport();
        if (viewport != null && viewport.getView() != null) {
            viewport.setBackground(viewport.getView().getBackground());
        }
    }

    /**
     * Áp dụng kiểu cho panel.
     */
    private static void stylePanel(JPanel panel) {
        if (Boolean.TRUE.equals(panel.getClientProperty(PANEL_STYLED))) {
            return;
        }

        if (panel instanceof SidebarMenu) {
            panel.putClientProperty(PANEL_STYLED, Boolean.TRUE);
            return;
        }

        if (isCardPanel(panel)) {
            panel.setOpaque(true);
            if (panel.getBackground() == null || panel.getBackground() instanceof UIResource) {
                panel.setBackground(AppColors.CARD_BACKGROUND);
            }
            panel.setBorder(createCardBorder());
        }

        panel.putClientProperty(PANEL_STYLED, Boolean.TRUE);
    }

    /**
     * Áp dụng kiểu cho label.
     */
    private static void styleLabel(JLabel label) {
        if (Boolean.TRUE.equals(label.getClientProperty(LABEL_STYLED))) {
            return;
        }

        Color foreground = label.getForeground();
        boolean preserveSidebarStyle = Color.WHITE.equals(foreground)
                || AppColors.SIDEBAR_TEXT.equals(foreground)
                || AppColors.SIDEBAR_MUTED_TEXT.equals(foreground);
        if (preserveSidebarStyle) {
            label.putClientProperty(LABEL_STYLED, Boolean.TRUE);
            return;
        }

        if (label.getFont() != null && label.getFont().isBold() && label.getFont().getSize2D() >= 16f) {
            styleSectionTitle(label);
        } else {
            label.setFont(UI_FONT);
        }
        label.putClientProperty(LABEL_STYLED, Boolean.TRUE);
    }

    /**
     * Kiểm tra panel card.
     */
    private static boolean isCardPanel(JPanel panel) {
        Border border = panel.getBorder();
        if (!(border instanceof CompoundBorder compoundBorder)) {
            return false;
        }
        if (panel.getBackground() == null) {
            return false;
        }
        if (!(compoundBorder.getOutsideBorder() instanceof LineBorder)) {
            return false;
        }
        if (!(compoundBorder.getInsideBorder() instanceof EmptyBorder)) {
            return false;
        }
        return !AppColors.SIDEBAR_BACKGROUND.equals(panel.getBackground())
                && !AppColors.SIDEBAR_BUTTON.equals(panel.getBackground())
                && !(SwingUtilities.getAncestorOfClass(SidebarMenu.class, panel) != null);
    }

    /**
     * Kiểm tra nút thanh bên.
     */
    private static boolean isSidebarButton(AbstractButton button) {
        return SwingUtilities.getAncestorOfClass(SidebarMenu.class, button) != null;
    }

    /**
     * Xác định nút màu sắc.
     */
    private static Color resolveButtonColor(String text, Color currentColor) {
        if (currentColor != null
                && !currentColor.equals(UIManager.getColor("Button.background"))
                && !(currentColor instanceof UIResource)) {
            return currentColor;
        }

        String normalized = text == null ? "" : text.toLowerCase();
        if (normalized.contains("xóa") || normalized.contains("hủy đăng ký")) {
            return AppColors.BUTTON_DANGER;
        }
        if (normalized.contains("sửa")) {
            return AppColors.BUTTON_WARNING;
        }
        if (normalized.contains("lưu")
                || normalized.contains("thêm")
                || normalized.contains("xác nhận")
                || normalized.contains("tìm")
                || normalized.contains("lọc")) {
            return AppColors.BUTTON_PRIMARY;
        }
        return AppColors.BUTTON_NEUTRAL;
    }

    /**
     * Xác định văn bản màu sắc.
     */
    private static Color resolveTextColor(Color background) {
        return isDark(background) ? AppColors.BUTTON_TEXT : AppColors.CARD_VALUE_TEXT;
    }

    /**
     * Kiểm tra dark.
     */
    private static boolean isDark(Color color) {
        int brightness = (int) Math.sqrt(
                color.getRed() * color.getRed() * .241
                        + color.getGreen() * color.getGreen() * .691
                        + color.getBlue() * color.getBlue() * .068);
        return brightness < 160;
    }

    /**
     * Xử lý adjust.
     */
    private static Color adjust(Color color, float factor) {
        int red = Math.min(255, Math.max(0, Math.round(color.getRed() * factor)));
        int green = Math.min(255, Math.max(0, Math.round(color.getGreen() * factor)));
        int blue = Math.min(255, Math.max(0, Math.round(color.getBlue() * factor)));
        return new Color(red, green, blue);
    }

    private static final class ThemeEventListener implements AWTEventListener {
        /**
         * Xử lý event dispatched.
         */
        @Override
        public void eventDispatched(AWTEvent event) {
            if (event instanceof WindowEvent windowEvent
                    && windowEvent.getID() == WindowEvent.WINDOW_OPENED) {
                applyTree(windowEvent.getWindow());
                return;
            }
            if (event instanceof java.awt.event.ContainerEvent containerEvent
                    && containerEvent.getID() == java.awt.event.ContainerEvent.COMPONENT_ADDED) {
                applyTree(containerEvent.getChild());
            }
        }
    }

    private static final class AppButtonUI extends BasicButtonUI {
        /**
         * Xử lý paint.
         */
        @Override
        public void paint(Graphics graphics, JComponent component) {
            AbstractButton button = (AbstractButton) component;
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color background = button.getBackground() == null ? AppColors.BUTTON_NEUTRAL : button.getBackground();
            if (!button.isEnabled()) {
                background = AppColors.BUTTON_DISABLED;
            } else if (button.getModel().isPressed()) {
                background = adjust(background, 0.88f);
            } else if (button.getModel().isRollover()) {
                background = adjust(background, 1.07f);
            }

            graphics2D.setColor(background);
            graphics2D.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), 12, 12);

            if (button.isFocusOwner()) {
                graphics2D.setColor(AppColors.BUTTON_FOCUS_RING);
                graphics2D.setStroke(new BasicStroke(1.4f));
                graphics2D.drawRoundRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, 12, 12);
            }

            graphics2D.dispose();
            super.paint(graphics, component);
        }
    }

    private static final class FocusBorder extends AbstractBorder {
        private static final Insets INSETS = new Insets(9, 12, 9, 12);

        /**
         * Trả về border insets.
         */
        @Override
        public Insets getBorderInsets(Component component) {
            return INSETS;
        }

        /**
         * Trả về border insets.
         */
        @Override
        public Insets getBorderInsets(Component component, Insets insets) {
            insets.top = INSETS.top;
            insets.left = INSETS.left;
            insets.bottom = INSETS.bottom;
            insets.right = INSETS.right;
            return insets;
        }

        /**
         * Xử lý paint border.
         */
        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean focused = component.isFocusOwner();
            graphics2D.setColor(focused ? AppColors.INPUT_BORDER_FOCUS : AppColors.INPUT_BORDER);
            graphics2D.setStroke(new BasicStroke(focused ? 1.5f : 1f));
            graphics2D.drawRoundRect(x, y, width - 1, height - 1, 12, 12);
            graphics2D.dispose();
        }
    }

    private static final class AppComboBoxUI extends BasicComboBoxUI {
        /**
         * Tạo nút arrow.
         */
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton("▾");
            button.setFocusable(false);
            button.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setForeground(AppColors.CARD_MUTED_TEXT);
            return button;
        }
    }

    private static final class AppComboBoxRenderer extends DefaultListCellRenderer {
        private final ListCellRenderer<Object> delegate;

        /**
         * Xử lý app chọn box renderer.
         */
        private AppComboBoxRenderer(ListCellRenderer<Object> delegate) {
            this.delegate = delegate;
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }

        /**
         * Trả về danh sách cell renderer component.
         */
        @Override
        public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component component;
            if (delegate != null && delegate != this) {
                component = delegate.getListCellRendererComponent(
                        (javax.swing.JList<? extends Object>) list, value, index, isSelected, cellHasFocus
                );
            } else {
                component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }

            if (component instanceof JLabel label) {
                label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                label.setFont(UI_FONT);
                if (!isSelected) {
                    label.setBackground(Color.WHITE);
                    label.setForeground(AppColors.CARD_VALUE_TEXT);
                }
            }
            return component;
        }
    }
}

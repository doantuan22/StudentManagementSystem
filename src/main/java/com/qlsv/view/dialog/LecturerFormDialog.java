/**
 * Hộp thoại giảng viên biểu mẫu dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.model.Faculty;
import com.qlsv.model.Subject;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.FilterOption;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LecturerFormDialog extends JDialog {

    private static final int INPUT_HEIGHT = 40;

    private static final String[] GENDER_OPTIONS = {"Nam", "Nữ", "Khác"};
    @SuppressWarnings("unchecked")
    private static final FilterOption<String>[] STATUS_OPTIONS = new FilterOption[]{
            new FilterOption<>("Đang hoạt động", "ACTIVE"),
            new FilterOption<>("Ngừng hoạt động", "INACTIVE")
    };

    private final JTextField lecturerCodeField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JComboBox<String> genderComboBox = new JComboBox<>(GENDER_OPTIONS);
    private final JTextField dateOfBirthField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextArea addressArea = new JTextArea();
    private final JComboBox<Faculty> facultyComboBox = new JComboBox<>();
    private final JComboBox<FilterOption<String>> statusComboBox = new JComboBox<>(STATUS_OPTIONS);
    private final JPanel selectedSubjectsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel subjectCardsPanel = new JPanel();

    private final List<Subject> availableSubjects = new ArrayList<>();
    private final Map<Long, JCheckBox> subjectCheckboxes = new LinkedHashMap<>();

    private LecturerFormResult result;

    /**
     * Khởi tạo giảng viên biểu mẫu.
     */
    private LecturerFormDialog(Component parent, LecturerFormModel model) {
        super(resolveOwner(parent), model.title(), Dialog.ModalityType.APPLICATION_MODAL);
        initComponents(model);
    }

    /**
     * Hiển thị hộp thoại.
     */
    public static LecturerFormResult showDialog(Component parent, LecturerFormModel model) {
        LecturerFormDialog dialog = new LecturerFormDialog(parent, model);
        dialog.setVisible(true);
        return dialog.result;
    }

    /**
     * Khởi tạo các thành phần giao diện.
     */
    private void initComponents(LecturerFormModel model) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppColors.CARD_BACKGROUND);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 6));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        JLabel titleLabel = new JLabel(model.title());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setOpaque(false);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));
        bodyPanel.add(createBasicInfoSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createWorkInfoSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createTeachingInfoSection());
        bodyPanel.add(Box.createVerticalStrut(24));
        bodyPanel.add(createContactInfoSection());

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JButton cancelButton = new JButton("Hủy");
        styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(event -> {
            result = null;
            dispose();
        });

        JButton saveButton = new JButton("Lưu");
        stylePrimaryButton(saveButton);
        saveButton.addActionListener(event -> handleSave());

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        footerPanel.add(cancelButton);
        footerPanel.add(saveButton);

        rootPanel.add(headerPanel, BorderLayout.NORTH);
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        rootPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        bindModel(model);

        getRootPane().setDefaultButton(saveButton);
        setMinimumSize(new Dimension(820, 760));
        setSize(new Dimension(860, 800));
        setLocationRelativeTo(getOwner());
    }

    /**
     * Xử lý model bind.
     */
    private void bindModel(LecturerFormModel model) {
        lecturerCodeField.setText(model.lecturerCode());
        fullNameField.setText(model.fullName());
        selectGender(model.gender());
        dateOfBirthField.setText(model.dateOfBirth());
        emailField.setText(model.email());
        phoneField.setText(model.phone());
        addressArea.setText(model.address());

        facultyComboBox.removeAllItems();
        for (Faculty faculty : model.faculties()) {
            facultyComboBox.addItem(faculty);
        }
        if (model.selectedFaculty() != null) {
            facultyComboBox.setSelectedItem(model.selectedFaculty());
        }

        rebuildSubjectSelection(model.subjects(), model.selectedSubjects());
        selectStatus(model.status());
        SwingUtilities.invokeLater(() -> lecturerCodeField.requestFocusInWindow());
    }

    /**
     * Tạo basic thông tin phần.
     */
    private JPanel createBasicInfoSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Mã giảng viên", styleTextField(lecturerCodeField)), fieldConstraints(0, 0));
        contentPanel.add(createField("Họ và tên", styleTextField(fullNameField)), fieldConstraints(1, 0));
        contentPanel.add(createField("Giới tính", styleComboBox(genderComboBox)), fieldConstraints(0, 1));
        contentPanel.add(createField("Ngày sinh (yyyy-MM-dd)", styleTextField(dateOfBirthField)), fieldConstraints(1, 1));
        return createSection("Thông tin cơ bản", "Thông tin định danh và nhân sự của giảng viên.", contentPanel);
    }

    /**
     * Tạo work thông tin phần.
     */
    private JPanel createWorkInfoSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Khoa", styleComboBox(facultyComboBox)), fieldConstraints(0, 0));
        contentPanel.add(createField("Trạng thái", styleComboBox(statusComboBox)), fieldConstraints(1, 0));
        return createSection("Thông tin công tác", "Thông tin khoa phụ trách và trạng thái sử dụng tài khoản.", contentPanel);
    }

    /**
     * Tạo teaching thông tin phần.
     */
    private JPanel createTeachingInfoSection() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 14));
        contentPanel.setOpaque(false);

        JPanel selectedWrapper = new JPanel(new BorderLayout(0, 8));
        selectedWrapper.setOpaque(false);
        JLabel selectedLabel = new JLabel("Môn đã chọn");
        selectedLabel.setFont(selectedLabel.getFont().deriveFont(Font.BOLD, 12.5f));
        selectedLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        selectedSubjectsPanel.setOpaque(true);
        selectedSubjectsPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        selectedSubjectsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        selectedWrapper.add(selectedLabel, BorderLayout.NORTH);
        selectedWrapper.add(selectedSubjectsPanel, BorderLayout.CENTER);

        JPanel cardsWrapper = new JPanel(new BorderLayout(0, 8));
        cardsWrapper.setOpaque(false);
        JLabel cardsLabel = new JLabel("Chọn môn giảng dạy");
        cardsLabel.setFont(cardsLabel.getFont().deriveFont(Font.BOLD, 12.5f));
        cardsLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        cardsWrapper.add(cardsLabel, BorderLayout.NORTH);
        cardsWrapper.add(createSubjectCardsScrollPane(), BorderLayout.CENTER);

        contentPanel.add(selectedWrapper, BorderLayout.NORTH);
        contentPanel.add(cardsWrapper, BorderLayout.CENTER);

        return createSection(
                "Môn giảng dạy",
                "Tích chọn nhiều môn học. Ở danh sách đã chọn, bạn có thể bấm Xóa để bỏ từng môn.",
                contentPanel
        );
    }

    /**
     * Tạo contact thông tin phần.
     */
    private JPanel createContactInfoSection() {
        JPanel contentPanel = createFieldGridPanel();
        contentPanel.add(createField("Email", styleTextField(emailField)), fieldConstraints(0, 0));
        contentPanel.add(createField("Số điện thoại", styleTextField(phoneField)), fieldConstraints(1, 0));
        contentPanel.add(createField("Địa chỉ", createAddressScrollPane()), fieldConstraints(0, 1, 2));
        return createSection("Liên hệ", "Thông tin liên lạc và địa chỉ của giảng viên.", contentPanel);
    }

    /**
     * Tạo phần.
     */
    private JPanel createSection(String title, String subtitle, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(true);
        panel.setBackground(AppColors.CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JPanel headingPanel = new JPanel(new BorderLayout(0, 4));
        headingPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        subtitleLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        headingPanel.add(titleLabel, BorderLayout.NORTH);
        headingPanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(headingPanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo panel trường grid.
     */
    private JPanel createFieldGridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Xử lý trường constraints.
     */
    private GridBagConstraints fieldConstraints(int x, int y) {
        return fieldConstraints(x, y, 1);
    }

    /**
     * Xử lý trường constraints.
     */
    private GridBagConstraints fieldConstraints(int x, int y, int width) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.weightx = width;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 12, width == 2 ? 0 : (x == 0 ? 12 : 0));
        return constraints;
    }

    /**
     * Tạo trường.
     */
    private JPanel createField(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12.5f));
        label.setForeground(AppColors.CARD_TITLE_TEXT);

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputComponent, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tạo địa chỉ scroll pane.
     */
    private JScrollPane createAddressScrollPane() {
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setRows(4);
        addressArea.setFont(addressArea.getFont().deriveFont(Font.PLAIN, 13.5f));
        addressArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scrollPane = new JScrollPane(addressArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /**
     * Tạo môn học cards scroll pane.
     */
    private JScrollPane createSubjectCardsScrollPane() {
        subjectCardsPanel.setOpaque(false);
        subjectCardsPanel.setLayout(new BoxLayout(subjectCardsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(subjectCardsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        scrollPane.getViewport().setBackground(AppColors.CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 260));
        return scrollPane;
    }

    /**
     * Áp dụng kiểu cho trường văn bản.
     */
    private JTextField styleTextField(JTextField textField) {
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13.5f));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, INPUT_HEIGHT));
        textField.setMinimumSize(new Dimension(0, INPUT_HEIGHT));
        return textField;
    }

    /**
     * Áp dụng kiểu cho chọn box.
     */
    private <T> JComboBox<T> styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13.5f));
        comboBox.setBorder(BorderFactory.createLineBorder(AppColors.INPUT_BORDER));
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, INPUT_HEIGHT));
        comboBox.setMinimumSize(new Dimension(0, INPUT_HEIGHT));
        return comboBox;
    }

    /**
     * Áp dụng kiểu cho nút primary.
     */
    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    /**
     * Áp dụng kiểu cho nút secondary.
     */
    private void styleSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_NEUTRAL);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    /**
     * Áp dụng kiểu cho nút chip.
     */
    private void styleChipButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_DANGER);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    /**
     * Xử lý select trạng thái.
     */
    private void selectStatus(String statusCode) {
        for (int index = 0; index < statusComboBox.getItemCount(); index++) {
            FilterOption<String> option = statusComboBox.getItemAt(index);
            if (option != null && option.value().equalsIgnoreCase(statusCode == null ? "" : statusCode)) {
                statusComboBox.setSelectedIndex(index);
                return;
            }
        }
        statusComboBox.setSelectedIndex(0);
    }

    /**
     * Xử lý select gender.
     */
    private void selectGender(String gender) {
        String normalizedGender = gender == null ? "" : gender.trim();
        for (int index = 0; index < genderComboBox.getItemCount(); index++) {
            String option = genderComboBox.getItemAt(index);
            if (option != null && option.equalsIgnoreCase(normalizedGender)) {
                genderComboBox.setSelectedIndex(index);
                return;
            }
        }
        if ("Nu".equalsIgnoreCase(normalizedGender)) {
            genderComboBox.setSelectedItem("Nữ");
            return;
        }
        if ("Khac".equalsIgnoreCase(normalizedGender)) {
            genderComboBox.setSelectedItem("Khác");
            return;
        }
        genderComboBox.setSelectedIndex(0);
    }

    /**
     * Xử lý rebuild môn học selection.
     */
    private void rebuildSubjectSelection(List<Subject> subjects, List<Subject> selectedSubjects) {
        availableSubjects.clear();
        subjectCheckboxes.clear();
        subjectCardsPanel.removeAll();

        if (subjects != null) {
            availableSubjects.addAll(subjects);
        }

        for (Subject subject : availableSubjects) {
            JCheckBox checkBox = new JCheckBox(buildSubjectText(subject));
            checkBox.setOpaque(false);
            checkBox.setSelected(selectedSubjects != null && selectedSubjects.contains(subject));
            checkBox.addActionListener(event -> refreshSelectedSubjectsPanel());
            subjectCheckboxes.put(subject.getId(), checkBox);
            subjectCardsPanel.add(createSubjectCard(checkBox));
            subjectCardsPanel.add(Box.createVerticalStrut(8));
        }

        refreshSelectedSubjectsPanel();
        subjectCardsPanel.revalidate();
        subjectCardsPanel.repaint();
    }

    /**
     * Tạo card môn học.
     */
    private JPanel createSubjectCard(JCheckBox checkBox) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(AppColors.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        card.add(checkBox, BorderLayout.CENTER);
        return card;
    }

    /**
     * Xử lý panel refresh đã chọn môn học.
     */
    private void refreshSelectedSubjectsPanel() {
        selectedSubjectsPanel.removeAll();
        List<Subject> selectedSubjects = getSelectedSubjects();
        if (selectedSubjects.isEmpty()) {
            JLabel emptyLabel = new JLabel("Chưa chọn môn giảng dạy nào.");
            emptyLabel.setForeground(AppColors.CARD_MUTED_TEXT);
            selectedSubjectsPanel.add(emptyLabel);
        } else {
            for (Subject subject : selectedSubjects) {
                selectedSubjectsPanel.add(createSelectedSubjectChip(subject));
            }
        }
        selectedSubjectsPanel.revalidate();
        selectedSubjectsPanel.repaint();
    }

    /**
     * Tạo môn học chip đã chọn.
     */
    private JPanel createSelectedSubjectChip(Subject subject) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chip.setOpaque(true);
        chip.setBackground(AppColors.CARD_BACKGROUND);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JLabel label = new JLabel(buildSubjectText(subject));
        label.setForeground(AppColors.CARD_VALUE_TEXT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.5f));

        JButton removeButton = new JButton("Xóa");
        styleChipButton(removeButton);
        removeButton.addActionListener(event -> removeSubjectSelection(subject));

        chip.add(label);
        chip.add(removeButton);
        return chip;
    }

    /**
     * Gỡ môn học selection.
     */
    private void removeSubjectSelection(Subject subject) {
        if (subject == null || subject.getId() == null) {
            return;
        }
        JCheckBox checkBox = subjectCheckboxes.get(subject.getId());
        if (checkBox != null) {
            checkBox.setSelected(false);
        }
        refreshSelectedSubjectsPanel();
    }

    /**
     * Tạo môn học văn bản.
     */
    private String buildSubjectText(Subject subject) {
        if (subject == null) {
            return "";
        }
        String subjectCode = subject.getSubjectCode() == null ? "" : subject.getSubjectCode().trim();
        String subjectName = subject.getSubjectName() == null ? "" : subject.getSubjectName().trim();
        return subjectCode.isBlank() ? subjectName : subjectCode + " - " + subjectName;
    }

    /**
     * Trả về môn học đã chọn.
     */
    private List<Subject> getSelectedSubjects() {
        List<Subject> selectedSubjects = new ArrayList<>();
        for (Subject subject : availableSubjects) {
            if (subject == null || subject.getId() == null) {
                continue;
            }
            JCheckBox checkBox = subjectCheckboxes.get(subject.getId());
            if (checkBox != null && checkBox.isSelected()) {
                selectedSubjects.add(subject);
            }
        }
        return selectedSubjects;
    }

    /**
     * Xử lý lưu.
     */
    private void handleSave() {
        @SuppressWarnings("unchecked")
        FilterOption<String> selectedStatus = (FilterOption<String>) statusComboBox.getSelectedItem();
        result = new LecturerFormResult(
                lecturerCodeField.getText(),
                fullNameField.getText(),
                (String) genderComboBox.getSelectedItem(),
                dateOfBirthField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressArea.getText(),
                (Faculty) facultyComboBox.getSelectedItem(),
                selectedStatus == null ? "ACTIVE" : selectedStatus.value(),
                getSelectedSubjects()
        );
        dispose();
    }

    /**
     * Xác định owner.
     */
    private static Window resolveOwner(Component parent) {
        if (parent == null) {
            return null;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    /**
     * Xử lý model giảng viên biểu mẫu.
     */
    public record LecturerFormModel(
            String title,
            String lecturerCode,
            String fullName,
            String gender,
            String dateOfBirth,
            String email,
            String phone,
            String address,
            List<Faculty> faculties,
            Faculty selectedFaculty,
            String status,
            List<Subject> subjects,
            List<Subject> selectedSubjects
    ) {
    }

    /**
     * Xử lý giảng viên kết quả biểu mẫu.
     */
    public record LecturerFormResult(
            String lecturerCode,
            String fullName,
            String gender,
            String dateOfBirth,
            String email,
            String phone,
            String address,
            Faculty faculty,
            String status,
            List<Subject> subjects
    ) {
    }
}

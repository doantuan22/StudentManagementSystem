package com.qlsv.view.student;

import com.qlsv.controller.StudentController;
import com.qlsv.model.Student;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class StudentProfilePanel extends BasePanel {

    private final StudentController studentController = new StudentController();
    private final JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));

    public StudentProfilePanel() {
        setBackground(AppColors.CONTENT_BACKGROUND);
        initComponents();
        reloadData();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Thông tin cá nhân sinh viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        JButton reloadButton = new JButton("Tải lại");
        reloadButton.addActionListener(event -> reloadData());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(reloadButton, BorderLayout.EAST);

        infoPanel.setBackground(AppColors.CARD_BACKGROUND);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void reloadData() {
        try {
            Student student = studentController.getCurrentStudent();
            infoPanel.removeAll();

            addField(infoPanel, "Mã sinh viên", DisplayTextUtil.defaultText(student.getStudentCode()));
            addField(infoPanel, "Họ và tên", DisplayTextUtil.defaultText(student.getFullName()));
            addField(infoPanel, "Giới tính", DisplayTextUtil.formatGender(student.getGender()));
            addField(infoPanel, "Ngày sinh", DisplayTextUtil.formatDate(student.getDateOfBirth()));
            addField(infoPanel, "Email", DisplayTextUtil.defaultText(student.getEmail()));
            addField(infoPanel, "Số điện thoại", DisplayTextUtil.defaultText(student.getPhone()));
            addField(infoPanel, "Lớp", student.getClassRoom() == null ? "Chưa cập nhật" : student.getClassRoom().getClassName());
            addField(infoPanel, "Khoa", student.getFaculty() == null ? "Chưa cập nhật" : student.getFaculty().getFacultyName());
            addField(infoPanel, "Niên khóa", DisplayTextUtil.defaultText(student.getAcademicYear()));
            addField(infoPanel, "Trạng thái", DisplayTextUtil.formatStatus(student.getStatus()));
            addField(infoPanel, "Địa chỉ", DisplayTextUtil.defaultText(student.getAddress()));
            addField(infoPanel, "Mã người dùng liên kết", DisplayTextUtil.formatUserReference(student.getUserId()));
            addField(infoPanel, "Ghi chú", "Sinh viên hiện chỉ được xem thông tin; nếu cần chỉnh sửa, admin sẽ cập nhật.");

            infoPanel.revalidate();
            infoPanel.repaint();
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }

    private void addField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 13f));

        JLabel valueComponent = new JLabel(value);
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
}

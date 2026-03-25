package com.qlsv.view.student;

import com.qlsv.controller.StudentController;
import com.qlsv.model.Student;
import com.qlsv.utils.DisplayTextUtil;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class StudentProfilePanel extends BasePanel {

    private final StudentController studentController = new StudentController();

    public StudentProfilePanel() {
        try {
            Student student = studentController.getCurrentStudent();
            JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 8));
            infoPanel.add(new JLabel("Mã sinh viên"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(student.getStudentCode())));
            infoPanel.add(new JLabel("Họ và tên"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(student.getFullName())));
            infoPanel.add(new JLabel("Giới tính"));
            infoPanel.add(new JLabel(DisplayTextUtil.formatGender(student.getGender())));
            infoPanel.add(new JLabel("Ngày sinh"));
            infoPanel.add(new JLabel(DisplayTextUtil.formatDate(student.getDateOfBirth())));
            infoPanel.add(new JLabel("Email"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(student.getEmail())));
            infoPanel.add(new JLabel("Số điện thoại"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(student.getPhone())));
            infoPanel.add(new JLabel("Lớp"));
            infoPanel.add(new JLabel(student.getClassRoom() == null ? "Chưa cập nhật" : student.getClassRoom().getClassName()));
            infoPanel.add(new JLabel("Khoa"));
            infoPanel.add(new JLabel(student.getFaculty() == null ? "Chưa cập nhật" : student.getFaculty().getFacultyName()));
            infoPanel.add(new JLabel("Niên khóa"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(student.getAcademicYear())));
            add(infoPanel, java.awt.BorderLayout.NORTH);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}

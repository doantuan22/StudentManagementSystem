package com.qlsv.view.student;

import com.qlsv.controller.StudentController;
import com.qlsv.model.Student;
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
            infoPanel.add(new JLabel("Ma sinh vien"));
            infoPanel.add(new JLabel(student.getStudentCode()));
            infoPanel.add(new JLabel("Ho ten"));
            infoPanel.add(new JLabel(student.getFullName()));
            infoPanel.add(new JLabel("Email"));
            infoPanel.add(new JLabel(student.getEmail()));
            infoPanel.add(new JLabel("So dien thoai"));
            infoPanel.add(new JLabel(student.getPhone()));
            infoPanel.add(new JLabel("Lop"));
            infoPanel.add(new JLabel(student.getClassRoom() == null ? "" : student.getClassRoom().getClassName()));
            infoPanel.add(new JLabel("Khoa"));
            infoPanel.add(new JLabel(student.getFaculty() == null ? "" : student.getFaculty().getFacultyName()));
            add(infoPanel, java.awt.BorderLayout.NORTH);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}

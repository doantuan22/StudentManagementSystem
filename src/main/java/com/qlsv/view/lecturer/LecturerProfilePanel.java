package com.qlsv.view.lecturer;

import com.qlsv.controller.LecturerController;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.common.BasePanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class LecturerProfilePanel extends BasePanel {

    private final LecturerController lecturerController = new LecturerController();

    public LecturerProfilePanel() {
        try {
            Lecturer lecturer = lecturerController.getCurrentLecturer();
            JPanel infoPanel = new JPanel(new GridLayout(0, 2, 8, 8));
            infoPanel.add(new JLabel("Ma giang vien"));
            infoPanel.add(new JLabel(lecturer.getLecturerCode()));
            infoPanel.add(new JLabel("Ho ten"));
            infoPanel.add(new JLabel(lecturer.getFullName()));
            infoPanel.add(new JLabel("Email"));
            infoPanel.add(new JLabel(lecturer.getEmail()));
            infoPanel.add(new JLabel("So dien thoai"));
            infoPanel.add(new JLabel(lecturer.getPhone()));
            infoPanel.add(new JLabel("Khoa"));
            infoPanel.add(new JLabel(lecturer.getFaculty() == null ? "" : lecturer.getFaculty().getFacultyName()));
            add(infoPanel, java.awt.BorderLayout.NORTH);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}

package com.qlsv.view.lecturer;

import com.qlsv.controller.LecturerController;
import com.qlsv.model.Lecturer;
import com.qlsv.utils.DisplayTextUtil;
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
            infoPanel.add(new JLabel("Mã giảng viên"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(lecturer.getLecturerCode())));
            infoPanel.add(new JLabel("Họ và tên"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(lecturer.getFullName())));
            infoPanel.add(new JLabel("Email"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(lecturer.getEmail())));
            infoPanel.add(new JLabel("Số điện thoại"));
            infoPanel.add(new JLabel(DisplayTextUtil.defaultText(lecturer.getPhone())));
            infoPanel.add(new JLabel("Khoa"));
            infoPanel.add(new JLabel(lecturer.getFaculty() == null ? "Chưa cập nhật" : lecturer.getFaculty().getFacultyName()));
            add(infoPanel, java.awt.BorderLayout.NORTH);
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}

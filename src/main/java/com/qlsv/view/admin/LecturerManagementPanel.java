package com.qlsv.view.admin;

import com.qlsv.controller.FacultyController;
import com.qlsv.controller.LecturerController;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class LecturerManagementPanel extends AbstractCrudPanel<Lecturer> {

    private final LecturerController lecturerController = new LecturerController();
    private final FacultyController facultyController = new FacultyController();

    public LecturerManagementPanel() {
        super("Quan ly giang vien");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Ma GV", "Ho ten", "Email", "Khoa", "Trang thai"};
    }

    @Override
    protected List<Lecturer> loadItems() {
        return lecturerController.getAllLecturers();
    }

    @Override
    protected Object[] toRow(Lecturer item) {
        return new Object[]{
                item.getId(),
                item.getLecturerCode(),
                item.getFullName(),
                item.getEmail(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName(),
                item.getStatus()
        };
    }

    @Override
    protected Lecturer promptForEntity(Lecturer existingItem) {
        JTextField userIdField = new JTextField(existingItem == null || existingItem.getUserId() == null ? "" : String.valueOf(existingItem.getUserId()));
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getLecturerCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getFullName());
        JTextField emailField = new JTextField(existingItem == null ? "" : existingItem.getEmail());
        JTextField phoneField = new JTextField(existingItem == null ? "" : existingItem.getPhone());
        JTextField statusField = new JTextField(existingItem == null ? "ACTIVE" : existingItem.getStatus());
        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        if (existingItem != null && existingItem.getFaculty() != null) {
            facultyComboBox.setSelectedItem(existingItem.getFaculty());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("User ID"));
        formPanel.add(userIdField);
        formPanel.add(new JLabel("Ma GV"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Ho ten"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("So dien thoai"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);
        formPanel.add(new JLabel("Trang thai"));
        formPanel.add(statusField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them giang vien" : "Sua giang vien",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Lecturer lecturer = existingItem == null ? new Lecturer() : existingItem;
        lecturer.setUserId(userIdField.getText().isBlank() ? null : Long.parseLong(userIdField.getText().trim()));
        lecturer.setLecturerCode(codeField.getText().trim());
        lecturer.setFullName(nameField.getText().trim());
        lecturer.setEmail(emailField.getText().trim());
        lecturer.setPhone(phoneField.getText().trim());
        lecturer.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        lecturer.setStatus(statusField.getText().trim());
        return lecturer;
    }

    @Override
    protected void saveEntity(Lecturer item) {
        lecturerController.saveLecturer(item);
    }

    @Override
    protected void deleteEntity(Lecturer item) {
        lecturerController.deleteLecturer(item.getId());
    }
}

package com.qlsv.view.admin;

import com.qlsv.controller.FacultyController;
import com.qlsv.model.Faculty;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class FacultyManagementPanel extends AbstractCrudPanel<Faculty> {

    private final FacultyController facultyController = new FacultyController();

    public FacultyManagementPanel() {
        super("Quan ly khoa");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Ma khoa", "Ten khoa", "Mo ta"};
    }

    @Override
    protected List<Faculty> loadItems() {
        return facultyController.getAllFaculties();
    }

    @Override
    protected Object[] toRow(Faculty item) {
        return new Object[]{item.getId(), item.getFacultyCode(), item.getFacultyName(), item.getDescription()};
    }

    @Override
    protected Faculty promptForEntity(Faculty existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getFacultyCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getFacultyName());
        JTextField descriptionField = new JTextField(existingItem == null ? "" : existingItem.getDescription());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Ma khoa"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Ten khoa"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Mo ta"));
        formPanel.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them khoa" : "Sua khoa",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Faculty faculty = existingItem == null ? new Faculty() : existingItem;
        faculty.setFacultyCode(codeField.getText().trim());
        faculty.setFacultyName(nameField.getText().trim());
        faculty.setDescription(descriptionField.getText().trim());
        return faculty;
    }

    @Override
    protected void saveEntity(Faculty item) {
        facultyController.saveFaculty(item);
    }

    @Override
    protected void deleteEntity(Faculty item) {
        facultyController.deleteFaculty(item.getId());
    }
}

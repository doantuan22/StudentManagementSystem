package com.qlsv.view.admin;

import com.qlsv.controller.FacultyController;
import com.qlsv.controller.SubjectController;
import com.qlsv.model.Faculty;
import com.qlsv.model.Subject;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class SubjectManagementPanel extends AbstractCrudPanel<Subject> {

    private final SubjectController subjectController = new SubjectController();
    private final FacultyController facultyController = new FacultyController();

    public SubjectManagementPanel() {
        super("Quan ly mon hoc");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Ma mon", "Ten mon", "Tin chi", "Khoa"};
    }

    @Override
    protected List<Subject> loadItems() {
        return subjectController.getAllSubjects();
    }

    @Override
    protected Object[] toRow(Subject item) {
        return new Object[]{
                item.getId(),
                item.getSubjectCode(),
                item.getSubjectName(),
                item.getCredits(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName()
        };
    }

    @Override
    protected Subject promptForEntity(Subject existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getSubjectCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getSubjectName());
        JTextField creditsField = new JTextField(existingItem == null ? "" : String.valueOf(existingItem.getCredits()));
        JTextField descriptionField = new JTextField(existingItem == null ? "" : existingItem.getDescription());
        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        if (existingItem != null && existingItem.getFaculty() != null) {
            facultyComboBox.setSelectedItem(existingItem.getFaculty());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Ma mon"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Ten mon"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Tin chi"));
        formPanel.add(creditsField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);
        formPanel.add(new JLabel("Mo ta"));
        formPanel.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them mon hoc" : "Sua mon hoc",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Subject subject = existingItem == null ? new Subject() : existingItem;
        subject.setSubjectCode(codeField.getText().trim());
        subject.setSubjectName(nameField.getText().trim());
        subject.setCredits(Integer.parseInt(creditsField.getText().trim()));
        subject.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        subject.setDescription(descriptionField.getText().trim());
        return subject;
    }

    @Override
    protected void saveEntity(Subject item) {
        subjectController.saveSubject(item);
    }

    @Override
    protected void deleteEntity(Subject item) {
        subjectController.deleteSubject(item.getId());
    }
}

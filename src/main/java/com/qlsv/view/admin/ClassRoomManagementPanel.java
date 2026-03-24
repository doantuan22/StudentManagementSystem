package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.FacultyController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class ClassRoomManagementPanel extends AbstractCrudPanel<ClassRoom> {

    private final ClassRoomController classRoomController = new ClassRoomController();
    private final FacultyController facultyController = new FacultyController();

    public ClassRoomManagementPanel() {
        super("Quan ly lop");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Ma lop", "Ten lop", "Nien khoa", "Khoa"};
    }

    @Override
    protected List<ClassRoom> loadItems() {
        return classRoomController.getAllClassRooms();
    }

    @Override
    protected Object[] toRow(ClassRoom item) {
        return new Object[]{
                item.getId(),
                item.getClassCode(),
                item.getClassName(),
                item.getAcademicYear(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName()
        };
    }

    @Override
    protected ClassRoom promptForEntity(ClassRoom existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getClassCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getClassName());
        JTextField yearField = new JTextField(existingItem == null ? "" : existingItem.getAcademicYear());
        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        if (existingItem != null && existingItem.getFaculty() != null) {
            facultyComboBox.setSelectedItem(existingItem.getFaculty());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Ma lop"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Ten lop"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Nien khoa"));
        formPanel.add(yearField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them lop" : "Sua lop",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        ClassRoom classRoom = existingItem == null ? new ClassRoom() : existingItem;
        classRoom.setClassCode(codeField.getText().trim());
        classRoom.setClassName(nameField.getText().trim());
        classRoom.setAcademicYear(yearField.getText().trim());
        classRoom.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        return classRoom;
    }

    @Override
    protected void saveEntity(ClassRoom item) {
        classRoomController.saveClassRoom(item);
    }

    @Override
    protected void deleteEntity(ClassRoom item) {
        classRoomController.deleteClassRoom(item.getId());
    }
}

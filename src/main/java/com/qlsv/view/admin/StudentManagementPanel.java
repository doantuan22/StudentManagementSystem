package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.FacultyController;
import com.qlsv.controller.StudentController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

public class StudentManagementPanel extends AbstractCrudPanel<Student> {

    private final StudentController studentController = new StudentController();
    private final FacultyController facultyController = new FacultyController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    public StudentManagementPanel() {
        super("Quan ly sinh vien");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Ma SV", "Ho ten", "Email", "Lop", "Khoa", "Trang thai"};
    }

    @Override
    protected List<Student> loadItems() {
        return studentController.getAllStudents();
    }

    @Override
    protected Object[] toRow(Student item) {
        return new Object[]{
                item.getId(),
                item.getStudentCode(),
                item.getFullName(),
                item.getEmail(),
                item.getClassRoom() == null ? "" : item.getClassRoom().getClassName(),
                item.getFaculty() == null ? "" : item.getFaculty().getFacultyName(),
                item.getStatus()
        };
    }

    @Override
    protected Student promptForEntity(Student existingItem) {
        JTextField userIdField = new JTextField(existingItem == null || existingItem.getUserId() == null ? "" : String.valueOf(existingItem.getUserId()));
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getStudentCode());
        JTextField nameField = new JTextField(existingItem == null ? "" : existingItem.getFullName());
        JTextField genderField = new JTextField(existingItem == null ? "" : existingItem.getGender());
        JTextField birthField = new JTextField(existingItem == null || existingItem.getDateOfBirth() == null ? "" : existingItem.getDateOfBirth().toString());
        JTextField emailField = new JTextField(existingItem == null ? "" : existingItem.getEmail());
        JTextField phoneField = new JTextField(existingItem == null ? "" : existingItem.getPhone());
        JTextField statusField = new JTextField(existingItem == null ? "ACTIVE" : existingItem.getStatus());

        JComboBox<Faculty> facultyComboBox = new JComboBox<>(facultyController.getFacultiesForSelection().toArray(new Faculty[0]));
        JComboBox<ClassRoom> classRoomComboBox = new JComboBox<>(classRoomController.getClassRoomsForSelection().toArray(new ClassRoom[0]));
        if (existingItem != null) {
            if (existingItem.getFaculty() != null) {
                facultyComboBox.setSelectedItem(existingItem.getFaculty());
            }
            if (existingItem.getClassRoom() != null) {
                classRoomComboBox.setSelectedItem(existingItem.getClassRoom());
            }
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("User ID"));
        formPanel.add(userIdField);
        formPanel.add(new JLabel("Ma SV"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Ho ten"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Gioi tinh"));
        formPanel.add(genderField);
        formPanel.add(new JLabel("Ngay sinh (yyyy-MM-dd)"));
        formPanel.add(birthField);
        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("So dien thoai"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Khoa"));
        formPanel.add(facultyComboBox);
        formPanel.add(new JLabel("Lop"));
        formPanel.add(classRoomComboBox);
        formPanel.add(new JLabel("Trang thai"));
        formPanel.add(statusField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them sinh vien" : "Sua sinh vien",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Student student = existingItem == null ? new Student() : existingItem;
        student.setUserId(userIdField.getText().isBlank() ? null : Long.parseLong(userIdField.getText().trim()));
        student.setStudentCode(codeField.getText().trim());
        student.setFullName(nameField.getText().trim());
        student.setGender(genderField.getText().trim());
        student.setDateOfBirth(birthField.getText().isBlank() ? null : LocalDate.parse(birthField.getText().trim()));
        student.setEmail(emailField.getText().trim());
        student.setPhone(phoneField.getText().trim());
        student.setFaculty((Faculty) facultyComboBox.getSelectedItem());
        student.setClassRoom((ClassRoom) classRoomComboBox.getSelectedItem());
        student.setStatus(statusField.getText().trim());
        return student;
    }

    @Override
    protected void saveEntity(Student item) {
        studentController.saveStudent(item);
    }

    @Override
    protected void deleteEntity(Student item) {
        studentController.deleteStudent(item.getId());
    }
}

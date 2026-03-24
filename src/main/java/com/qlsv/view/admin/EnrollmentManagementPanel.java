package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.StudentController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Student;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;

public class EnrollmentManagementPanel extends AbstractCrudPanel<Enrollment> {

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final StudentController studentController = new StudentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    public EnrollmentManagementPanel() {
        super("Quan ly dang ky hoc phan");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Sinh vien", "Hoc phan", "Trang thai", "Thoi gian"};
    }

    @Override
    protected List<Enrollment> loadItems() {
        return enrollmentController.getAllEnrollments();
    }

    @Override
    protected Object[] toRow(Enrollment item) {
        return new Object[]{
                item.getId(),
                item.getStudent() == null ? "" : item.getStudent().getFullName(),
                item.getCourseSection() == null ? "" : item.getCourseSection().getSectionCode(),
                item.getStatus(),
                item.getEnrolledAt()
        };
    }

    @Override
    protected Enrollment promptForEntity(Enrollment existingItem) {
        JComboBox<Student> studentComboBox = new JComboBox<>(studentController.getStudentsForSelection().toArray(new Student[0]));
        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(courseSectionController.getAllCourseSectionsForSelection().toArray(new CourseSection[0]));
        JTextField statusField = new JTextField(existingItem == null ? "REGISTERED" : existingItem.getStatus());

        if (existingItem != null) {
            if (existingItem.getStudent() != null) {
                studentComboBox.setSelectedItem(existingItem.getStudent());
            }
            if (existingItem.getCourseSection() != null) {
                sectionComboBox.setSelectedItem(existingItem.getCourseSection());
            }
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Sinh vien"));
        formPanel.add(studentComboBox);
        formPanel.add(new JLabel("Hoc phan"));
        formPanel.add(sectionComboBox);
        formPanel.add(new JLabel("Trang thai"));
        formPanel.add(statusField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them dang ky" : "Sua dang ky",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Enrollment enrollment = existingItem == null ? new Enrollment() : existingItem;
        enrollment.setStudent((Student) studentComboBox.getSelectedItem());
        enrollment.setCourseSection((CourseSection) sectionComboBox.getSelectedItem());
        enrollment.setStatus(statusField.getText().trim());
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(LocalDateTime.now());
        }
        return enrollment;
    }

    @Override
    protected void saveEntity(Enrollment item) {
        enrollmentController.saveEnrollment(item);
    }

    @Override
    protected void deleteEntity(Enrollment item) {
        enrollmentController.deleteEnrollment(item.getId());
    }
}

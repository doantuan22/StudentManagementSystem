package com.qlsv.view.admin;

import com.qlsv.controller.ClassRoomController;
import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.LecturerController;
import com.qlsv.controller.SubjectController;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Subject;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class CourseSectionManagementPanel extends AbstractCrudPanel<CourseSection> {

    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final SubjectController subjectController = new SubjectController();
    private final LecturerController lecturerController = new LecturerController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    public CourseSectionManagementPanel() {
        super("Quan ly hoc phan");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Ma hoc phan", "Mon hoc", "Giang vien", "Lop", "Hoc ky", "Nam hoc"};
    }

    @Override
    protected List<CourseSection> loadItems() {
        return courseSectionController.getAllCourseSections();
    }

    @Override
    protected Object[] toRow(CourseSection item) {
        return new Object[]{
                item.getId(),
                item.getSectionCode(),
                item.getSubject() == null ? "" : item.getSubject().getSubjectName(),
                item.getLecturer() == null ? "" : item.getLecturer().getFullName(),
                item.getClassRoom() == null ? "" : item.getClassRoom().getClassName(),
                item.getSemester(),
                item.getSchoolYear()
        };
    }

    @Override
    protected CourseSection promptForEntity(CourseSection existingItem) {
        JTextField codeField = new JTextField(existingItem == null ? "" : existingItem.getSectionCode());
        JTextField semesterField = new JTextField(existingItem == null ? "HK1" : existingItem.getSemester());
        JTextField schoolYearField = new JTextField(existingItem == null ? "2025-2026" : existingItem.getSchoolYear());
        JTextField scheduleField = new JTextField(existingItem == null ? "" : existingItem.getScheduleText());
        JTextField maxStudentsField = new JTextField(existingItem == null ? "50" : String.valueOf(existingItem.getMaxStudents()));

        JComboBox<Subject> subjectComboBox = new JComboBox<>(subjectController.getSubjectsForSelection().toArray(new Subject[0]));
        JComboBox<Lecturer> lecturerComboBox = new JComboBox<>(lecturerController.getLecturersForSelection().toArray(new Lecturer[0]));
        JComboBox<ClassRoom> classRoomComboBox = new JComboBox<>(classRoomController.getClassRoomsForSelection().toArray(new ClassRoom[0]));

        if (existingItem != null) {
            if (existingItem.getSubject() != null) {
                subjectComboBox.setSelectedItem(existingItem.getSubject());
            }
            if (existingItem.getLecturer() != null) {
                lecturerComboBox.setSelectedItem(existingItem.getLecturer());
            }
            if (existingItem.getClassRoom() != null) {
                classRoomComboBox.setSelectedItem(existingItem.getClassRoom());
            }
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Ma hoc phan"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Mon hoc"));
        formPanel.add(subjectComboBox);
        formPanel.add(new JLabel("Giang vien"));
        formPanel.add(lecturerComboBox);
        formPanel.add(new JLabel("Lop"));
        formPanel.add(classRoomComboBox);
        formPanel.add(new JLabel("Hoc ky"));
        formPanel.add(semesterField);
        formPanel.add(new JLabel("Nam hoc"));
        formPanel.add(schoolYearField);
        formPanel.add(new JLabel("Lich hoc"));
        formPanel.add(scheduleField);
        formPanel.add(new JLabel("Si so toi da"));
        formPanel.add(maxStudentsField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them hoc phan" : "Sua hoc phan",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        CourseSection courseSection = existingItem == null ? new CourseSection() : existingItem;
        courseSection.setSectionCode(codeField.getText().trim());
        courseSection.setSubject((Subject) subjectComboBox.getSelectedItem());
        courseSection.setLecturer((Lecturer) lecturerComboBox.getSelectedItem());
        courseSection.setClassRoom((ClassRoom) classRoomComboBox.getSelectedItem());
        courseSection.setSemester(semesterField.getText().trim());
        courseSection.setSchoolYear(schoolYearField.getText().trim());
        courseSection.setScheduleText(scheduleField.getText().trim());
        courseSection.setMaxStudents(Integer.parseInt(maxStudentsField.getText().trim()));
        return courseSection;
    }

    @Override
    protected void saveEntity(CourseSection item) {
        courseSectionController.saveCourseSection(item);
    }

    @Override
    protected void deleteEntity(CourseSection item) {
        courseSectionController.deleteCourseSection(item.getId());
    }
}

package com.qlsv.view.admin;

import com.qlsv.controller.CourseSectionController;
import com.qlsv.controller.ScheduleController;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Schedule;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class ScheduleManagementPanel extends AbstractCrudPanel<Schedule> {

    private final ScheduleController scheduleController = new ScheduleController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    public ScheduleManagementPanel() {
        super("Quan ly lich hoc");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Hoc phan", "Mon hoc", "Giang vien", "Thu", "Tiet", "Phong", "Ghi chu"};
    }

    @Override
    protected List<Schedule> loadItems() {
        return scheduleController.getAllSchedules();
    }

    @Override
    protected Object[] toRow(Schedule item) {
        CourseSection courseSection = item.getCourseSection();
        return new Object[]{
                item.getId(),
                courseSection == null ? "" : courseSection.getSectionCode(),
                courseSection == null || courseSection.getSubject() == null ? "" : courseSection.getSubject().getSubjectName(),
                courseSection == null || courseSection.getLecturer() == null ? "" : courseSection.getLecturer().getFullName(),
                item.getDayOfWeek(),
                item.getStartPeriod() + "-" + item.getEndPeriod(),
                item.getRoom(),
                item.getNote()
        };
    }

    @Override
    protected Schedule promptForEntity(Schedule existingItem) {
        JComboBox<CourseSection> sectionComboBox = new JComboBox<>(
                courseSectionController.getAllCourseSectionsForSelection().toArray(new CourseSection[0]));
        JTextField dayField = new JTextField(existingItem == null ? "Thu 2" : existingItem.getDayOfWeek());
        JTextField startPeriodField = new JTextField(existingItem == null ? "1" : String.valueOf(existingItem.getStartPeriod()));
        JTextField endPeriodField = new JTextField(existingItem == null ? "3" : String.valueOf(existingItem.getEndPeriod()));
        JTextField roomField = new JTextField(existingItem == null ? "" : existingItem.getRoom());
        JTextField noteField = new JTextField(existingItem == null ? "" : existingItem.getNote());

        if (existingItem != null && existingItem.getCourseSection() != null) {
            sectionComboBox.setSelectedItem(existingItem.getCourseSection());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Hoc phan"));
        formPanel.add(sectionComboBox);
        formPanel.add(new JLabel("Thu hoc"));
        formPanel.add(dayField);
        formPanel.add(new JLabel("Tiet bat dau"));
        formPanel.add(startPeriodField);
        formPanel.add(new JLabel("Tiet ket thuc"));
        formPanel.add(endPeriodField);
        formPanel.add(new JLabel("Phong hoc"));
        formPanel.add(roomField);
        formPanel.add(new JLabel("Ghi chu"));
        formPanel.add(noteField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them lich hoc" : "Sua lich hoc",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Schedule schedule = existingItem == null ? new Schedule() : existingItem;
        schedule.setCourseSection((CourseSection) sectionComboBox.getSelectedItem());
        schedule.setDayOfWeek(dayField.getText().trim());
        schedule.setStartPeriod(Integer.parseInt(startPeriodField.getText().trim()));
        schedule.setEndPeriod(Integer.parseInt(endPeriodField.getText().trim()));
        schedule.setRoom(roomField.getText().trim());
        schedule.setNote(noteField.getText().trim());
        return schedule;
    }

    @Override
    protected void saveEntity(Schedule item) {
        scheduleController.saveSchedule(item);
    }

    @Override
    protected void deleteEntity(Schedule item) {
        scheduleController.deleteSchedule(item.getId());
    }
}

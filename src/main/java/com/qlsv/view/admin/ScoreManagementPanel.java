package com.qlsv.view.admin;

import com.qlsv.controller.EnrollmentController;
import com.qlsv.controller.ScoreController;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Score;
import com.qlsv.view.common.AbstractCrudPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;

public class ScoreManagementPanel extends AbstractCrudPanel<Score> {

    private final ScoreController scoreController = new ScoreController();
    private final EnrollmentController enrollmentController = new EnrollmentController();

    public ScoreManagementPanel() {
        super("Quan ly diem");
        refreshData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Sinh vien", "Hoc phan", "QT", "GK", "CK", "Tong ket", "Ket qua"};
    }

    @Override
    protected List<Score> loadItems() {
        return scoreController.getAllScores();
    }

    @Override
    protected Object[] toRow(Score item) {
        Enrollment enrollment = item.getEnrollment();
        String studentName = enrollment == null || enrollment.getStudent() == null ? "" : enrollment.getStudent().getFullName();
        String sectionCode = enrollment == null || enrollment.getCourseSection() == null ? "" : enrollment.getCourseSection().getSectionCode();
        return new Object[]{
                item.getId(),
                studentName,
                sectionCode,
                item.getProcessScore(),
                item.getMidtermScore(),
                item.getFinalScore(),
                item.getTotalScore(),
                item.getResult()
        };
    }

    @Override
    protected Score promptForEntity(Score existingItem) {
        JComboBox<Enrollment> enrollmentComboBox = new JComboBox<>(enrollmentController.getAllEnrollments().toArray(new Enrollment[0]));
        JTextField processField = new JTextField(existingItem == null || existingItem.getProcessScore() == null ? "" : String.valueOf(existingItem.getProcessScore()));
        JTextField midtermField = new JTextField(existingItem == null || existingItem.getMidtermScore() == null ? "" : String.valueOf(existingItem.getMidtermScore()));
        JTextField finalField = new JTextField(existingItem == null || existingItem.getFinalScore() == null ? "" : String.valueOf(existingItem.getFinalScore()));

        if (existingItem != null && existingItem.getEnrollment() != null) {
            enrollmentComboBox.setSelectedItem(existingItem.getEnrollment());
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Dang ky hoc phan"));
        formPanel.add(enrollmentComboBox);
        formPanel.add(new JLabel("Diem qua trinh"));
        formPanel.add(processField);
        formPanel.add(new JLabel("Diem giua ky"));
        formPanel.add(midtermField);
        formPanel.add(new JLabel("Diem cuoi ky"));
        formPanel.add(finalField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, existingItem == null ? "Them diem" : "Sua diem",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Score score = existingItem == null ? new Score() : existingItem;
        score.setEnrollment((Enrollment) enrollmentComboBox.getSelectedItem());
        score.setProcessScore(parseScore(processField.getText()));
        score.setMidtermScore(parseScore(midtermField.getText()));
        score.setFinalScore(parseScore(finalField.getText()));
        return score;
    }

    @Override
    protected void saveEntity(Score item) {
        scoreController.saveScore(item);
    }

    @Override
    protected void deleteEntity(Score item) {
        scoreController.deleteScore(item.getId());
    }

    private Double parseScore(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? 0.0 : Double.parseDouble(rawValue.trim());
    }
}

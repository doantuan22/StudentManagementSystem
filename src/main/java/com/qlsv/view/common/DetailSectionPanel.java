package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

public class DetailSectionPanel extends JPanel {

    private final JLabel titleLabel;
    private final JPanel bodyPanel = new JPanel(new BorderLayout());

    public DetailSectionPanel(String title, String emptyMessage) {
        setLayout(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));

        Border outerBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(217, 217, 217)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
        setBorder(outerBorder);

        titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));

        bodyPanel.setOpaque(false);
        add(titleLabel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);

        showMessage(emptyMessage);
    }

    public void setSectionTitle(String title) {
        titleLabel.setText(title);
    }

    public void showMessage(String message) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.ITALIC, 14f));
        messageLabel.setForeground(new Color(90, 90, 90));
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        bodyPanel.removeAll();
        bodyPanel.add(messagePanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void showFields(String[][] fields) {
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        gridPanel.setOpaque(false);

        for (String[] field : fields) {
            String label = field.length > 0 ? field[0] : "";
            String value = field.length > 1 ? field[1] : "";
            gridPanel.add(createFieldCard(label, value));
        }

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(gridPanel);

        bodyPanel.removeAll();
        bodyPanel.add(container, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    private JPanel createFieldCard(String label, String value) {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 6));
        cardPanel.setOpaque(true);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12.5f));
        labelComponent.setForeground(new Color(70, 70, 70));

        JLabel valueComponent = new JLabel(toHtml(value));
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.PLAIN, 13.5f));

        cardPanel.add(labelComponent, BorderLayout.NORTH);
        cardPanel.add(valueComponent, BorderLayout.CENTER);
        return cardPanel;
    }

    private String toHtml(String value) {
        String escapedValue = value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
        return "<html><div style='width:220px;'>" + escapedValue + "</div></html>";
    }
}

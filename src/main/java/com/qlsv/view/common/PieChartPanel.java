package com.qlsv.view.common;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple pie chart component for showing statistics.
 */
public class PieChartPanel extends JPanel {

    private final List<Slice> slices = new ArrayList<>();
    private String title = "Thống kê";

    public PieChartPanel() {
        setOpaque(true);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(300, 300));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void clearSlices() {
        slices.clear();
    }

    public void addSlice(String name, double value, Color color) {
        slices.add(new Slice(name, value, color));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int margin = 50;
        int size = Math.min(width, height) - margin * 2;
        
        // Shift chart to the left (target ~20% of width)
        int x = (int)(width * 0.12); 
        int y = (height - size) / 2 + 10;

        double total = 0;
        for (Slice slice : slices) {
            total += slice.value;
        }

        int startAngle = 90;

        // Draw title
        g2.setColor(AppColors.CARD_TITLE_TEXT);
        g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (width - titleWidth) / 2, 35);

        if (total == 0) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawOval(x, y, size, size);
            String emptyMsg = "Không có dữ liệu";
            int msgWidth = g2.getFontMetrics().stringWidth(emptyMsg);
            g2.drawString(emptyMsg, x + (size - msgWidth) / 2, y + size / 2);
        } else {
            for (int i = 0; i < slices.size(); i++) {
                Slice slice = slices.get(i);
                int angle = (int) Math.round((slice.value * 360) / total);
                
                if (i == slices.size() - 1) {
                    int used = 0;
                    for(int j=0; j<i; j++) {
                        used += (int) Math.round((slices.get(j).value * 360) / total);
                    }
                    angle = 360 - used;
                }

                g2.setColor(slice.color);
                g2.fillArc(x, y, size, size, startAngle, -angle);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(1.8f));
                double rad = Math.toRadians(startAngle);
                g2.drawLine(x + size / 2, y + size / 2, 
                           (int)(x + size / 2 + (size / 2) * Math.cos(rad)), 
                           (int)(y + size / 2 - (size / 2) * Math.sin(rad)));

                startAngle -= angle;
            }

            g2.setColor(Color.WHITE);
            int innerSize = (int)(size * 0.55);
            g2.fillOval(x + (size - innerSize) / 2, y + (size - innerSize) / 2, innerSize, innerSize);
            
            drawLegend(g2, width, height, x, y, size);
        }

        g2.dispose();
    }

    private void drawLegend(Graphics2D g2, int width, int height, int pieX, int pieY, int pieSize) {
        // Move legend further right to separate from chart
        int legendX = pieX + pieSize + (int)(width * 0.1); 
        int legendY = pieY + (pieSize / 4);
        int boxSize = 14;
        
        g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
        
        for (Slice slice : slices) {
            g2.setColor(slice.color);
            g2.fillRoundRect(legendX, legendY, boxSize, boxSize, 4, 4);
            
            g2.setColor(AppColors.CARD_TITLE_TEXT);
            String label = String.format("%s (%d)", slice.name, (int)slice.value);
            g2.drawString(label, legendX + boxSize + 12, legendY + boxSize - 1);
            
            legendY += 30;
        }
    }

    private static class Slice {
        String name;
        double value;
        Color color;

        Slice(String name, double value, Color color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
}

package com.qlsv.view.common;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * Biểu đồ cột để hiển thị thống kê dữ liệu hệ thống.
 * Thay thế biểu đồ tròn để dễ so sánh giữa các nhóm dữ liệu.
 */
public class BarChartPanel extends JPanel {

    private final List<Bar> bars = new ArrayList<>();
    private String title = "Thống kê";

    public BarChartPanel() {
        setOpaque(true);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 480));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void clearBars() {
        bars.clear();
    }

    public void addBar(String name, double value, Color color) {
        bars.add(new Bar(name, value, color));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        
        // Vẽ tiêu đề
        g2.setColor(AppColors.CARD_TITLE_TEXT);
        g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleWidth = titleMetrics.stringWidth(title);
        g2.drawString(title, (width - titleWidth) / 2, 40);

        if (bars.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            String emptyMsg = "Không có dữ liệu";
            int msgWidth = g2.getFontMetrics().stringWidth(emptyMsg);
            g2.drawString(emptyMsg, (width - msgWidth) / 2, height / 2);
            g2.dispose();
            return;
        }

        // Tính toán vùng vẽ biểu đồ
        int topMargin = 70;
        int bottomMargin = 80;
        int leftMargin = 60;
        int rightMargin = 40;
        
        int chartWidth = width - leftMargin - rightMargin;
        int chartHeight = height - topMargin - bottomMargin;

        // Tìm giá trị lớn nhất
        double maxValue = 0;
        for (Bar bar : bars) {
            if (bar.value > maxValue) {
                maxValue = bar.value;
            }
        }

        // Làm tròn maxValue lên để trục Y đẹp hơn
        maxValue = Math.ceil(maxValue / 10.0) * 10;
        if (maxValue == 0) {
            maxValue = 10;
        }

        // Vẽ trục tọa độ
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        
        // Trục Y (dọc)
        g2.drawLine(leftMargin, topMargin, leftMargin, topMargin + chartHeight);
        
        // Trục X (ngang)
        g2.drawLine(leftMargin, topMargin + chartHeight, leftMargin + chartWidth, topMargin + chartHeight);

        // Vẽ các đường lưới ngang và nhãn trục Y
        g2.setColor(new Color(230, 230, 230));
        g2.setStroke(new java.awt.BasicStroke(1f));
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = topMargin + chartHeight - (i * chartHeight / gridLines);
            double value = (maxValue * i) / gridLines;
            
            // Đường lưới
            if (i > 0) {
                g2.drawLine(leftMargin, y, leftMargin + chartWidth, y);
            }
            
            // Nhãn giá trị
            g2.setColor(AppColors.CARD_MUTED_TEXT);
            String label = String.format("%.0f", value);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, leftMargin - fm.stringWidth(label) - 8, y + 4);
            g2.setColor(new Color(230, 230, 230));
        }

        // Tính toán kích thước và vị trí các cột
        int barCount = bars.size();
        int totalGapWidth = chartWidth / 8; // 12.5% cho khoảng cách
        int barWidth = (chartWidth - totalGapWidth) / barCount;
        int gap = totalGapWidth / (barCount + 1);

        // Vẽ các cột
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics labelMetrics = g2.getFontMetrics();
        
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            
            int x = leftMargin + gap + i * (barWidth + gap);
            int barHeight = (int) ((bar.value / maxValue) * chartHeight);
            int y = topMargin + chartHeight - barHeight;

            // Vẽ cột với gradient nhẹ
            Color baseColor = bar.color;
            Color lightColor = new Color(
                Math.min(255, baseColor.getRed() + 30),
                Math.min(255, baseColor.getGreen() + 30),
                Math.min(255, baseColor.getBlue() + 30)
            );
            
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                x, y, lightColor,
                x, y + barHeight, baseColor
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

            // Viền cột
            g2.setColor(baseColor.darker());
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawRoundRect(x, y, barWidth, barHeight, 6, 6);

            // Hiển thị giá trị trên đầu cột
            g2.setColor(AppColors.CARD_TITLE_TEXT);
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            String valueStr = String.format("%.0f", bar.value);
            int valueWidth = g2.getFontMetrics().stringWidth(valueStr);
            g2.drawString(valueStr, x + (barWidth - valueWidth) / 2, y - 8);

            // Nhãn tên dưới cột
            g2.setColor(AppColors.CARD_TITLE_TEXT);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            int labelWidth = labelMetrics.stringWidth(bar.name);
            
            // Nếu nhãn quá dài, xoay 45 độ
            if (labelWidth > barWidth + 10) {
                g2.rotate(-Math.PI / 4, x + barWidth / 2, topMargin + chartHeight + 15);
                g2.drawString(bar.name, x + barWidth / 2, topMargin + chartHeight + 15);
                g2.rotate(Math.PI / 4, x + barWidth / 2, topMargin + chartHeight + 15);
            } else {
                g2.drawString(bar.name, x + (barWidth - labelWidth) / 2, topMargin + chartHeight + 25);
            }
        }

        g2.dispose();
    }

    private static class Bar {
        String name;
        double value;
        Color color;

        Bar(String name, double value, Color color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
}

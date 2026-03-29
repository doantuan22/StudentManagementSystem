package com.qlsv.utils;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.BasicStroke;

/**
 * Utility class to provide vector-based icons using Graphics2D.
 */
public class IconUtil {

    public enum IconType {
        DASHBOARD, STUDENTS, LECTURERS, FACULTY, CLASSES, ROOMS, SUBJECTS, SECTIONS, 
        ENROLLMENT, SCORES, SCHEDULE, REPORTS, PROFILE, PASSWORD, LOCK, CHECK, USERS, USER_GROUP,
        BUILDING, BOOK, CALENDAR, CLOCK, CHART_PIE, CHART_BAR, FILE_TEXT, LOGOUT
    }

    public static Icon getIcon(IconType type, int size) {
        return new VectorIcon(type, size);
    }

    private static class VectorIcon implements Icon {
        private final IconType type;
        private final int size;

        public VectorIcon(IconType type, int size) {
            this.type = type;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            
            g2.translate(x, y);
            float scale = size / 24.0f;
            g2.scale(scale, scale);
            
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            drawPath(g2);
            
            g2.dispose();
        }

        private void drawPath(Graphics2D g2) {
            Path2D path = new Path2D.Float();
            switch (type) {
                case DASHBOARD:
                    // Outline of a dashboard/grid
                    g2.drawRect(3, 3, 7, 7);
                    g2.drawRect(14, 3, 7, 7);
                    g2.drawRect(3, 14, 7, 7);
                    g2.drawRect(14, 14, 7, 7);
                    break;
                case STUDENTS:
                case USERS:
                case USER_GROUP:
                    // Simplified users icon
                    g2.drawOval(8, 3, 8, 8); // Head
                    path.moveTo(4, 20);
                    path.curveTo(4, 15, 12, 13, 12, 13);
                    path.curveTo(12, 13, 20, 15, 20, 20);
                    g2.draw(path);
                    break;
                case LECTURERS:
                    // User with a badge/briefcase feel
                    g2.drawOval(8, 3, 8, 8);
                    path.moveTo(4, 20);
                    path.lineTo(20, 20);
                    path.lineTo(20, 18);
                    path.curveTo(20, 15, 16, 13, 12, 13);
                    path.curveTo(8, 13, 4, 15, 4, 18);
                    path.closePath();
                    g2.draw(path);
                    g2.drawLine(12, 13, 12, 16);
                    break;
                case FACULTY:
                case BUILDING:
                    // Building icon
                    path.moveTo(3, 21);
                    path.lineTo(21, 21);
                    path.lineTo(21, 7);
                    path.lineTo(12, 3);
                    path.lineTo(3, 7);
                    path.closePath();
                    g2.draw(path);
                    g2.drawRect(9, 13, 6, 8);
                    break;
                case CLASSES:
                    // School icon
                    path.moveTo(2, 21);
                    path.lineTo(22, 21);
                    path.moveTo(4, 21);
                    path.lineTo(4, 10);
                    path.lineTo(12, 6);
                    path.lineTo(20, 10);
                    path.lineTo(20, 21);
                    g2.draw(path);
                    g2.drawOval(10, 10, 4, 4);
                    break;
                case ROOMS:
                    // Door or door-open icon
                    g2.drawRect(5, 3, 14, 18);
                    g2.drawOval(15, 12, 2, 2);
                    break;
                case SUBJECTS:
                   case BOOK:
                    // Book icon
                    path.moveTo(4, 19);
                    path.curveTo(4, 17, 6, 17, 6, 17);
                    path.lineTo(20, 17);
                    path.lineTo(20, 5);
                    path.lineTo(6, 5);
                    path.curveTo(4, 5, 4, 7, 4, 7);
                    path.closePath();
                    g2.draw(path);
                    g2.drawLine(6, 5, 6, 17);
                    break;
                case SECTIONS:
                    // Layers or folder
                    g2.drawRect(4, 7, 16, 12);
                    path.moveTo(4, 7);
                    path.lineTo(4, 5);
                    path.lineTo(10, 5);
                    path.lineTo(12, 7);
                    g2.draw(path);
                    break;
                case ENROLLMENT:
                case CHECK:
                    // Check square
                    g2.drawRect(3, 3, 18, 18);
                    path.moveTo(8, 12);
                    path.lineTo(11, 15);
                    path.lineTo(16, 9);
                    g2.draw(path);
                    break;
                case SCORES:
                case CHART_BAR:
                    // Bar chart
                    g2.drawLine(3, 3, 3, 21);
                    g2.drawLine(3, 21, 21, 21);
                    g2.drawRect(6, 12, 4, 9);
                    g2.drawRect(12, 8, 4, 13);
                    g2.drawRect(18, 15, 4, 6);
                    break;
                case SCHEDULE:
                case CALENDAR:
                    // Calendar
                    g2.drawRect(3, 4, 18, 17);
                    g2.drawLine(3, 9, 21, 9);
                    g2.drawLine(8, 2, 8, 6);
                    g2.drawLine(16, 2, 16, 6);
                    break;
                case REPORTS:
                case CHART_PIE:
                    // Pie chart
                    g2.drawOval(3, 3, 18, 18);
                    path.moveTo(12, 12);
                    path.lineTo(12, 3);
                    path.moveTo(12, 12);
                    path.lineTo(20, 16);
                    g2.draw(path);
                    break;
                case PROFILE:
                    // User ID card
                    g2.drawRect(3, 5, 18, 14);
                    g2.drawOval(6, 9, 4, 4);
                    g2.drawLine(13, 9, 18, 9);
                    g2.drawLine(13, 13, 18, 13);
                    break;
                case PASSWORD:
                case LOCK:
                    // Lock icon
                    g2.drawRect(5, 10, 14, 10);
                    path.moveTo(8, 10);
                    path.curveTo(8, 5, 16, 5, 16, 10);
                    g2.draw(path);
                    break;
                case CLOCK:
                    g2.drawOval(3, 3, 18, 18);
                    g2.drawLine(12, 12, 12, 7);
                    g2.drawLine(12, 12, 16, 12);
                    break;
                case FILE_TEXT:
                    path.moveTo(4, 3);
                    path.lineTo(14, 3);
                    path.lineTo(20, 9);
                    path.lineTo(20, 21);
                    path.lineTo(4, 21);
                    path.closePath();
                    g2.draw(path);
                    g2.drawLine(14, 3, 14, 9);
                    g2.drawLine(14, 9, 20, 9);
                    g2.drawLine(7, 13, 17, 13);
                    g2.drawLine(7, 17, 17, 17);
                    break;
                default:
                    g2.drawRect(4, 4, 16, 16);
                    break;
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}

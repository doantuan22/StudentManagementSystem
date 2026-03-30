/**
 * Hệ thống typography chuẩn cho toàn bộ ứng dụng.
 */
package com.qlsv.view.common;

import java.awt.Font;

public final class AppFonts {

    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Headings
    public static final Font H1 = BASE_FONT.deriveFont(Font.BOLD, 24f);
    public static final Font H2 = BASE_FONT.deriveFont(Font.BOLD, 20f);
    public static final Font H3 = BASE_FONT.deriveFont(Font.BOLD, 17f);
    public static final Font H4 = BASE_FONT.deriveFont(Font.BOLD, 15f);
    
    // Body text
    public static final Font BODY = BASE_FONT.deriveFont(Font.PLAIN, 13f);
    public static final Font BODY_BOLD = BASE_FONT.deriveFont(Font.BOLD, 13f);
    public static final Font BODY_LARGE = BASE_FONT.deriveFont(Font.PLAIN, 14f);
    
    // Small text
    public static final Font CAPTION = BASE_FONT.deriveFont(Font.PLAIN, 12f);
    public static final Font CAPTION_BOLD = BASE_FONT.deriveFont(Font.BOLD, 12f);
    
    // Special
    public static final Font BUTTON = BASE_FONT.deriveFont(Font.BOLD, 13f);
    public static final Font INPUT = BASE_FONT.deriveFont(Font.PLAIN, 13f);

    private AppFonts() {
    }
}


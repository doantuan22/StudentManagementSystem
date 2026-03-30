/**
 * Hệ thống spacing chuẩn cho toàn bộ ứng dụng.
 * Base unit: 4px
 */
package com.qlsv.view.common;

public final class AppSpacing {

    public static final int UNIT = 4;
    
    // Spacing scale
    public static final int XS = UNIT;          // 4px
    public static final int SM = UNIT * 2;      // 8px
    public static final int MD = UNIT * 3;      // 12px
    public static final int LG = UNIT * 4;      // 16px
    public static final int XL = UNIT * 6;      // 24px
    public static final int XXL = UNIT * 8;     // 32px
    
    // Common padding
    public static final int PADDING_SMALL = MD;     // 12px
    public static final int PADDING_NORMAL = LG;    // 16px
    public static final int PADDING_LARGE = XL;     // 24px
    
    // Component spacing
    public static final int COMPONENT_GAP = SM;     // 8px
    public static final int SECTION_GAP = XL;       // 24px
    public static final int FIELD_GAP = LG;         // 16px
    
    // Border radius
    public static final int RADIUS_SMALL = SM;      // 8px
    public static final int RADIUS_NORMAL = MD;     // 12px
    public static final int RADIUS_LARGE = LG;      // 16px

    private AppSpacing() {
    }
}

/**
 * Utility để debounce search operations, tránh search quá nhiều lần khi user đang gõ.
 */
package com.qlsv.utils;

import javax.swing.Timer;
import java.awt.event.ActionListener;

public final class SearchDebouncer {

    private final Timer timer;
    private static final int DEFAULT_DELAY_MS = 300;

    /**
     * Tạo debouncer với delay mặc định 300ms.
     */
    public SearchDebouncer(Runnable action) {
        this(action, DEFAULT_DELAY_MS);
    }

    /**
     * Tạo debouncer với delay tùy chỉnh.
     */
    public SearchDebouncer(Runnable action, int delayMs) {
        ActionListener listener = event -> action.run();
        timer = new Timer(delayMs, listener);
        timer.setRepeats(false);
    }

    /**
     * Trigger debounced action. Nếu đang chờ thì reset timer.
     */
    public void trigger() {
        if (timer.isRunning()) {
            timer.restart();
        } else {
            timer.start();
        }
    }

    /**
     * Cancel pending action.
     */
    public void cancel() {
        timer.stop();
    }

    /**
     * Cleanup resources.
     */
    public void dispose() {
        timer.stop();
    }
}

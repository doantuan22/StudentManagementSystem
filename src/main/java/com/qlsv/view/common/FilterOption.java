package com.qlsv.view.common;

/**
 * Gia tri hien thi dung chung cho cac combobox loc/form,
 * giup tach nhan hien thi khoi gia tri nghiep vu ben trong.
 */
public record FilterOption<T>(String label, T value) {

    @Override
    public String toString() {
        return label;
    }
}

package com.qlsv.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.qlsv.exception.AppException;

import javax.swing.JTable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public final class PDFExportUtil {

    private PDFExportUtil() {
    }

    public static void exportTable(String title, JTable table, File file) {
        try {
            if (file.getParentFile() != null) {
                Files.createDirectories(file.getParentFile().toPath());
            }
        } catch (IOException exception) {
            throw new AppException("Không thể tạo thư mục chứa file PDF.", exception);
        }

        Document document = new Document();
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Đăng ký các font hệ thống để hỗ trợ tiếng Việt
            FontFactory.registerDirectories();
            Font titleFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16, Font.BOLD);
            Font headerFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12, Font.BOLD);
            Font contentFont = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12, Font.NORMAL);

            document.add(new Paragraph(title, titleFont));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(table.getColumnCount());
            pdfTable.setWidthPercentage(100);

            for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                PdfPCell headerCell = new PdfPCell(new Phrase(table.getColumnName(columnIndex), headerFont));
                pdfTable.addCell(headerCell);
            }

            for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
                for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                    Object value = table.getValueAt(rowIndex, columnIndex);
                    String text = value == null ? "" : String.valueOf(value);
                    pdfTable.addCell(new Phrase(text, contentFont));
                }
            }

            document.add(pdfTable);
            // document.close() must be called while outputStream is still open.
            document.close();
        } catch (IOException | DocumentException exception) {
            throw new AppException("Không thể xuất file PDF.", exception);
        }
    }
}

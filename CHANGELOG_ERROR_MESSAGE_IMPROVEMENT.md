# Cải Tiến Thông Báo Lỗi - Cập Nhật Học Phần

## 🎯 Vấn Đề

Khi admin cập nhật thời khóa biểu hoặc môn học của học phần đã có sinh viên đăng ký và có điểm, thông báo lỗi:
- ❌ Quá dài, khó đọc
- ❌ Chữ bị khuất trong dialog nhỏ
- ❌ Không rõ ràng về nguyên nhân và giải pháp

## ✅ Giải Pháp Đã Áp Dụng

### 1. Cải Thiện Logic Validation (CourseSectionService.java)

#### Trước:
```java
throw new ValidationException(
    "Khong the dong bo dang ky hoc phan trung mon trong cung hoc ky va nam hoc. " +
    "(Sinh viên " + laterEnrollment.getStudent().getFullName() + 
    " đã có điểm ở đăng ký bị trùng mới phát sinh)"
);
```

#### Sau:
```java
private String buildConflictErrorMessage(List<String> studentsWithScores) {
    StringBuilder message = new StringBuilder();
    message.append("KHÔNG THỂ CẬP NHẬT HỌC PHẦN\n\n");
    message.append("Lý do: Thay đổi thời khóa biểu hoặc môn học gây xung đột với ");
    message.append(studentsWithScores.size());
    message.append(" sinh viên đã có điểm.\n\n");
    message.append("Danh sách sinh viên bị ảnh hưởng:\n");
    
    int count = 1;
    for (String student : studentsWithScores) {
        message.append(count++).append(". ").append(student).append("\n");
        if (count > 10) {
            message.append("... và ").append(studentsWithScores.size() - 10)
                   .append(" sinh viên khác\n");
            break;
        }
    }
    
    message.append("\nGiải pháp:\n");
    message.append("• Xóa điểm của các sinh viên trên trước khi cập nhật\n");
    message.append("• Hoặc giữ nguyên thời khóa biểu/môn học hiện tại");
    
    return message.toString();
}
```

### 2. Cải Thiện UI Dialog (DialogUtil.java)

#### Trước:
```java
public static void showError(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
}
```
❌ Vấn đề: Message dài bị cắt, không có scroll

#### Sau:
```java
public static void showError(Component parent, String message) {
    // Nếu message dài hoặc có nhiều dòng, dùng JTextArea với scroll
    if (message.length() >= 150 || message.split("\n").length > 3) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 13));
        
        int lines = message.split("\n").length;
        int rows = Math.min(lines + 1, 15);
        textArea.setRows(rows);
        textArea.setColumns(50);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, Math.min(rows * 25 + 20, 400)));
        
        JOptionPane.showMessageDialog(parent, scrollPane, "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Message ngắn dùng dialog thông thường
    JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
}
```
✅ Cải thiện:
- Tự động phát hiện message dài
- Hiển thị trong JTextArea với scroll
- Kích thước dialog tự động điều chỉnh
- Font rõ ràng, dễ đọc

### 3. Thông Báo Lỗi Cụ Thể Hơn

#### Xung đột môn học:
```
KHÔNG THỂ CẬP NHẬT HỌC PHẦN

Loại xung đột: Trùng môn học
Một số sinh viên đã đăng ký môn học này ở học phần khác trong cùng học kỳ.

Vui lòng kiểm tra lại môn học, học kỳ hoặc năm học.
```

#### Xung đột lịch học:
```
KHÔNG THỂ CẬP NHẬT HỌC PHẦN

Loại xung đột: Trùng lịch học
Một số sinh viên có lịch học bị trùng với học phần khác đã đăng ký.

Vui lòng kiểm tra lại thời khóa biểu của học phần.
```

#### Xung đột với sinh viên đã có điểm:
```
KHÔNG THỂ CẬP NHẬT HỌC PHẦN

Lý do: Thay đổi thời khóa biểu hoặc môn học gây xung đột với 3 sinh viên đã có điểm.

Danh sách sinh viên bị ảnh hưởng:
1. SV001 - Nguyễn Văn A
2. SV002 - Trần Thị B
3. SV003 - Lê Văn C

Giải pháp:
• Xóa điểm của các sinh viên trên trước khi cập nhật
• Hoặc giữ nguyên thời khóa biểu/môn học hiện tại
```

## 📊 So Sánh Trước/Sau

### Trước:
```
[Lỗi]
Khong the dong bo dang ky hoc phan trung mon trong cung hoc ky va nam hoc. (Sinh viên Nguyễn Văn A đã có điểm ở đăng ký bị trùng mới phát sinh)
[OK]
```
❌ Chữ bị cắt, không rõ ràng

### Sau:
```
┌─────────────────────────────────────────────────┐
│                     Lỗi                         │
├─────────────────────────────────────────────────┤
│ KHÔNG THỂ CẬP NHẬT HỌC PHẦN                    │
│                                                 │
│ Lý do: Thay đổi thời khóa biểu hoặc môn học   │
│ gây xung đột với 3 sinh viên đã có điểm.      │
│                                                 │
│ Danh sách sinh viên bị ảnh hưởng:             │
│ 1. SV001 - Nguyễn Văn A                       │
│ 2. SV002 - Trần Thị B                         │
│ 3. SV003 - Lê Văn C                           │
│                                                 │
│ Giải pháp:                                     │
│ • Xóa điểm của các sinh viên trên trước khi   │
│   cập nhật                                     │
│ • Hoặc giữ nguyên thời khóa biểu/môn học      │
│   hiện tại                                     │
│                                                 │
│                    [OK]                         │
└─────────────────────────────────────────────────┘
```
✅ Rõ ràng, có scroll, dễ đọc

## 🎨 Tính Năng Mới

1. **Auto-detect message dài**: Tự động chuyển sang JTextArea nếu > 150 ký tự hoặc > 3 dòng
2. **Scrollable**: Có thanh scroll nếu nội dung quá dài
3. **Responsive size**: Kích thước dialog tự động điều chỉnh (tối đa 400px height)
4. **Better formatting**: Sử dụng bullet points, numbering, sections
5. **Actionable**: Đưa ra giải pháp cụ thể

## 🔧 Files Đã Thay Đổi

1. `src/main/java/com/qlsv/service/CourseSectionService.java`
   - Thêm method `buildConflictErrorMessage()`
   - Cải thiện `removeDuplicateEnrollments()`
   - Cải thiện message constants

2. `src/main/java/com/qlsv/utils/DialogUtil.java`
   - Nâng cấp `showError()` method
   - Thêm logic auto-detect message dài
   - Thêm JTextArea + JScrollPane cho message dài

## ✅ Kết Quả

- ✅ Thông báo lỗi rõ ràng, dễ đọc
- ✅ Không bị cắt chữ
- ✅ Có scroll cho nội dung dài
- ✅ Liệt kê cụ thể sinh viên bị ảnh hưởng
- ✅ Đưa ra giải pháp rõ ràng
- ✅ Tự động áp dụng cho tất cả error messages dài trong hệ thống

## 🚀 Cách Test

1. Tạo học phần có sinh viên đăng ký
2. Nhập điểm cho sinh viên
3. Thử đổi môn học hoặc thời khóa biểu của học phần
4. Xem thông báo lỗi mới - rõ ràng và dễ đọc!

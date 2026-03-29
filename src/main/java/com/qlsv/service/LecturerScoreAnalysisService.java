/**
 * Xử lý nghiệp vụ điểm giảng viên phân tích.
 */
package com.qlsv.service;

import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
import com.qlsv.model.Subject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class LecturerScoreAnalysisService {

    private static final double PASS_SCORE = 5.0;
    private static final int INCOMPLETE_PREVIEW_LIMIT = 5;

    private final GroqService groqService = new GroqService();

    /**
     * Chuẩn bị snapshot.
     */
    public LecturerScoreAnalysisSnapshot prepareSnapshot(List<Score> filteredScores, String filterLabel) {
        if (filteredScores == null || filteredScores.isEmpty()) {
            throw new ValidationException("Không có dữ liệu điểm trong danh sách hiện tại để phân tích.");
        }

        List<StudentScoreSnapshot> rows = new ArrayList<>();
        List<String> incompleteStudents = new ArrayList<>();
        LinkedHashSet<String> sectionCodes = new LinkedHashSet<>();
        LinkedHashSet<String> subjectNames = new LinkedHashSet<>();

        double totalSum = 0.0;
        double minScore = Double.POSITIVE_INFINITY;
        double maxScore = Double.NEGATIVE_INFINITY;
        long passCount = 0;
        long failCount = 0;

        for (Score score : filteredScores) {
            if (!hasCompletePersistedScore(score)) {
                incompleteStudents.add(describeScoreOwner(score));
                continue;
            }

            StudentScoreSnapshot row = toSnapshot(score);
            rows.add(row);

            if (!row.sectionCode().isBlank()) {
                sectionCodes.add(row.sectionCode());
            }
            if (!row.subjectName().isBlank()) {
                subjectNames.add(row.subjectName());
            }

            totalSum += row.totalScore();
            minScore = Math.min(minScore, row.totalScore());
            maxScore = Math.max(maxScore, row.totalScore());
            if ("PASS".equalsIgnoreCase(row.result())) {
                passCount++;
            } else {
                failCount++;
            }
        }

        if (!incompleteStudents.isEmpty()) {
            throw new ValidationException(buildIncompleteScoreMessage(filteredScores.size(), incompleteStudents));
        }

        return new LecturerScoreAnalysisSnapshot(
                normalizeText(filterLabel),
                List.copyOf(sectionCodes),
                List.copyOf(subjectNames),
                rows.size(),
                round(totalSum / rows.size()),
                passCount,
                failCount,
                round(minScore),
                round(maxScore),
                List.copyOf(rows)
        );
    }

    /**
     * Phân tích snapshot.
     */
    public String analyzeSnapshot(LecturerScoreAnalysisSnapshot snapshot) {
        GroqService.AnalysisResponse response = groqService.requestAnalysis(
                buildSystemPrompt(),
                buildUserPrompt(snapshot)
        );
        if (!response.success()) {
            throw new ValidationException(response.message());
        }
        return response.message();
    }

    /**
     * Kiểm tra complete persisted điểm.
     */
    private boolean hasCompletePersistedScore(Score score) {
        if (score == null || score.getId() == null) {
            return false;
        }

        Enrollment enrollment = score.getEnrollment();
        if (enrollment == null || enrollment.getStudent() == null || enrollment.getCourseSection() == null) {
            return false;
        }

        return score.getProcessScore() != null
                && score.getMidtermScore() != null
                && score.getFinalScore() != null;
    }

    /**
     * Xử lý to snapshot.
     */
    private StudentScoreSnapshot toSnapshot(Score score) {
        Enrollment enrollment = score.getEnrollment();
        Student student = enrollment.getStudent();
        CourseSection courseSection = enrollment.getCourseSection();
        Subject subject = courseSection.getSubject();

        double processScore = round(score.getProcessScore());
        double midtermScore = round(score.getMidtermScore());
        double finalScore = round(score.getFinalScore());
        double totalScore = score.getTotalScore() == null
                /**
                 * Tính tổng điểm.
                 */
                ? calculateTotalScore(processScore, midtermScore, finalScore)
                : round(score.getTotalScore());

        return new StudentScoreSnapshot(
                normalizeText(student.getStudentCode()),
                normalizeText(student.getFullName()),
                normalizeText(courseSection.getSectionCode()),
                subject == null ? "" : normalizeText(subject.getSubjectName()),
                processScore,
                midtermScore,
                finalScore,
                totalScore,
                normalizeResult(score.getResult(), totalScore)
        );
    }

    /**
     * Tạo system prompt.
     */
    private String buildSystemPrompt() {
        return "Bạn là chuyên gia phân tích dữ liệu học tập hỗ trợ giảng viên đại học.";
    }

    /**
     * Tạo người dùng prompt.
     */
    private String buildUserPrompt(LecturerScoreAnalysisSnapshot snapshot) {
        StringBuilder prompt = new StringBuilder(1024 + snapshot.rows().size() * 96);
        prompt.append("Hãy phân tích dữ liệu điểm của nhóm sinh viên theo góc nhìn hỗ trợ giảng viên.\n");
        prompt.append("Phạm vi bộ lọc hiện tại: ").append(defaultValue(snapshot.filterLabel(), "Danh sách đang hiển thị")).append('\n');
        prompt.append("Học phần xuất hiện: ").append(joinValues(snapshot.sectionCodes(), "Không xác định")).append('\n');
        prompt.append("Môn học xuất hiện: ").append(joinValues(snapshot.subjectNames(), "Không xác định")).append('\n');
        prompt.append("Số lượng sinh viên: ").append(snapshot.studentCount()).append('\n');
        prompt.append(String.format(
                Locale.US,
                "Thống kê nhanh: điểm trung bình %.2f, đạt %d, chưa đạt %d, thấp nhất %.2f, cao nhất %.2f.%n",
                snapshot.averageScore(),
                snapshot.passCount(),
                snapshot.failCount(),
                snapshot.minScore(),
                snapshot.maxScore()
        ));
        prompt.append("Danh sách điểm cô đọng:\n");

        int rowNumber = 1;
        for (StudentScoreSnapshot row : snapshot.rows()) {
            prompt.append(rowNumber++)
                    .append(". ")
                    .append(row.studentCode())
                    .append(" - ")
                    .append(row.fullName())
                    .append(" | HP ")
                    .append(defaultValue(row.sectionCode(), "N/A"))
                    .append(" | MH ")
                    .append(defaultValue(row.subjectName(), "N/A"))
                    .append(String.format(
                            Locale.US,
                            " | QT %.2f | GK %.2f | CK %.2f | TK %.2f | %s%n",
                            row.processScore(),
                            row.midtermScore(),
                            row.finalScore(),
                            row.totalScore(),
                            row.result()
                    ));
        }

        prompt.append("\nYêu cầu trả lời:\n");
        prompt.append("- Tóm tắt tình hình học tập chung của nhóm sinh viên.\n");
        prompt.append("- Phân tích xu hướng điểm số, mức độ tiếp thu và phân bố kết quả.\n");
        prompt.append("- Chỉ ra điểm mạnh, điểm yếu và nội dung hoặc kỹ năng có thể đang bị hổng.\n");
        prompt.append("- Đề xuất cách dạy, cách ôn tập, cách chia nhóm hỗ trợ và cách điều chỉnh tiến độ hoặc kiểm tra để nâng cao chất lượng.\n");
        prompt.append("- Viết bằng tiếng Việt, rõ ràng, dễ đọc, ưu tiên các tiêu đề ngắn và gạch đầu dòng.\n");
        prompt.append("- Phân tích theo vai trò giảng viên, không xưng hô như đang tư vấn cho một sinh viên cá nhân.\n");
        prompt.append("- Nếu có nhận định mang tính suy luận, hãy nói rõ đó là suy luận dựa trên xu hướng điểm.");
        return prompt.toString();
    }

    /**
     * Tạo incomplete điểm thông báo.
     */
    private String buildIncompleteScoreMessage(int totalCount, List<String> incompleteStudents) {
        int previewCount = Math.min(INCOMPLETE_PREVIEW_LIMIT, incompleteStudents.size());
        String preview = String.join("; ", incompleteStudents.subList(0, previewCount));
        String moreLabel = incompleteStudents.size() > previewCount ? "; ..." : "";
        return "Chỉ được phân tích khi toàn bộ sinh viên trong danh sách hiện tại đã có đầy đủ điểm. "
                + "Hiện còn "
                + incompleteStudents.size()
                + "/"
                + totalCount
                + " sinh viên chưa chấm hoặc thiếu điểm: "
                + preview
                + moreLabel;
    }

    /**
     * Xử lý describe điểm owner.
     */
    private String describeScoreOwner(Score score) {
        if (score == null || score.getEnrollment() == null || score.getEnrollment().getStudent() == null) {
            return "Sinh viên chưa xác định";
        }

        Student student = score.getEnrollment().getStudent();
        String studentCode = normalizeText(student.getStudentCode());
        String fullName = normalizeText(student.getFullName());
        if (studentCode.isBlank() && fullName.isBlank()) {
            return "Sinh viên chưa xác định";
        }
        if (studentCode.isBlank()) {
            return fullName;
        }
        if (fullName.isBlank()) {
            return studentCode;
        }
        return studentCode + " - " + fullName;
    }

    /**
     * Chuẩn hóa kết quả.
     */
    private String normalizeResult(String result, double totalScore) {
        if (result == null || result.isBlank()) {
            return totalScore >= PASS_SCORE ? "PASS" : "FAIL";
        }
        return result.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Tính tổng điểm.
     */
    private double calculateTotalScore(double processScore, double midtermScore, double finalScore) {
        return round(processScore * 0.3 + midtermScore * 0.2 + finalScore * 0.5);
    }

    /**
     * Xử lý round.
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Xử lý value mặc định.
     */
    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Xử lý join values.
     */
    private String joinValues(List<String> values, String fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }
        return String.join(", ", values);
    }

    /**
     * Chuẩn hóa văn bản.
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Xử lý điểm giảng viên phân tích snapshot.
     */
    public record LecturerScoreAnalysisSnapshot(
            String filterLabel,
            List<String> sectionCodes,
            List<String> subjectNames,
            int studentCount,
            double averageScore,
            long passCount,
            long failCount,
            double minScore,
            double maxScore,
            List<StudentScoreSnapshot> rows
    ) {

        /**
         * Xử lý hộp thoại title.
         */
        public String dialogTitle() {
            if (sectionCodes != null && sectionCodes.size() == 1) {
                return "Phân tích điểm lớp học phần " + sectionCodes.get(0);
            }
            return "Phân tích điểm lớp học phần";
        }
    }

    /**
     * Xử lý điểm sinh viên snapshot.
     */
    public record StudentScoreSnapshot(
            String studentCode,
            String fullName,
            String sectionCode,
            String subjectName,
            double processScore,
            double midtermScore,
            double finalScore,
            double totalScore,
            String result
    ) {
    }
}

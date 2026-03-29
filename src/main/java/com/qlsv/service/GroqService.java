package com.qlsv.service;

import com.qlsv.config.AppConfig;
import com.qlsv.model.Score;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GroqService {

    private static final String API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL_NAME = "llama-3.3-70b-versatile";
    private static final String STUDENT_SYSTEM_PROMPT = "Bạn là chuyên gia phân tích kết quả học tập.";

    private final String apiKey;
    private final HttpClient httpClient;

    public GroqService() {
        this.apiKey = AppConfig.getProperty("groq.api.key");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String analyzeScores(List<Score> scores) {
        AnalysisResponse response = requestAnalysis(STUDENT_SYSTEM_PROMPT, buildStudentUserPrompt(scores));
        return response.message();
    }

    public AnalysisResponse requestAnalysis(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return AnalysisResponse.failure("Lỗi: Chưa cấu hình Groq API Key trong application.properties (groq.api.key=...)");
        }

        try {
            String requestBody = buildRequestBody(systemPrompt, userPrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return AnalysisResponse.success(extractContentFromResponse(response.body()));
            }
            return AnalysisResponse.failure("Lỗi API (" + response.statusCode() + "): " + response.body());
        } catch (Exception exception) {
            return AnalysisResponse.failure("Lỗi khi gọi AI: " + exception.getMessage());
        }
    }

    private String buildStudentUserPrompt(List<Score> scores) {
        return "Bạn là một cố vấn học tập chuyên nghiệp. Hãy phân tích bảng điểm sau đây của sinh viên và đưa ra nhận xét, lời khuyên học tập (viết bằng tiếng Việt, súc tích, dễ đọc): "
                + formatScoresToJson(scores);
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) {
        return "{"
                + "\"model\": \"" + MODEL_NAME + "\","
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},"
                + "  {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}"
                + "]"
                + "}";
    }

    private String formatScoresToJson(List<Score> scores) {
        return scores.stream()
                .map(score -> {
                    String subjectName = "N/A";
                    if (score != null
                            && score.getEnrollment() != null
                            && score.getEnrollment().getCourseSection() != null
                            && score.getEnrollment().getCourseSection().getSubject() != null) {
                        subjectName = score.getEnrollment().getCourseSection().getSubject().getSubjectName();
                    }

                    return String.format(
                            Locale.US,
                            "{\"subject\":\"%s\",\"total\":%s,\"result\":\"%s\"}",
                            escapeJson(subjectName),
                            formatNumber(score == null ? null : score.getTotalScore()),
                            escapeJson(score == null ? null : score.getResult())
                    );
                })
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String formatNumber(Double value) {
        return value == null ? "null" : String.format(Locale.US, "%.1f", value);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char currentChar = value.charAt(index);
            switch (currentChar) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(currentChar);
            }
        }
        return builder.toString();
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            String marker = "\"content\":";
            int contentStart = responseBody.indexOf(marker);
            if (contentStart == -1) {
                return "Không tìm thấy nội dung trong phản hồi.";
            }

            contentStart += marker.length();
            while (contentStart < responseBody.length() && responseBody.charAt(contentStart) != '"') {
                contentStart++;
            }
            contentStart++;

            StringBuilder result = new StringBuilder();
            boolean escaped = false;
            for (int index = contentStart; index < responseBody.length(); index++) {
                char currentChar = responseBody.charAt(index);
                if (escaped) {
                    if (currentChar == 'n') {
                        result.append('\n');
                    } else if (currentChar == 't') {
                        result.append('\t');
                    } else if (currentChar == '"') {
                        result.append('"');
                    } else if (currentChar == '\\') {
                        result.append('\\');
                    } else {
                        result.append('\\').append(currentChar);
                    }
                    escaped = false;
                } else if (currentChar == '\\') {
                    escaped = true;
                } else if (currentChar == '"') {
                    break;
                } else {
                    result.append(currentChar);
                }
            }
            return result.toString();
        } catch (Exception exception) {
            return "Không thể xử lý phản hồi từ AI: " + exception.getMessage();
        }
    }

    public record AnalysisResponse(boolean success, String message) {

        public static AnalysisResponse success(String message) {
            return new AnalysisResponse(true, message);
        }

        public static AnalysisResponse failure(String message) {
            return new AnalysisResponse(false, message);
        }
    }
}

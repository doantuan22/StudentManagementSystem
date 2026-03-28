package com.qlsv.service;

import com.qlsv.config.AppConfig;
import com.qlsv.model.Score;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class GroqService {

    private final String apiKey;
    private final HttpClient httpClient;

    public GroqService() {
        this.apiKey = AppConfig.getProperty("groq.api.key");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String analyzeScores(List<Score> scores) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Lỗi: Chưa cấu hình Groq API Key trong application.properties (groq.api.key=...)";
        }

        try {
            String scoresJson = formatScoresToJson(scores);
            String prompt = "Bạn là một cố vấn học tập chuyên nghiệp. Hãy phân tích bảng điểm sau đây của sinh viên và đưa ra nhận xét, lời khuyên học tập (viết bằng tiếng Việt, súc tích, dễ đọc): " + scoresJson;

            String requestBody = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"Bạn là chuyên gia phân tích kết quả học tập.\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}"
                    + "]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractContentFromResponse(response.body());
            } else {
                return "Lỗi API (" + response.statusCode() + "): " + response.body();
            }
        } catch (Exception e) {
            return "Lỗi khi gọi AI: " + e.getMessage();
        }
    }

    private String formatScoresToJson(List<Score> scores) {
        return scores.stream().map(s -> {
            String subjectName = (s.getEnrollment() != null && s.getEnrollment().getCourseSection() != null && s.getEnrollment().getCourseSection().getSubject() != null)
                    ? s.getEnrollment().getCourseSection().getSubject().getSubjectName() : "N/A";
            return String.format("{\"subject\":\"%s\",\"total\":%.1f,\"result\":\"%s\"}", 
                    subjectName, s.getTotalScore(), s.getResult());
        }).collect(Collectors.joining(",", "[", "]"));
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            // Thủ công tách content từ JSON response của OpenAI/Groq format
            // Response format: {"choices":[{"message":{"content":"..."}}]}
            String marker = "\"content\":";
            int contentStart = responseBody.indexOf(marker);
            if (contentStart == -1) return "Không tìm thấy nội dung trong phản hồi.";
            
            contentStart += marker.length();
            // Tìm dấu ngoặc kép bắt đầu chuỗi content
            while (contentStart < responseBody.length() && responseBody.charAt(contentStart) != '\"') {
                contentStart++;
            }
            contentStart++; // Bỏ qua dấu "
            
            // Tìm dấu ngoặc kép kết thúc (cẩn thận với escape \")
            StringBuilder result = new StringBuilder();
            boolean escaped = false;
            for (int i = contentStart; i < responseBody.length(); i++) {
                char c = responseBody.charAt(i);
                if (escaped) {
                    if (c == 'n') result.append('\n');
                    else if (c == 't') result.append('\t');
                    else if (c == '\"') result.append('\"');
                    else if (c == '\\') result.append('\\');
                    else result.append('\\').append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '\"') {
                    break;
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "Không thể xử lý phản hồi từ AI: " + e.getMessage();
        }
    }
}

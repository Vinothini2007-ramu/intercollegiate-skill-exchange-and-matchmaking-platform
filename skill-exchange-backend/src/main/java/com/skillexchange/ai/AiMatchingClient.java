package com.skillexchange.ai;

import com.skillexchange.util.JsonUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Calls the local Python Sentence-BERT service from the Java backend. */
public class AiMatchingClient {
    private final String baseUrl;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public AiMatchingClient() {
        this(System.getProperty("ai.url", "http://127.0.0.1:5000"));
    }

    public AiMatchingClient(String baseUrl) { this.baseUrl = baseUrl; }

    public AiResult score(String student1, String student2) throws IOException, InterruptedException {
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("student1", student1 == null ? "" : student1);
        body.put("student2", student2 == null ? "" : student2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/ai/match"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(body), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IOException("AI service returned HTTP " + response.statusCode());

        double percent = extractNumber(response.body(), "match_percentage");
        String recommendation = extractString(response.body(), "recommendation");
        return new AiResult(percent, recommendation);
    }

    private double extractNumber(String json, String key) throws IOException {
        Matcher m = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*([-+]?\\d+(?:\\.\\d+)?)").matcher(json);
        if (!m.find()) throw new IOException("AI response missing " + key);
        return Double.parseDouble(m.group(1));
    }

    private String extractString(String json, String key) throws IOException {
        Matcher m = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(json);
        if (!m.find()) throw new IOException("AI response missing " + key);
        return m.group(1);
    }

    public static final class AiResult {
        private final double percentage;
        private final String recommendation;
        public AiResult(double percentage, String recommendation) {
            this.percentage = percentage; this.recommendation = recommendation;
        }
        public double getPercentage() { return percentage; }
        public String getRecommendation() { return recommendation; }
    }
}

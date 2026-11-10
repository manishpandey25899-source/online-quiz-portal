package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.Category;
import com.aiquiz.quizportal.entity.QuizAttempt;
import com.aiquiz.quizportal.repository.CategoryRepository;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> analyzeUserPerformance(Integer userId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);

        Map<String, Object> response = new LinkedHashMap<>();

        if (attempts == null || attempts.isEmpty()) {
            response.put("totalQuizzes", 0);
            response.put("totalQuestionsAnswered", 0);
            response.put("correctAnswers", 0);
            response.put("averagePercentage", 0.0);
            response.put("placementReadinessScore", 0);
            response.put("summary", "You haven't attempted any quizzes yet. Take your first quiz or daily challenge to generate AI performance insights!");
            response.put("strengths", Collections.emptyList());
            response.put("weaknesses", Collections.emptyList());
            response.put("actionableRecommendations", Arrays.asList(
                    "Start with a subject you feel confident in to establish a baseline.",
                    "Aim for at least 80% accuracy before taking the Full Placement Mock.",
                    "Review explanations in the Answer Key after each quiz submission."
            ));
            response.put("categoryBreakdown", Collections.emptyList());
            response.put("aiPowered", false);
            return response;
        }

        // Aggregate overall statistics
        long totalScore = attempts.stream().mapToLong(a -> a.getScore() != null ? a.getScore() : 0).sum();
        long totalQuestions = attempts.stream().mapToLong(a -> a.getTotalQuestions() != null ? a.getTotalQuestions() : 0).sum();
        double overallPct = totalQuestions > 0 ? ((double) totalScore * 100.0) / totalQuestions : 0.0;
        overallPct = Math.round(overallPct * 10.0) / 10.0;

        // Load categories map
        Map<Integer, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName, (c1, c2) -> c1));

        // Group attempts by category
        Map<Integer, List<QuizAttempt>> byCategory = attempts.stream()
                .filter(a -> a.getCategoryId() != null)
                .collect(Collectors.groupingBy(QuizAttempt::getCategoryId));

        List<Map<String, Object>> categoryBreakdown = new ArrayList<>();
        List<String> strongCategories = new ArrayList<>();
        List<String> weakCategories = new ArrayList<>();

        for (Map.Entry<Integer, List<QuizAttempt>> entry : byCategory.entrySet()) {
            Integer catId = entry.getKey();
            String catName = categoryNames.getOrDefault(catId, "Category " + catId);
            List<QuizAttempt> catAttempts = entry.getValue();

            long catScore = catAttempts.stream().mapToLong(a -> a.getScore() != null ? a.getScore() : 0).sum();
            long catTotal = catAttempts.stream().mapToLong(a -> a.getTotalQuestions() != null ? a.getTotalQuestions() : 0).sum();
            double catPct = catTotal > 0 ? ((double) catScore * 100.0) / catTotal : 0.0;
            catPct = Math.round(catPct * 10.0) / 10.0;

            String status;
            if (catPct >= 75.0) {
                status = "Strong";
                strongCategories.add(catName + " (" + catPct + "%)");
            } else if (catPct >= 50.0) {
                status = "Moderate";
            } else {
                status = "Needs Improvement";
                weakCategories.add(catName + " (" + catPct + "%)");
            }

            Map<String, Object> catStat = new LinkedHashMap<>();
            catStat.put("categoryId", catId);
            catStat.put("categoryName", catName);
            catStat.put("attempts", catAttempts.size());
            catStat.put("averagePercentage", catPct);
            catStat.put("status", status);
            categoryBreakdown.add(catStat);
        }

        // Calculate Placement Readiness Score (0-100)
        double coverageFactor = Math.min(byCategory.size() / 3.0, 1.0) * 30.0;
        double volumeFactor = Math.min(attempts.size() / 5.0, 1.0) * 20.0;
        double scoreFactor = (overallPct / 100.0) * 50.0;
        int placementReadiness = (int) Math.round(coverageFactor + volumeFactor + scoreFactor);
        placementReadiness = Math.max(0, Math.min(100, placementReadiness));

        response.put("totalQuizzes", attempts.size());
        response.put("totalQuestionsAnswered", totalQuestions);
        response.put("correctAnswers", totalScore);
        response.put("averagePercentage", overallPct);
        response.put("placementReadinessScore", placementReadiness);
        response.put("categoryBreakdown", categoryBreakdown);

        // Attempt Gemini AI Analysis if API key is present
        boolean aiSuccess = false;
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.startsWith("${")) {
            try {
                Map<String, Object> aiResult = callGeminiForAnalysis(attempts.size(), overallPct, categoryBreakdown, strongCategories, weakCategories);
                if (aiResult != null) {
                    response.put("summary", aiResult.get("summary"));
                    response.put("strengths", aiResult.get("strengths"));
                    response.put("weaknesses", aiResult.get("weaknesses"));
                    response.put("actionableRecommendations", aiResult.get("actionableRecommendations"));
                    response.put("aiPowered", true);
                    aiSuccess = true;
                }
            } catch (Exception ignored) {
                // Silently fallback to rule-based engine
            }
        }

        if (!aiSuccess) {
            // High-quality deterministic rule-based analysis
            populateRuleBasedInsights(response, attempts.size(), overallPct, strongCategories, weakCategories, categoryBreakdown);
            response.put("aiPowered", false);
        }

        return response;
    }

    private void populateRuleBasedInsights(Map<String, Object> response, int quizCount, double overallPct,
                                          List<String> strongCategories, List<String> weakCategories,
                                          List<Map<String, Object>> categoryBreakdown) {
        StringBuilder summary = new StringBuilder();
        if (overallPct >= 80.0) {
            summary.append("Outstanding performance! You are demonstrating solid technical accuracy across ").append(quizCount).append(" quizzes. ");
            summary.append("You are in a prime position for campus technical assessments.");
        } else if (overallPct >= 60.0) {
            summary.append("Good consistent progress across ").append(quizCount).append(" quizzes with an average score of ").append(overallPct).append("%. ");
            summary.append("Targeting your lower-scoring topics will quickly boost your campus placement readiness.");
        } else {
            summary.append("You have completed ").append(quizCount).append(" quizzes with an overall accuracy of ").append(overallPct).append("%. ");
            summary.append("Focused practice on foundational concepts will help you build confidence and raise your scores.");
        }

        List<String> recommendations = new ArrayList<>();
        if (!weakCategories.isEmpty()) {
            recommendations.add("Prioritize revising topics in " + weakCategories.get(0) + " using Practice Mode.");
        } else {
            recommendations.add("Challenge yourself with timed Placement Mocks to build test-taking endurance.");
        }

        if (quizCount < 5) {
            recommendations.add("Take at least 5 quizzes to unlock advanced performance trend metrics.");
        } else {
            recommendations.add("Review your bookmarked questions regularly to reinforce tricky concepts.");
        }

        recommendations.add("Complete the Daily Challenge every morning to maintain your active learning streak.");

        response.put("summary", summary.toString());
        response.put("strengths", strongCategories.isEmpty() ? Collections.singletonList("Active learner taking initial assessment steps") : strongCategories);
        response.put("weaknesses", weakCategories.isEmpty() ? Collections.singletonList("None detected! Keep practicing across more categories") : weakCategories);
        response.put("actionableRecommendations", recommendations);
    }

    private Map<String, Object> callGeminiForAnalysis(int quizCount, double overallPct,
                                                     List<Map<String, Object>> breakdown,
                                                     List<String> strongCategories,
                                                     List<String> weakCategories) {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(4000);
            factory.setReadTimeout(4000);
            RestTemplate restTemplate = new RestTemplate(factory);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey.trim();

            String prompt = "You are an expert college placement mentor for BCA / Computer Science students. " +
                    "Analyze the student's quiz statistics:\n" +
                    "- Total Quizzes: " + quizCount + "\n" +
                    "- Overall Accuracy: " + overallPct + "%\n" +
                    "- Category Breakdown: " + objectMapper.writeValueAsString(breakdown) + "\n\n" +
                    "Return ONLY a valid JSON object (no markdown, no backticks, no extra text) with exactly these 4 keys:\n" +
                    "{\n" +
                    "  \"summary\": \"2-sentence encouraging academic assessment\",\n" +
                    "  \"strengths\": [\"point 1\", \"point 2\"],\n" +
                    "  \"weaknesses\": [\"point 1\", \"point 2\"],\n" +
                    "  \"actionableRecommendations\": [\"tip 1\", \"tip 2\", \"tip 3\"]\n" +
                    "}";

            Map<String, Object> part = Collections.singletonMap("text", prompt);
            Map<String, Object> content = Collections.singletonMap("parts", Collections.singletonList(part));
            Map<String, Object> requestBody = Collections.singletonMap("contents", Collections.singletonList(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidateText = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (!candidateText.isMissingNode()) {
                    String rawJson = candidateText.asText().trim();
                    // Strip markdown fences if Gemini wrapped it in ```json ... ```
                    if (rawJson.startsWith("```")) {
                        rawJson = rawJson.replaceAll("^```[a-zA-Z]*\\s*", "").replaceAll("```$", "").trim();
                    }
                    JsonNode parsed = objectMapper.readTree(rawJson);
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("summary", parsed.path("summary").asText());

                    List<String> strengths = new ArrayList<>();
                    parsed.path("strengths").forEach(n -> strengths.add(n.asText()));
                    result.put("strengths", strengths.isEmpty() ? strongCategories : strengths);

                    List<String> weaknesses = new ArrayList<>();
                    parsed.path("weaknesses").forEach(n -> weaknesses.add(n.asText()));
                    result.put("weaknesses", weaknesses.isEmpty() ? weakCategories : weaknesses);

                    List<String> recommendations = new ArrayList<>();
                    parsed.path("actionableRecommendations").forEach(n -> recommendations.add(n.asText()));
                    result.put("actionableRecommendations", recommendations);

                    return result;
                }
            }
        } catch (Exception e) {
            // Fallback handled by caller
        }
        return null;
    }
}

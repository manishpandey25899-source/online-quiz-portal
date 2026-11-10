package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.Category;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.repository.CategoryRepository;
import com.aiquiz.quizportal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlacementMockService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public static class MockInfo {
        private String type;
        private String title;
        private String description;
        private int durationMinutes;
        private int totalQuestions;
        private String badge;

        public MockInfo(String type, String title, String description, int durationMinutes, int totalQuestions, String badge) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.durationMinutes = durationMinutes;
            this.totalQuestions = totalQuestions;
            this.badge = badge;
        }

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getDurationMinutes() { return durationMinutes; }
        public int getTotalQuestions() { return totalQuestions; }
        public String getBadge() { return badge; }
    }

    public List<MockInfo> getAvailableMocks() {
        return Arrays.asList(
                new MockInfo("full", "Full Campus Placement Mock",
                        "Comprehensive recruitment test covering Technical, Aptitude, and Core CS domains.",
                        30, 25, "Most Popular"),
                new MockInfo("technical", "Technical Assessment Mock",
                        "Core programming, Data Structures, OOP concepts, and Web Technologies.",
                        25, 20, "High Focus"),
                new MockInfo("aptitude", "Aptitude & Reasoning Mock",
                        "Quantitative ability, analytical reasoning, and logical problem solving.",
                        20, 15, "Foundational")
        );
    }

    public Map<String, Object> generateMockTest(String mockType) {
        String type = (mockType != null) ? mockType.toLowerCase().trim() : "full";

        MockInfo info = getAvailableMocks().stream()
                .filter(m -> m.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(getAvailableMocks().get(0));

        List<Category> allCategories = categoryRepository.findAll();
        List<Question> allQuestions = questionRepository.findAll();

        if (allQuestions.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("mockType", info.getType());
            empty.put("title", info.getTitle());
            empty.put("durationMinutes", info.getDurationMinutes());
            empty.put("totalQuestions", 0);
            empty.put("questions", Collections.emptyList());
            return empty;
        }

        List<Question> filteredQuestions = filterQuestionsForMock(type, allCategories, allQuestions);

        // Shuffle and limit to desired question count
        Collections.shuffle(filteredQuestions);
        int targetCount = Math.min(info.getTotalQuestions(), filteredQuestions.size());
        List<Question> selected = new ArrayList<>(filteredQuestions.subList(0, targetCount));

        // Sanitize questions for student test-taking (remove correctOption and explanation)
        List<Map<String, Object>> sanitized = selected.stream().map(q -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", q.getId());
            map.put("questionText", q.getQuestionText());
            map.put("optionA", q.getOptionA());
            map.put("optionB", q.getOptionB());
            map.put("optionC", q.getOptionC());
            map.put("optionD", q.getOptionD());
            map.put("categoryId", q.getCategory() != null ? q.getCategory().getId() : null);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mockType", info.getType());
        response.put("title", info.getTitle());
        response.put("description", info.getDescription());
        response.put("durationMinutes", info.getDurationMinutes());
        response.put("totalQuestions", sanitized.size());
        response.put("questions", sanitized);

        return response;
    }

    private List<Question> filterQuestionsForMock(String mockType, List<Category> categories, List<Question> allQuestions) {
        Map<Integer, String> categoryNames = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c.getCategoryName().toLowerCase(), (c1, c2) -> c1));

        if ("aptitude".equals(mockType)) {
            List<Question> aptitudeQuestions = allQuestions.stream()
                    .filter(q -> {
                        Integer catId = q.getCategory() != null ? q.getCategory().getId() : null;
                        String name = categoryNames.getOrDefault(catId, "");
                        return name.contains("aptitude") || name.contains("reason") || name.contains("math") || name.contains("logic");
                    })
                    .collect(Collectors.toList());
            return aptitudeQuestions.isEmpty() ? allQuestions : aptitudeQuestions;
        } else if ("technical".equals(mockType)) {
            List<Question> techQuestions = allQuestions.stream()
                    .filter(q -> {
                        Integer catId = q.getCategory() != null ? q.getCategory().getId() : null;
                        String name = categoryNames.getOrDefault(catId, "");
                        return !name.contains("aptitude") && !name.contains("reason") && !name.contains("logic");
                    })
                    .collect(Collectors.toList());
            return techQuestions.isEmpty() ? allQuestions : techQuestions;
        } else {
            // Full Placement Mock: balanced mix
            return allQuestions;
        }
    }
}

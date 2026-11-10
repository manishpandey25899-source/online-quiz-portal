package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.UserPrincipal;
import com.aiquiz.quizportal.service.AiAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AiAnalysisController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceAnalysis(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(aiAnalysisService.analyzeUserPerformance(principal.getId()));
    }
}

package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.service.PlacementMockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/placement")
public class PlacementMockController {

    @Autowired
    private PlacementMockService placementMockService;

    @GetMapping("/mocks")
    public ResponseEntity<List<PlacementMockService.MockInfo>> getAvailableMocks() {
        return ResponseEntity.ok(placementMockService.getAvailableMocks());
    }

    @GetMapping("/mock/{mockType}")
    public ResponseEntity<Map<String, Object>> getMockTest(@PathVariable String mockType) {
        return ResponseEntity.ok(placementMockService.generateMockTest(mockType));
    }
}

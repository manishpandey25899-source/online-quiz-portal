package com.aiquiz.quizportal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "AI Quiz Portal Backend Running Successfully";
    }

    @GetMapping("/test")
    public String test() {
        return "Project chal raha hai!";
    }
}
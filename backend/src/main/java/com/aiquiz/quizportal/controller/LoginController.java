package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public User loginUser(@RequestBody User user) {

        User dbUser = userRepository.findByEmail(user.getEmail());

        if (dbUser != null &&
                dbUser.getPassword().equals(user.getPassword())) {

            return dbUser;
        }

        return null;
    }
}
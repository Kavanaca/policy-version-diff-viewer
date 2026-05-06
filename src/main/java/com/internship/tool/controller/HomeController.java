package com.internship.tool.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<h1>Welcome to Tool91 Backend!</h1><p>The Spring Boot application is running successfully with H2 Database.</p>";
    }
}

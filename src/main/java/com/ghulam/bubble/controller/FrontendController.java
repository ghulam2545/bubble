package com.ghulam.bubble.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/schemas")
    public String schemas() {
        return "schemas";
    }

    @GetMapping("/storage")
    public String storage() {
        return "storage";
    }

    @GetMapping("/system")
    public String system() {
        return "system";
    }

    @GetMapping("/statistics")
    public String statistics() {
        return "statistics";
    }

    @GetMapping("/queries")
    public String queries() {
        return "queries";
    }

    @GetMapping("/activity")
    public String activity() {
        return "activity";
    }
}


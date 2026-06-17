package com.goalkeeperdash.backoffice.web;

import com.goalkeeperdash.backoffice.service.AnalyticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Back-office login page + dashboard. */
@Controller
public class AdminHomeController {

    private final AnalyticsService analyticsService;

    public AdminHomeController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("stats", analyticsService.snapshot());
        model.addAttribute("active", "dashboard");
        return "admin/dashboard";
    }
}

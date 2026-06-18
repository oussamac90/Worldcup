package com.goalkeeperdash.backoffice.web;

import com.goalkeeperdash.leaderboard.season.SeasonAdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Season control: create (SCHEDULED), activate, close (§7.2). */
@Controller
public class AdminSeasonController {

    private final SeasonAdminService seasonAdmin;

    public AdminSeasonController(SeasonAdminService seasonAdmin) {
        this.seasonAdmin = seasonAdmin;
    }

    @GetMapping("/admin/seasons")
    public String list(Model model) {
        model.addAttribute("seasons", seasonAdmin.listAll());
        model.addAttribute("active", "seasons");
        return "admin/seasons";
    }

    @PostMapping("/admin/seasons")
    public String create(@RequestParam String name,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAt,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endsAt) {
        seasonAdmin.create(name, toInstant(startsAt), toInstant(endsAt));
        return "redirect:/admin/seasons";
    }

    @PostMapping("/admin/seasons/{id}/activate")
    public String activate(@PathVariable UUID id) {
        seasonAdmin.activate(id);
        return "redirect:/admin/seasons";
    }

    @PostMapping("/admin/seasons/{id}/close")
    public String close(@PathVariable UUID id) {
        seasonAdmin.close(id);
        return "redirect:/admin/seasons";
    }

    private Instant toInstant(LocalDateTime ldt) {
        return ldt.toInstant(ZoneOffset.UTC);
    }
}

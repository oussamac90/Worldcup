package com.goalkeeperdash.backoffice.web;

import com.goalkeeperdash.game.api.ScoreModerationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/** Submission moderation: list flagged, inspect eventSummary, invalidate/restore (§7.2). */
@Controller
public class AdminSubmissionController {

    private final ScoreModerationService moderation;

    public AdminSubmissionController(ScoreModerationService moderation) {
        this.moderation = moderation;
    }

    @GetMapping("/admin/submissions")
    public String flagged(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("submissions", moderation.listFlagged(PageRequest.of(page, 50)));
        model.addAttribute("page", page);
        model.addAttribute("active", "submissions");
        return "admin/submissions";
    }

    @GetMapping("/admin/submissions/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        var sub = moderation.get(id);
        model.addAttribute("submission", sub);
        model.addAttribute("eventSummary", sub.eventSummary() == null ? "{}" : sub.eventSummary().toPrettyString());
        model.addAttribute("active", "submissions");
        return "admin/submission-detail";
    }

    @PostMapping("/admin/submissions/{id}/invalidate")
    public String invalidate(@PathVariable UUID id) {
        moderation.invalidate(id);
        return "redirect:/admin/submissions/" + id;
    }

    @PostMapping("/admin/submissions/{id}/restore")
    public String restore(@PathVariable UUID id) {
        moderation.restore(id);
        return "redirect:/admin/submissions/" + id;
    }
}

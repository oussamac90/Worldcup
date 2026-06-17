package com.goalkeeperdash.backoffice.web;

import com.goalkeeperdash.game.api.ScoreModerationService;
import com.goalkeeperdash.user.api.UserAdminService;
import com.goalkeeperdash.user.domain.UserStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/** User moderation pages (§7.2): search, view profile/identities/submissions, set status, reset name. */
@Controller
public class AdminUserController {

    private final UserAdminService userAdmin;
    private final ScoreModerationService moderation;

    public AdminUserController(UserAdminService userAdmin, ScoreModerationService moderation) {
        this.userAdmin = userAdmin;
        this.moderation = moderation;
    }

    @GetMapping("/admin/users")
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("users", userAdmin.search(q));
        model.addAttribute("q", q);
        model.addAttribute("active", "users");
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        model.addAttribute("user", userAdmin.getDetail(id));
        model.addAttribute("submissions", moderation.listForUser(id));
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("active", "users");
        return "admin/user-detail";
    }

    @PostMapping("/admin/users/{id}/status")
    public String setStatus(@PathVariable UUID id, @RequestParam UserStatus status) {
        userAdmin.setStatus(id, status);
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/admin/users/{id}/reset-name")
    public String resetName(@PathVariable UUID id) {
        userAdmin.resetDisplayName(id);
        return "redirect:/admin/users/" + id;
    }
}

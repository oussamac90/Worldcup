package com.goalkeeperdash.backoffice.web;

import com.goalkeeperdash.user.api.NationAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Nation configuration: toggle active, edit display name / colors (§7.2). */
@Controller
public class AdminNationController {

    private final NationAdminService nationAdmin;

    public AdminNationController(NationAdminService nationAdmin) {
        this.nationAdmin = nationAdmin;
    }

    @GetMapping("/admin/nations")
    public String list(Model model) {
        model.addAttribute("nations", nationAdmin.listAll());
        model.addAttribute("active", "nations");
        return "admin/nations";
    }

    @PostMapping("/admin/nations/{code}/toggle")
    public String toggle(@PathVariable String code, @RequestParam boolean active) {
        nationAdmin.setActive(code, active);
        return "redirect:/admin/nations";
    }

    @PostMapping("/admin/nations/{code}/update")
    public String update(@PathVariable String code,
                         @RequestParam String name,
                         @RequestParam String flagColors) {
        nationAdmin.update(code, name, flagColors);
        return "redirect:/admin/nations";
    }
}

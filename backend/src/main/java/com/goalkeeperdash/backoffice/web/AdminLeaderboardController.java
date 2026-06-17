package com.goalkeeperdash.backoffice.web;

import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.leaderboard.service.LeaderboardService;
import com.goalkeeperdash.leaderboard.service.RebuildService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

/** Leaderboard inspection + Redis rebuild trigger (§5.5/§7.2). */
@Controller
public class AdminLeaderboardController {

    private final LeaderboardService leaderboardService;
    private final RebuildService rebuildService;
    private final SeasonService seasonService;

    public AdminLeaderboardController(LeaderboardService leaderboardService, RebuildService rebuildService,
                                      SeasonService seasonService) {
        this.leaderboardService = leaderboardService;
        this.rebuildService = rebuildService;
        this.seasonService = seasonService;
    }

    @GetMapping("/admin/leaderboards")
    public String view(@RequestParam(required = false) UUID seasonId, Model model) {
        UUID effective = seasonId != null ? seasonId : seasonService.activeSeasonIdOrNull();
        if (effective != null) {
            model.addAttribute("nations", leaderboardService.nationsBoard(effective, 50, 0));
            model.addAttribute("seasonId", effective);
        }
        model.addAttribute("active", "leaderboards");
        return "admin/leaderboards";
    }

    @PostMapping("/admin/leaderboards/rebuild")
    public String rebuild(@RequestParam(required = false) UUID seasonId, RedirectAttributes ra) {
        int users = (seasonId != null) ? rebuildService.rebuildSeason(seasonId) : rebuildService.rebuildAll();
        ra.addFlashAttribute("message", "Rebuilt Redis leaderboards (" + users + " user entries)");
        return "redirect:/admin/leaderboards" + (seasonId != null ? "?seasonId=" + seasonId : "");
    }
}

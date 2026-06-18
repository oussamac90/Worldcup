package com.goalkeeperdash.user.web;

import com.goalkeeperdash.user.api.NationService;
import com.goalkeeperdash.user.api.NationView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public list of active nations (§4.6). */
@RestController
@RequestMapping("/api/v1/nations")
public class NationController {

    private final NationService nationService;

    public NationController(NationService nationService) {
        this.nationService = nationService;
    }

    @GetMapping
    public List<NationView> list() {
        return nationService.listActive();
    }
}

package com.goalkeeperdash.user.web;

import com.goalkeeperdash.common.security.AppUserPrincipal;
import com.goalkeeperdash.user.service.ProfileService;
import com.goalkeeperdash.user.web.dto.MeResponse;
import com.goalkeeperdash.user.web.dto.ProfileDtos;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Profile + nation selection endpoints (§4.6). All require a valid access JWT. */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final ProfileService profileService;

    public MeController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public MeResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return profileService.getMe(principal.userId());
    }

    @PatchMapping
    public MeResponse updateProfile(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @Valid @RequestBody ProfileDtos.UpdateProfileRequest body) {
        return profileService.updateDisplayName(principal.userId(), body.displayName());
    }

    @PutMapping("/nation")
    public MeResponse setNation(@AuthenticationPrincipal AppUserPrincipal principal,
                                @Valid @RequestBody ProfileDtos.SetNationRequest body) {
        return profileService.setNation(principal.userId(), body.nationCode().toUpperCase());
    }
}

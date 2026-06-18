package com.goalkeeperdash.user.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProfileDtos {

    private ProfileDtos() {}

    public record UpdateProfileRequest(@NotBlank @Size(min = 3, max = 20) String displayName) {}

    public record SetNationRequest(@NotBlank @Size(min = 3, max = 3) String nationCode) {}
}

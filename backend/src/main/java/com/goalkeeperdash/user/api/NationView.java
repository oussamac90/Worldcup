package com.goalkeeperdash.user.api;

import java.util.UUID;

/** Public projection of a Nation, shared with other modules and the API. */
public record NationView(UUID id, String code, String name, String flagColors, boolean active) {}

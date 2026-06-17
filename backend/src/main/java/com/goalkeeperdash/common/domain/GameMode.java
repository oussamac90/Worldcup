package com.goalkeeperdash.common.domain;

/**
 * Game modes. Shared in {@code common} because both the {@code game} module
 * (sessions, submissions) and {@code leaderboard} reference it.
 */
public enum GameMode {
    TOURNAMENT,
    SURVIVAL,
    SUDDEN_DEATH,
    SHOOTOUT
}

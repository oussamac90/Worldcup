package com.goalkeeperdash.game.repo;

import com.goalkeeperdash.game.domain.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {
}

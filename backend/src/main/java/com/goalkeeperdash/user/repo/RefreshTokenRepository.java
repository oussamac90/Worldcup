package com.goalkeeperdash.user.repo;

import com.goalkeeperdash.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.revoked = true where r.userId = :userId")
    void revokeAllForUser(UUID userId);
}

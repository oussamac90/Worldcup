package com.goalkeeperdash.user.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Nation reads exposed to other modules (leaderboard needs nation lookups). The
 * {@code user} module owns the Nation entity; consumers go through this interface.
 */
public interface NationService {

    List<NationView> listActive();

    Optional<NationView> findByCode(String code);

    Optional<NationView> findById(UUID id);
}

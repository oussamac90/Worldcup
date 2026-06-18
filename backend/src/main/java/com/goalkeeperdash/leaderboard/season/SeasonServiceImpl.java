package com.goalkeeperdash.leaderboard.season;

import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.common.domain.SeasonStatus;
import com.goalkeeperdash.common.domain.SeasonView;
import com.goalkeeperdash.leaderboard.domain.Season;
import com.goalkeeperdash.leaderboard.repo.SeasonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/** Read side of season context, implementing the {@code common} contract. */
@Service
@Transactional(readOnly = true)
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasons;

    public SeasonServiceImpl(SeasonRepository seasons) {
        this.seasons = seasons;
    }

    @Override
    public Optional<SeasonView> findActiveSeason() {
        return seasons.findFirstByStatus(SeasonStatus.ACTIVE).map(SeasonServiceImpl::toView);
    }

    @Override
    public Optional<SeasonView> findById(UUID seasonId) {
        return seasons.findById(seasonId).map(SeasonServiceImpl::toView);
    }

    public static SeasonView toView(Season s) {
        return new SeasonView(s.getId(), s.getName(), s.getStartsAt(), s.getEndsAt(), s.getStatus());
    }
}

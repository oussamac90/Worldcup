package com.goalkeeperdash.bootstrap;

import com.goalkeeperdash.backoffice.security.AdminAccountService;
import com.goalkeeperdash.common.config.AppProperties;
import com.goalkeeperdash.common.domain.SeasonStatus;
import com.goalkeeperdash.leaderboard.domain.NationSeasonStat;
import com.goalkeeperdash.leaderboard.domain.Season;
import com.goalkeeperdash.leaderboard.domain.UserSeasonStat;
import com.goalkeeperdash.leaderboard.repo.NationSeasonStatRepository;
import com.goalkeeperdash.leaderboard.repo.SeasonRepository;
import com.goalkeeperdash.leaderboard.repo.UserSeasonStatRepository;
import com.goalkeeperdash.leaderboard.season.SeasonAdminService;
import com.goalkeeperdash.leaderboard.service.RebuildService;
import com.goalkeeperdash.user.domain.AuthProvider;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.domain.User;
import com.goalkeeperdash.user.domain.UserIdentity;
import com.goalkeeperdash.user.repo.NationRepository;
import com.goalkeeperdash.user.repo.UserIdentityRepository;
import com.goalkeeperdash.user.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Idempotent startup seeder (§10). Each step is insert-if-empty so reboots never
 * duplicate. Gated by {@code app.seed.enabled}; disable in prod
 * ({@code APP_SEED_ENABLED=false}). Synthetic users are clearly marked
 * ({@code synthetic=true}, provider SYNTHETIC) so they can be excluded/purged.
 */
@Component
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);
    private static final int SYNTHETIC_USERS = 400;

    private final AppProperties props;
    private final NationRepository nations;
    private final SeasonRepository seasons;
    private final SeasonAdminService seasonAdmin;
    private final AdminAccountService adminAccounts;
    private final UserRepository users;
    private final UserIdentityRepository identities;
    private final UserSeasonStatRepository userStats;
    private final NationSeasonStatRepository nationStats;
    private final RebuildService rebuildService;

    public SeedRunner(AppProperties props, NationRepository nations, SeasonRepository seasons,
                      SeasonAdminService seasonAdmin, AdminAccountService adminAccounts, UserRepository users,
                      UserIdentityRepository identities, UserSeasonStatRepository userStats,
                      NationSeasonStatRepository nationStats, RebuildService rebuildService) {
        this.props = props;
        this.nations = nations;
        this.seasons = seasons;
        this.seasonAdmin = seasonAdmin;
        this.adminAccounts = adminAccounts;
        this.users = users;
        this.identities = identities;
        this.userStats = userStats;
        this.nationStats = nationStats;
        this.rebuildService = rebuildService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!props.seed().enabled()) {
            log.info("Seeding disabled (app.seed.enabled=false)");
            return;
        }
        seedNations();
        Season season = seedActiveSeason();
        adminAccounts.ensureBootstrapAdmin(props.admin().bootstrapUser(), props.admin().bootstrapPassword());

        if (props.seed().simulatedNations()) {
            seedSimulatedData(season);
        }
        log.info("Seeding complete");
    }

    private void seedNations() {
        if (nations.count() > 0) {
            return;
        }
        List<Nation> toSave = NationCatalog.nations().stream()
                .map(e -> new Nation(UUID.randomUUID(), e.code(), e.name(), e.flagColors(), true))
                .toList();
        nations.saveAll(toSave);
        log.info("Seeded {} nations", toSave.size());
    }

    private Season seedActiveSeason() {
        Season active = seasons.findFirstByStatus(SeasonStatus.ACTIVE).orElse(null);
        if (active != null) {
            return active;
        }
        Instant now = Instant.now();
        Season created = seasonAdmin.create("Season 1 — Road to 2026", now, now.plus(90, ChronoUnit.DAYS));
        return seasonAdmin.activate(created.getId());
    }

    private void seedSimulatedData(Season season) {
        if (!userStats.findBySeasonId(season.getId()).isEmpty()) {
            log.info("Simulated data already present for season {}", season.getId());
            return;
        }
        List<Nation> activeNations = nations.findAllByActiveTrueOrderByNameAsc();
        if (activeNations.isEmpty()) {
            return;
        }
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Map<UUID, long[]> perNation = new HashMap<>(); // nationId -> [totalBest, contributorCount]

        List<User> newUsers = new ArrayList<>();
        List<UserIdentity> newIdentities = new ArrayList<>();
        List<UserSeasonStat> newStats = new ArrayList<>();

        for (int i = 0; i < SYNTHETIC_USERS; i++) {
            Nation nation = activeNations.get(rnd.nextInt(activeNations.size()));
            User u = new User();
            u.setId(UUID.randomUUID());
            u.setDisplayName("AI_Keeper_" + (i + 1));
            u.setNationId(nation.getId());
            u.setNationChosenAt(Instant.now());
            u.setSynthetic(true);
            // Set timestamps explicitly since we bypass the persistence lifecycle for batch save.
            u.setCreatedAt(Instant.now());
            u.setUpdatedAt(Instant.now());
            newUsers.add(u);

            UserIdentity id = new UserIdentity(u.getId(), AuthProvider.SYNTHETIC, "synthetic:" + u.getId(), null);
            id.setId(UUID.randomUUID());
            newIdentities.add(id);

            int playCount = 1 + rnd.nextInt(15);
            int bestScore = 50 + rnd.nextInt(2950);
            long total = (long) bestScore + (long) (playCount - 1) * (bestScore / 2L);

            UserSeasonStat stat = new UserSeasonStat();
            stat.setId(UUID.randomUUID());
            stat.setUserId(u.getId());
            stat.setSeasonId(season.getId());
            stat.setNationId(nation.getId());
            stat.setBestScore(bestScore);
            stat.setTotalScore(total);
            stat.setPlayCount(playCount);
            stat.setUpdatedAt(Instant.now());
            newStats.add(stat);

            long[] agg = perNation.computeIfAbsent(nation.getId(), k -> new long[2]);
            agg[0] += bestScore; // national total = sum of bestScore (§3.2)
            agg[1] += 1;
        }

        users.saveAll(newUsers);
        identities.saveAll(newIdentities);
        userStats.saveAll(newStats);

        List<NationSeasonStat> nationStatRows = new ArrayList<>();
        perNation.forEach((nationId, agg) -> {
            NationSeasonStat n = new NationSeasonStat();
            n.setId(UUID.randomUUID());
            n.setNationId(nationId);
            n.setSeasonId(season.getId());
            n.setTotalScore(agg[0]);
            n.setContributorCount((int) agg[1]);
            n.setUpdatedAt(Instant.now());
            nationStatRows.add(n);
        });
        nationStats.saveAll(nationStatRows);

        rebuildService.rebuildSeason(season.getId());
        log.info("Seeded {} synthetic users across {} nations and warmed Redis", SYNTHETIC_USERS, perNation.size());
    }
}

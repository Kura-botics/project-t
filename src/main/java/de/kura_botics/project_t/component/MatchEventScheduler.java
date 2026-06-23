package de.kura_botics.project_t.component;

import de.kura_botics.project_t.dto.api.ApiTeamDTO;
import de.kura_botics.project_t.service.FootballApiService;
import de.kura_botics.project_t.service.MatchScoringService;
import de.kura_botics.project_t.service.MatchSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers scheduled background tasks to synchronize match data.
 * Contains separate schedulers: a daily scheduler for full database updates
 * and a frequent live scheduler to track the completion status of ongoing matches.
 */
@Component
public class MatchEventScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MatchEventScheduler.class);

    private final LoadAllGamesToDB loadAllGamesToDB;
    private final MatchSyncService matchSyncService;
    private final FootballApiService footballApiService;
    private final MatchScoringService matchScoringService;

    public MatchEventScheduler(LoadAllGamesToDB loadAllGamesToDB,
                               MatchSyncService matchSyncService,
                               FootballApiService footballApiService,
                               MatchScoringService matchScoringService) {
        this.loadAllGamesToDB = loadAllGamesToDB;
        this.matchSyncService = matchSyncService;
        this.footballApiService = footballApiService;
        this.matchScoringService = matchScoringService;
    }

    /**
     * Executes daily at 09:00 AM server time.
     * Performs a complete update of the database to fetch new match information
     * and checks if a tournament winner has been decided to allocate points.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void dailyScheduler() {
        logger.info("Executing daily complete database update...");

        // TODO extract the core logic into a service
        loadAllGamesToDB.run();

        ApiTeamDTO winner = footballApiService.fetchTournamentWinner();
        if (winner != null && winner.tla() != null) {
            logger.info("Tournament winner found (TLA: {}). Evaluating guesses...", winner.tla());
            matchScoringService.evaluateWinner(winner.tla());
        }
    }

    /**
     * Executes every 10 minutes.
     * Scans today's matches to check if any ongoing games have finished.
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void liveScheduler() {
        logger.info("Scanning for finished live games...");
        matchSyncService.checkForFinishedGames();
    }
}
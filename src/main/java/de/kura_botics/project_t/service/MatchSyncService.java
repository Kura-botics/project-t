package de.kura_botics.project_t.service;

import de.kura_botics.project_t.dto.api.ApiMatchDTO;
import de.kura_botics.project_t.dto.api.ApiMatchResponseDTO;
import de.kura_botics.project_t.entity.Match;
import de.kura_botics.project_t.entity.MatchStatus;
import de.kura_botics.project_t.repository.MatchRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for synchronizing ongoing and past matches with the external API.
 * Identifies finished games, updates final scores, and triggers the point evaluation.
 */
@Service
public class MatchSyncService {

    private static final Logger logger = LoggerFactory.getLogger(MatchSyncService.class);

    private final FootballApiService footballApiService;
    private final MatchRepository matchRepository;
    private final MatchScoringService matchScoringService;

    public MatchSyncService(FootballApiService footballApiService, MatchRepository matchRepository,
                            MatchScoringService matchScoringService) {
        this.footballApiService = footballApiService;
        this.matchRepository = matchRepository;
        this.matchScoringService = matchScoringService;
    }

    /**
     * Checks for games that have started but haven't been scored yet.
     * Fetches current data from the API and updates the local database.
     * If a game is marked as FINISHED, it triggers the scoring evaluation.
     */
    @Transactional
    public void checkForFinishedGames() {
        logger.debug("Scanning for finished games...");

        List<Match> candidates = matchRepository.findUnscoredMatchesInPast(Instant.now());
        if (candidates.isEmpty()) {
            return;
        }

        logger.info("Found {} unchecked games in the past. Requesting API update...", candidates.size());
        ApiMatchResponseDTO response = footballApiService.fetchAllMatches();

        if (response == null || response.matches() == null) {
            logger.warn("No match data received from the API during sync.");
            return;
        }

        // Optimize lookup: Map API matches by their ID to avoid nested loops
        Map<Integer, ApiMatchDTO> apiMatchMap = response.matches().stream()
                .collect(Collectors.toMap(ApiMatchDTO::id, Function.identity(), (existing, replacement) -> existing));

        for (Match dbMatch : candidates) {
            ApiMatchDTO apiMatchDTO = apiMatchMap.get(dbMatch.getApiMatchId());

            if (apiMatchDTO != null) {
                updateMatchData(dbMatch, apiMatchDTO);
            }
        }
    }

    /**
     * Updates a single database match with new data from the API and triggers scoring if finished.
     *
     * @param dbMatch     The existing database entity.
     * @param apiMatchDTO The fresh data from the API.
     */
    private void updateMatchData(Match dbMatch, ApiMatchDTO apiMatchDTO) {
        if (apiMatchDTO.score() != null && apiMatchDTO.score().fullTime() != null) {
            dbMatch.setGoalsHome(apiMatchDTO.score().fullTime().home());
            dbMatch.setGoalsAway(apiMatchDTO.score().fullTime().away());
        }

        dbMatch.setStatus(translateStatus(apiMatchDTO.status()));

        if (dbMatch.getStatus() == MatchStatus.FINISHED) {
            logger.info("Match finished: {} vs {}", dbMatch.getHomeTeam(), dbMatch.getAwayTeam());
            logger.info("Final score: {}:{}", dbMatch.getGoalsHome(), dbMatch.getGoalsAway());

            matchScoringService.evaluateMatch(dbMatch);
            dbMatch.setScored(true);
        }

        matchRepository.save(dbMatch);
    }

    /**
     * Converts the raw status string from the API into the domain-specific MatchStatus enum.
     *
     * @param apiStatus The status string provided by the API.
     * @return The corresponding MatchStatus enum value.
     */
    private MatchStatus translateStatus(String apiStatus) {
        if (apiStatus == null) {
            return MatchStatus.UPCOMING;
        }
        return switch (apiStatus) {
            case "FINISHED" -> MatchStatus.FINISHED;
            case "IN_PLAY", "PAUSED" -> MatchStatus.LIVE;
            default -> MatchStatus.UPCOMING;
        };
    }
}
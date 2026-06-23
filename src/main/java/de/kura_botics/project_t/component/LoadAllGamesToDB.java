package de.kura_botics.project_t.component;

import de.kura_botics.project_t.dto.api.ApiMatchDTO;
import de.kura_botics.project_t.dto.api.ApiMatchResponseDTO;
import de.kura_botics.project_t.dto.api.ApiTeamDTO;
import de.kura_botics.project_t.entity.Match;
import de.kura_botics.project_t.entity.MatchStatus;
import de.kura_botics.project_t.repository.MatchRepository;
import de.kura_botics.project_t.service.FootballApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the database with match data on application startup.
 * Fetches all match data from the external football API and synchronizes it
 * with the local database, either by updating existing records or creating new ones.
 */
@Component
public class LoadAllGamesToDB implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(LoadAllGamesToDB.class);

    private final FootballApiService footballApiService;
    private final MatchRepository matchRepository;

    public LoadAllGamesToDB(FootballApiService footballApiService, MatchRepository matchRepository) {
        this.footballApiService = footballApiService;
        this.matchRepository = matchRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("Requesting match data from the Football API...");
        ApiMatchResponseDTO response = footballApiService.fetchAllMatches();

        if (response == null || response.matches() == null) {
            logger.warn("No match data received from the API.");
            return;
        }

        List<Match> matchesToSave = new ArrayList<>();

        for (ApiMatchDTO apiMatch : response.matches()) {
            Match dbMatch = matchRepository.findByApiMatchId(apiMatch.id())
                    .orElseGet(Match::new);

            updateMatchWithApiData(dbMatch, apiMatch);
            matchesToSave.add(dbMatch);
        }

        matchRepository.saveAll(matchesToSave);
        logger.info("Successfully synced {} games to the database.", matchesToSave.size());
    }

    /**
     * Maps the data from the API Data Transfer Object to the database Entity.
     *
     * @param dbMatch  The database entity to update.
     * @param apiMatch The API DTO containing the source data.
     */
    private void updateMatchWithApiData(Match dbMatch, ApiMatchDTO apiMatch) {
        dbMatch.setApiMatchId(apiMatch.id());

        if (apiMatch.utcDate() != null) {
            dbMatch.setKickoffDate(Instant.parse(apiMatch.utcDate()));
        }

        dbMatch.setHomeTeam(extractTeamTla(apiMatch.homeTeam()));
        dbMatch.setAwayTeam(extractTeamTla(apiMatch.awayTeam()));

        if (apiMatch.score() != null && apiMatch.score().fullTime() != null) {
            dbMatch.setGoalsHome(apiMatch.score().fullTime().home());
            dbMatch.setGoalsAway(apiMatch.score().fullTime().away());
        }

        dbMatch.setStatus(translateStatus(apiMatch.status()));
    }

    /**
     * Safely extracts the Team's Three Letter Acronym (TLA).
     * Defaults to a placeholder if the team or TLA is missing.
     *
     * @param team The ApiTeamDTO containing the team details.
     * @return The TLA string, or a default placeholder.
     */
    private String extractTeamTla(ApiTeamDTO team) {
        return (team != null && team.tla() != null) ? team.tla() : "TBD";
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
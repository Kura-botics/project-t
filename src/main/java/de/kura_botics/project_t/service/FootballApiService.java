package de.kura_botics.project_t.service;

import de.kura_botics.project_t.dto.api.ApiCompetitionResponse;
import de.kura_botics.project_t.dto.api.ApiMatchResponseDTO;
import de.kura_botics.project_t.dto.api.ApiTeamDTO;
import de.kura_botics.project_t.entity.Team;
import de.kura_botics.project_t.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all external HTTP communications with the football-data.org API.
 * Responsible for fetching match schedules, team data, and tournament results.
 */
@Service
public class FootballApiService {

    private static final Logger logger = LoggerFactory.getLogger(FootballApiService.class);

    // TODO (Security/Config): Move this key to application.properties and inject
    private static final String API_KEY = "INSERT YOUR OWN KEY HERE";

    private static final String BASE_URL = "https://api.football-data.org/v4/competitions/2000";
    private static final String MATCHES_URL = BASE_URL + "/matches";
    private static final String TEAMS_URL = BASE_URL + "/teams";

    private final RestTemplate restTemplate;
    private final TeamRepository teamRepository;

    public FootballApiService(TeamRepository teamRepository) {
        this.restTemplate = new RestTemplate();
        this.teamRepository = teamRepository;
    }

    /**
     * Fetches all match data for the specified competition from the external API.
     *
     * @return DTO containing the list of matches, or null if the request fails.
     */
    public ApiMatchResponseDTO fetchAllMatches() {
        logger.info("Requesting all match data from the API...");

        try {
            ResponseEntity<ApiMatchResponseDTO> response = restTemplate.exchange(
                    MATCHES_URL,
                    HttpMethod.GET,
                    createAuthEntity(),
                    ApiMatchResponseDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to fetch matches from API. Reason: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Fetches the overall tournament data to check if a winner has been decided.
     *
     * @return DTO containing the winning team's data, or null if undecided or the request fails.
     */
    public ApiTeamDTO fetchTournamentWinner() {
        logger.info("Requesting tournament winner data from the API...");

        try {
            ResponseEntity<ApiCompetitionResponse> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.GET,
                    createAuthEntity(),
                    ApiCompetitionResponse.class
            );

            if (response.getBody() != null && response.getBody().season() != null) {
                return response.getBody().season().winner();
            }
        } catch (Exception e) {
            logger.error("Failed to fetch tournament winner from API. Reason: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Fetches all participating teams from the API and persists them to the local database.
     */
    public void fetchAllTeams() {
        logger.info("Requesting all team data from the API...");

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    TEAMS_URL,
                    HttpMethod.GET,
                    createAuthEntity(),
                    JsonNode.class
            );

            JsonNode root = response.getBody();

            if (root != null && root.has("teams")) {
                List<Team> teamsToSave = new ArrayList<>();

                for (JsonNode teamNode : root.get("teams")) {
                    if (!teamNode.hasNonNull("tla")) continue;

                    String tla = teamNode.get("tla").asString();
                    String name = teamNode.hasNonNull("name") ? teamNode.get("name").asString() : "UNKNOWN";
                    String crest = teamNode.hasNonNull("crest") ? teamNode.get("crest").asString() : null;
                    String clubColours = teamNode.hasNonNull("clubColors") ? teamNode.get("clubColors").asString() : null;

                    Team team = new Team(tla, name, crest, clubColours);
                    teamsToSave.add(team);
                }

                teamRepository.saveAll(teamsToSave);
                logger.info("Successfully saved {} teams to the database.", teamsToSave.size());
            }
        } catch (Exception e) {
            logger.error("Failed to fetch or save teams from API. Reason: {}", e.getMessage());
        }
    }

    /**
     * Automatically triggers when the Spring application finishes starting up.
     * Populates the team database if it is currently empty.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initTeamsOnStartup() {
        if (teamRepository.count() == 0) {
            logger.info("Team database is empty. Initializing teams on startup...");
            fetchAllTeams();
        }
    }

    /**
     * Helper method to generate the required HTTP headers for API authentication.
     *
     * @return HttpEntity containing the authorization token.
     */
    private HttpEntity<String> createAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY);
        return new HttpEntity<>(headers);
    }
}
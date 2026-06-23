package de.kura_botics.project_t.dto.api;

/**
 * Data Transfer Object representing the root response when fetching tournament competition data from the API.
 *
 * @param season The DTO containing specific details about the current season, including the tournament winner.
 */
public record ApiCompetitionResponse(ApiSeasonDTO season) {
}
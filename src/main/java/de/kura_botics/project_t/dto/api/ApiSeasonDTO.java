package de.kura_botics.project_t.dto.api;

/**
 * Data Transfer Object representing a specific season of a competition.
 *
 * @param winner The DTO containing information about the team that won the tournament.
 */
public record ApiSeasonDTO(ApiTeamDTO winner) {
}
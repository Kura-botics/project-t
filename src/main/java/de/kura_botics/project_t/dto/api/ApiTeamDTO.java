package de.kura_botics.project_t.dto.api;

/**
 * Data Transfer Object representing a football team.
 *
 * @param tla The Three Letter Acronym (TLA) identifying the team (e.g., "GER", "ARG").
 */
public record ApiTeamDTO(String tla) {
}
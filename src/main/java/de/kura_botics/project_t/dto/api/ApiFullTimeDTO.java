package de.kura_botics.project_t.dto.api;

/**
 * Data Transfer Object representing the final full-time score of a match.
 *
 * @param home The number of goals scored by the home team.
 * @param away The number of goals scored by the away team.
 */
public record ApiFullTimeDTO(Integer home, Integer away) {
}
package de.kura_botics.project_t.dto.api;

/**
 * Data Transfer Object representing a match fetched from the external football API.
 *
 * @param id       The unique identifier for the match provided by the external API.
 * @param utcDate  The scheduled kickoff date and time in UTC format (ISO 8601 string).
 * @param status   The current status of the match (e.g., TIMED, IN_PLAY, FINISHED).
 * @param homeTeam The DTO containing information about the home team.
 * @param awayTeam The DTO containing information about the away team.
 * @param score    The DTO containing the scoring details of the match.
 */
public record ApiMatchDTO(
        Integer id,
        String utcDate,
        String status,
        ApiTeamDTO homeTeam,
        ApiTeamDTO awayTeam,
        ApiScoreDTO score
) {
}
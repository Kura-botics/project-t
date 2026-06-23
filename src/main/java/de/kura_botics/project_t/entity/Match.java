package de.kura_botics.project_t.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing a football match in the database.
 * Maps to the "matches" table and holds data synced from the external API
 * as well as the scoring status for the prediction logic.
 */
@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @Column(name = "home_team", nullable= false, columnDefinition= "CHAR(3)")
    private String homeTeam;

    @Column(name = "away_team", nullable= false, columnDefinition= "CHAR(3)")
    private String awayTeam;

    @Column(name = "goals_home")
    private Integer goalsHome;

    //Note: forgot the "s" at the end of goals, but its to late to change now lol
    @Column(name = "goal_away")
    private Integer goalsAway;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable= false)
    private MatchStatus status = MatchStatus.UPCOMING;

    @Column(name = "api_match_id", unique = true)
    private Integer apiMatchId;

    @Column(name = "kickoff_date")
    private Instant kickoffDate;

    @Column(name = "is_scored")
    private boolean isScored = false;
}
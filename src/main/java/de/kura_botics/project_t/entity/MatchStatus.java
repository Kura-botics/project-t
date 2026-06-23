package de.kura_botics.project_t.entity;

/**
 * Represents the current state of a football match.
 * Persisted as a String in the database to ensure data integrity
 * regardless of the enum's declaration order.
 */
public enum MatchStatus{
    UPCOMING, // DB value = 0
    LIVE, // DB value = 1
    FINISHED // DB value = 2
}
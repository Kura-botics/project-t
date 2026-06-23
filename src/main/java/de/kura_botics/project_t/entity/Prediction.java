package de.kura_botics.project_t.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a prediction made in the database.
 * Maps to the "predictions" table and holds the data for a prediction
 * made by a user linked to one specific match. Also holds the points gained
 * by the prediction if it was scored
 */
@Entity
@Table(name = "predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prediction{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name= "user_id", nullable= false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable= false)
    private Match match;

    @Column(name ="prediction_goal_home")
    private Integer predictionGoalHome;

    @Column(name ="prediction_goal_away")
    private Integer predictionGoalAway;

    @Column(name = "points_earned",nullable= false)
    private Integer pointsEarned = 0;
}
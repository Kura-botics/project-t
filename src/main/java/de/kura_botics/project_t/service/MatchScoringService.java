package de.kura_botics.project_t.service;

import de.kura_botics.project_t.entity.Match;
import de.kura_botics.project_t.entity.Prediction;
import de.kura_botics.project_t.entity.SeasonalScore;
import de.kura_botics.project_t.entity.WinnerPrediction;
import de.kura_botics.project_t.repository.PredictionRepository;
import de.kura_botics.project_t.repository.SeasonalScoreRepository;
import de.kura_botics.project_t.repository.UserRepository;
import de.kura_botics.project_t.repository.WinnerPredictionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service responsible for evaluating user predictions against actual match results
 * and allocating points to the user's seasonal score.
 */
@Service
public class MatchScoringService {

    private static final Logger logger = LoggerFactory.getLogger(MatchScoringService.class);

    //TODO move point scoring to application.properties
    private static final int CORRECT_WC_WINNER = 15;
    private static final int CORRECT_GUESS = 3;
    private static final int CORRECT_DIFF = 2;
    private static final int CORRECT_WINNER = 1;

    private final PredictionRepository predictionRepository;
    private final SeasonalScoreRepository seasonalScoreRepository;
    private final UserRepository userRepository;
    private final WinnerPredictionRepository winnerPredictionRepository;

    public MatchScoringService(PredictionRepository predictionRepository,
                               SeasonalScoreRepository seasonalScoreRepository,
                               UserRepository userRepository,
                               WinnerPredictionRepository winnerPredictionRepository) {
        this.predictionRepository = predictionRepository;
        this.seasonalScoreRepository = seasonalScoreRepository;
        this.userRepository = userRepository;
        this.winnerPredictionRepository = winnerPredictionRepository;
    }

    /**
     * Evaluates all predictions for a given match and assigns points based on accuracy.
     *
     * @param match The finished match containing the final score.
     */
    @Transactional
    public void evaluateMatch(Match match) {
        if (match.getGoalsAway() == null || match.getGoalsHome() == null) {
            return;
        }

        int actualGoalsHome = match.getGoalsHome();
        int actualGoalsAway = match.getGoalsAway();

        List<Prediction> predictionList = predictionRepository.findByMatch(match);

        for (Prediction prediction : predictionList) {
            if (prediction.getPredictionGoalHome() == null || prediction.getPredictionGoalAway() == null) {
                continue;
            }

            int predGoalsHome = prediction.getPredictionGoalHome();
            int predGoalsAway = prediction.getPredictionGoalAway();

            int points = 0;
            boolean isExact = false;
            boolean isDiff = false;
            boolean isWinner = false;

            if (predGoalsHome == actualGoalsHome && predGoalsAway == actualGoalsAway) {
                points = CORRECT_GUESS;
                isExact = true;
            } else if ((predGoalsHome - predGoalsAway) == (actualGoalsHome - actualGoalsAway)) {
                points = CORRECT_DIFF;
                isDiff = true;
            } else if (Integer.signum(predGoalsHome - predGoalsAway) == Integer.signum(actualGoalsHome - actualGoalsAway)) {
                // Integer.signum compares if both differences are strictly positive, negative, or zero
                points = CORRECT_WINNER;
                isWinner = true;
            }

            prediction.setPointsEarned(points);
            predictionRepository.save(prediction);

            updateUserScore(prediction.getUser().getId(), points, isExact, isDiff, isWinner);
        }

        logger.debug("Successfully evaluated {} predictions for match ID: {}", predictionList.size(), match.getId());
    }

    /**
     * Evaluates the overall tournament winner predictions and allocates bonus points.
     *
     * @param actualWinner The Three Letter Acronym (TLA) of the winning team.
     */
    @Transactional
    public void evaluateWinner(String actualWinner) {
        List<WinnerPrediction> predictions = winnerPredictionRepository.findByIsScoredFalse();

        if (predictions.isEmpty()) {
            return;
        }

        logger.info("Tournament winner is set to: {}. Analyzing {} predictions...", actualWinner, predictions.size());

        for (WinnerPrediction prediction : predictions) {
            if (actualWinner.equals(prediction.getTeam())) {
                SeasonalScore score = getOrCreateSeasonalScore(prediction.getUser().getId());

                score.setTotalPoints(score.getTotalPoints() + CORRECT_WC_WINNER);
                score.setCw(score.getCw() + 1);

                seasonalScoreRepository.save(score);
            }
            prediction.setScored(true);
            winnerPredictionRepository.save(prediction);
        }
    }

    /**
     * Helper method to update a user's seasonal score totals based on a specific match prediction result.
     */
    private void updateUserScore(Long userId, int points, boolean isExact, boolean isDiff, boolean isWinner) {
        SeasonalScore score = getOrCreateSeasonalScore(userId);

        score.setTotalPoints(score.getTotalPoints() + points);

        if (isExact) score.setCg(score.getCg() + 1);
        if (isDiff) score.setCgd(score.getCgd() + 1);
        if (isWinner) score.setCw(score.getCw() + 1);

        seasonalScoreRepository.save(score);
    }

    /**
     * Helper method to retrieve an existing SeasonalScore for a user or initialize a new one.
     *
     * @param userId The ID of the user.
     * @return The existing or newly created SeasonalScore entity.
     */
    private SeasonalScore getOrCreateSeasonalScore(Long userId) {
        return seasonalScoreRepository.findById(userId).orElseGet(() -> {
            SeasonalScore newScore = new SeasonalScore();
            newScore.setUser(userRepository.getReferenceById(userId));
            return newScore;
        });
    }
}
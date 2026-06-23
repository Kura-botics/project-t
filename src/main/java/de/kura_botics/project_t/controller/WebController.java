package de.kura_botics.project_t.controller;

import de.kura_botics.project_t.entity.*;
import de.kura_botics.project_t.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main web controller handling the dashboard, match details, and user predictions.
 * Note: Business logic is currently handled directly via repositories.
 */
// TODO (Architecture): Extract business logic and repository calls into dedicated services
//  (e.g., DashboardService, PredictionService).
//  The controller should only handle HTTP routing and view model mapping.
@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;
    private final SeasonalScoreRepository seasonalScoreRepository;
    private final WinnerPredictionRepository winnerPredictionRepository;
    private final TeamRepository teamRepository;

    public WebController(MatchRepository matchRepository, PredictionRepository predictionRepository,
                         UserRepository userRepository, SeasonalScoreRepository seasonalScoreRepository,
                         WinnerPredictionRepository winnerPredictionRepository, TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.userRepository = userRepository;
        this.seasonalScoreRepository = seasonalScoreRepository;
        this.winnerPredictionRepository = winnerPredictionRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * Displays the dashboard, including today's matches, the top 3 leaderboard, and the user's tournament winner bet.
     */
    @GetMapping("/")
    public String showDashboard(Model model, Principal principal) {
        WinnerPrediction userPrediction = null;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                userPrediction = winnerPredictionRepository.findByUserId(user.getId()).orElse(null);
            }
        }

        List<Match> allMatches = matchRepository.findAll(Sort.by(Sort.Direction.ASC, "kickoffDate"));

        ZonedDateTime matchDayStart = calculateLogicalMatchDayStart();
        ZonedDateTime matchDayEnd = matchDayStart.plusDays(1);

        List<Match> todaysMatches = allMatches.stream()
                .filter(m -> {
                    ZonedDateTime kickoff = m.getKickoffDate().atZone(ZoneId.systemDefault());
                    return !kickoff.isBefore(matchDayStart) && kickoff.isBefore(matchDayEnd);
                })
                .collect(Collectors.toList());

        boolean isTournamentLocked = false;
        if (!allMatches.isEmpty()) {
            isTournamentLocked = isMatchLocked(allMatches.get(0).getKickoffDate());
        }

        List<SeasonalScore> fullLeaderboard = seasonalScoreRepository.findAll(
                Sort.by(Sort.Direction.DESC, "totalPoints").and(Sort.by(Sort.Direction.DESC, "cw"))
        );
        List<SeasonalScore> top3Leaderboard = fullLeaderboard.stream().limit(3).collect(Collectors.toList());

        // load teams to map
        List<Team> allTeams = teamRepository.findAll();
        Map<String, Team> teamMap = allTeams.stream()
                .collect(Collectors.toMap(Team::getTla, team -> team));

        //get tla for drop down menu
        List<String> teamTlas = allTeams.stream()
                .map(Team::getTla)
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("todaysMatches", todaysMatches);
        model.addAttribute("top3Leaderboard", top3Leaderboard);
        model.addAttribute("winnerPrediction", userPrediction);
        model.addAttribute("isTournamentLocked", isTournamentLocked);

        // give dynamic teams and map to frontend
        model.addAttribute("teams", teamTlas);
        model.addAttribute("teamMap", teamMap);

        return "dashboard";
    }
    /**
     * Displays the details of a specific match, including team details and user predictions.
     */
    @GetMapping("/match/{id}")
    public String showMatchDetails(@PathVariable int id, Model model) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game with ID " + id + " not found"));

        List<Prediction> predictions = predictionRepository.findByMatch(match);
        boolean isLocked = isMatchLocked(match.getKickoffDate());

        Team homeTeamDetails = teamRepository.findById(match.getHomeTeam()).orElse(null);
        Team awayTeamDetails = teamRepository.findById(match.getAwayTeam()).orElse(null);
        String matchGradient = buildMatchGradient(homeTeamDetails, awayTeamDetails);

        model.addAttribute("match", match);
        model.addAttribute("predictions", predictions);
        model.addAttribute("isLocked", isLocked);
        model.addAttribute("homeTeamDetails", homeTeamDetails);
        model.addAttribute("awayTeamDetails", awayTeamDetails);
        model.addAttribute("matchGradient", matchGradient);

        return "match-details";
    }

    /**
     * Processes a user's score prediction for a specific match.
     */
    @PostMapping("/match/{id}/predict")
    public String submitPrediction(@PathVariable int id,
                                   @RequestParam int homeGoals,
                                   @RequestParam int awayGoals,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (isMatchLocked(match.getKickoffDate())) {
            logger.warn("User {} attempted to predict locked match {}", user.getUsername(), id);
            redirectAttributes.addFlashAttribute("error", "Zu spät! Die Tippabgabe ist bereits gesperrt!");
            return "redirect:/match/" + id;
        }

        Prediction prediction = predictionRepository.findByMatch(match).stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseGet(Prediction::new);

        prediction.setMatch(match);
        prediction.setUser(user);
        prediction.setPredictionGoalHome(homeGoals);
        prediction.setPredictionGoalAway(awayGoals);
        prediction.setPointsEarned(0);

        predictionRepository.save(prediction);
        logger.info("Prediction saved for user {} on match {}", user.getUsername(), id);

        redirectAttributes.addFlashAttribute("success", "Dein Tipp wurde erfolgreich gespeichert");
        return "redirect:/match/" + id;
    }

    /**
     * Displays a list of all upcoming and past matches.
     */
    @GetMapping("/matches")
    public String showAllMatches(Model model) {
        List<Match> matches = matchRepository.findAll(Sort.by(Sort.Direction.ASC, "kickoffDate"));
        model.addAttribute("matches", matches);
        return "matches";
    }

    /**
     * Displays the full leaderboard scoreboard.
     */
    @GetMapping("/scoreboard")
    public String showScoreboard(Model model) {
        List<SeasonalScore> leaderboard = seasonalScoreRepository.findAll(
                Sort.by(Sort.Direction.DESC, "totalPoints").and(Sort.by(Sort.Direction.DESC, "cw"))
        );
        model.addAttribute("leaderboard", leaderboard);
        return "scoreboard";
    }

    /**
     * Processes a user's overall tournament winner prediction.
     */
    @PostMapping("/winner/predict")
    public String submitWinnerPrediction(@RequestParam String team, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Match> matches = matchRepository.findAll(Sort.by(Sort.Direction.ASC, "kickoffDate"));
        if (!matches.isEmpty() && isMatchLocked(matches.get(0).getKickoffDate())) {
            logger.warn("User {} attempted to predict tournament winner after lock", user.getUsername());
            redirectAttributes.addFlashAttribute("winnerError", "Zu spät! Das Turnier hat bereits begonnen");
            return "redirect:/";
        }

        WinnerPrediction prediction = winnerPredictionRepository.findByUserId(user.getId())
                .orElseGet(WinnerPrediction::new);

        prediction.setUser(user);
        prediction.setTeam(team);

        winnerPredictionRepository.save(prediction);
        logger.info("Tournament winner prediction saved for user {}", user.getUsername());

        redirectAttributes.addFlashAttribute("winnerSucess", "Dein Weltmeistertipp wurde gespeichert");
        return "redirect:/";
    }

    // --- Helper Methods ---

    /**
     * Determines if a match is locked for predictions (60 minutes prior to kickoff).
     */
    // TODO (Config):
    //  Extract the 60-minute lockout duration into application.properties
    //  so it can be changed without recompiling the code.
    private boolean isMatchLocked(Instant kickoffDate) {
        return Instant.now().isAfter(kickoffDate.minus(Duration.ofMinutes(60)));
    }

    /**
     * Calculates the start of the logical match day (09:00 AM to 09:00 AM next day).
     */
    private ZonedDateTime calculateLogicalMatchDayStart() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        if (now.getHour() < 9) {
            return now.minusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        }
        return now.withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Builds a CSS linear gradient string using the primary and secondary colors of two teams.
     */
    private String buildMatchGradient(Team homeTeam, Team awayTeam) {
        String h1 = "#64748B", h2 = "#64748B";
        if (homeTeam != null && homeTeam.getClubColors() != null) {
            String[] parts = homeTeam.getClubColors().split("/");
            h1 = mapColorToHex(parts[0]);
            h2 = parts.length > 1 ? mapColorToHex(parts[1]) : h1;
        }

        String a1 = "#64748B", a2 = "#64748B";
        if (awayTeam != null && awayTeam.getClubColors() != null) {
            String[] parts = awayTeam.getClubColors().split("/");
            a1 = mapColorToHex(parts[0]);
            a2 = parts.length > 1 ? mapColorToHex(parts[1]) : a1;
        }

        return String.format("background: linear-gradient(to right, %s, %s, %s, %s);", h1, h2, a1, a2);
    }

    /**
     * Translates English color names to corresponding Tailwind hex codes.
     */
    // TODO (Refactor): Extract color mapping logic into a standalone Enum (e.g., TeamColor)
    //  or move team configurations to a dedicated database table.
    private String mapColorToHex(String color) {
        if (color == null) return "#64748B";

        return switch (color.trim().toLowerCase()) {
            case "white" -> "#FFFFFF";
            case "black" -> "#000000";
            case "red" -> "#EF4444";
            case "blue" -> "#3B82F6";
            case "green" -> "#22C55E";
            case "yellow" -> "#EAB308";
            case "orange" -> "#F97316";
            case "sky blue" -> "#38BDF8";
            case "navy blue" -> "#1E3A8A";
            case "gold" -> "#FACC15";
            case "maroon" -> "#800000";
            default -> "#64748B";
        };
    }
}
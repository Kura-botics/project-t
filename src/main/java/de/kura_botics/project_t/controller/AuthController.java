package de.kura_botics.project_t.controller;

import de.kura_botics.project_t.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles web requests related to user authentication, including login and registration.
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_REGISTER = "register";
    private static final String REDIRECT_LOGIN_SUCCESS = "redirect:/login?registered=true";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the user login form.
     *
     * @return The name of the login view template.
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return VIEW_LOGIN;
    }

    /**
     * Displays the user registration form.
     *
     * @return The name of the register view template.
     */
    @GetMapping("/register")
    public String showRegisterForm() {
        return VIEW_REGISTER;
    }

    /**
     * Processes the submission of the registration form.
     *
     * @param username  The user's desired username.
     * @param password  The user's desired password.
     * @param firstname The user's first name.
     * @param lastname  The user's last name.
     * @param model     The Spring MVC model to pass attributes back to the view.
     * @return A redirect to the login page on success, or the register view with an error message on failure.
     */
    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String firstname,
                                      @RequestParam String lastname,
                                      Model model) {
        logger.info("Processing registration attempt for username: {}", username);

        try {
            userService.registerNewUser(username, password, firstname, lastname);
            logger.info("Successfully registered new user: {}", username);
            return REDIRECT_LOGIN_SUCCESS;

        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed for username: {}. Reason: {}", username, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return VIEW_REGISTER;
        }
    }
}
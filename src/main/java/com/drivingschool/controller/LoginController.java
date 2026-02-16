package com.drivingschool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Login Controller - Handles authetication pages and dashboard routing
 */

@Controller
public class LoginController {

    /**
     * Show login page
     * URL: GET /login
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Dashboard routing based on user role
     * URL: GET /dashboard
     *
     * After successful login, redirect user to their appropriate dashboard:
     *  - ADMIN         -> /admin/trainees (admin dashboard)
     *  - INSTRUCTOR    -> /instructor/dashboard
     *  - TRAINEE       -> /trainee/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        // Get user's role
        for (GrantedAuthority authority: authentication.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (role.equals("ROLE_INSTRUCTOR")) {
                return "redirect:/instructor/dashboard";
            } else if (role.equals("ROLE_TRAINEE")) {
                return "redirect:/trainee/dashboard";
            }
        }

        // default fallback
        return "redirect:/login";
    }

    /**
     * Access denied page
     * URL: GET /access-denied
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage",
                "You don't have permission to access this page.");
        return "access-denied";
    }
}

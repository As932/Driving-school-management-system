package com.drivingschool.controller;

import com.drivingschool.model.AppUser;
import com.drivingschool.model.Instructor;
import com.drivingschool.model.Session;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.AppUserRepository;
import com.drivingschool.repository.InstructorRepository;
import com.drivingschool.repository.SessionRepository;
import com.drivingschool.repository.TraineeRepository;
import org.springframework.boot.Banner;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instructor Dashboard Controller
 * Handles instructor specific views and data
 */

@Controller
@RequestMapping("/instructor")
public class InstructorDashboardController {

    private final InstructorRepository instructorRepository;
    private final SessionRepository sessionRepository;
    private final TraineeRepository traineeRepository;
    private final AppUserRepository appUserRepository;

    public InstructorDashboardController(InstructorRepository instructorRepository,
                                         SessionRepository sessionRepository,
                                         TraineeRepository traineeRepository,
                                         AppUserRepository appUserRepository) {
        this.instructorRepository = instructorRepository;
        this.sessionRepository = sessionRepository;
        this.traineeRepository = traineeRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Helper method to find instructor by username
     */
    private Instructor findInstructorByUsername(String username) {
        AppUser appUser = appUserRepository.findByUsername(username);

        if (appUser == null) {
            return null;
        }

        // get user id
        Integer userId = appUser.getUserId();

        // find instructor by name
        List<Instructor> allInstructors = instructorRepository.findAll();

        return allInstructors.stream()
                .filter(i -> i.getUserId() != null && i.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Main instructor dashboard
     * URL: GET /instructor/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // get logged-in username
        String username = authentication.getName();

        // find instructor by username
        Instructor instructor = findInstructorByUsername(username);

        if (instructor == null) {
            model.addAttribute("errorMessage",
                    "Instructor profile not found");
            return "error";
        }

        // get instructor sessions
        List<Session> allSessions = sessionRepository.findByInstructorId(instructor.getInstructorId());

        // filter upcoming sessions
        List<Session> upcomingSessions = allSessions.stream()
                .filter(s -> s.isScheduled())
                .filter(s -> s.isUpcoming())
                .sorted((s1, s2) -> s1.getStartDateTime().compareTo(s2.getStartDateTime()))
                .limit(5)
                .toList();

        // filter completed sessions
        List<Session> completedSessions = allSessions.stream()
                .filter(s -> s.isCompleted())
                .sorted((s1, s2) -> s2.getEndDateTime().compareTo(s1.getEndDateTime()))
                .limit(5)
                .toList();

        // get assigned trainees
        List<Trainee> assignedTrainees = traineeRepository.findByInstructorId(instructor.getInstructorId());

        // calculate statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", allSessions.size());
        stats.put("upcomingSessions", upcomingSessions.size());
        stats.put("completedSessions", completedSessions.size());
        stats.put("assignedTrainees", assignedTrainees.size());

        // calculate total hours taught (completed sessions only)
        double totalHours = completedSessions.stream()
                .mapToDouble(Session::getDurationHours)
                .sum();
        stats.put("totalHours", totalHours);

        // add to model
        model.addAttribute("instructor", instructor);
        model.addAttribute("upcomingSessions", upcomingSessions);
        model.addAttribute("completedSessions", completedSessions);
        model.addAttribute("assignedTrainees", assignedTrainees);
        model.addAttribute("stats", stats);
        model.addAttribute("username", username);

        return "instructor/dashboard";
    }

    /**
     * My schedule - all sessions for this instructor
     * URL: GET /instructor/schedule
     */
    @GetMapping("/schedule")
    public String Schedule(Authentication authentication, Model model) {
        String username = authentication.getName();
        Instructor instructor = findInstructorByUsername(username);

        if (instructor == null) {
            model.addAttribute("errorMessage",
                    "Instructor profile not found");
            return "error";
        }

        List<Session> sessions = sessionRepository.findByInstructorId(instructor.getInstructorId());

        model.addAttribute("instructor", instructor);
        model.addAttribute("sessions", sessions);
        model.addAttribute("username", username);

        return "instructor/schedule";
    }

    /**
     * My trainees - all assigned trainees
     * URL: GET /instructor/trainees
     */
    @GetMapping("/trainees")
    public String trainees(Authentication authentication, Model model) {
        String username = authentication.getName();
        Instructor instructor = findInstructorByUsername(username);

        if (instructor == null) {
            model.addAttribute("errorMessage",
                    "Instructor profile not found");
            return "error";
        }

        List<Trainee> trainees = traineeRepository.findByInstructorId(instructor.getInstructorId());

        model.addAttribute("instructor", instructor);
        model.addAttribute("trainees", trainees);
        model.addAttribute("username", username);

        return "instructor/trainees";
    }

}

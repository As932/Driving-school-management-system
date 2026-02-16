package com.drivingschool.controller;

import com.drivingschool.model.Instructor;
import com.drivingschool.model.Session;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.InstructorRepository;
import com.drivingschool.repository.TraineeRepository;
import com.drivingschool.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Session Controller - Handles HTTP requests for session management
 * URL Mapping:
 *  - GET /admin/sessions               -> List all sessions
 *  - GET /admin/sessions/add           -> Show add form
 *  - POST /admin/sessions/add          -> Process add form
 *  - GET /admin/sessions/edit/{id}     -> Show edit form
 *  - POST /admin/sessions/edit/{id}    -> Process edit form
 *  - GET /admin/sessions/delete/{id}   -> Delete session
 *  - POST /admin/sessions/{id}/feedback    -> Add feedback
 */

@Controller
@RequestMapping("/admin/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final InstructorRepository instructorRepository;
    private final TraineeRepository traineeRepository;

    public SessionController(SessionService sessionService, InstructorRepository instructorRepository, TraineeRepository traineeRepository) {
        this.sessionService = sessionService;
        this.instructorRepository = instructorRepository;
        this.traineeRepository = traineeRepository;
    }

    /**
     * Display list of all sessions
     * URL: GET /admin/sessions
     */
    @GetMapping
    public String listSessions(@RequestParam(required = false) String type,
                               @RequestParam(required = false) String status,
                               Model model) {

        List<Session> sessions;

        // Filter by type and/or status
        if (type != null && !type.isEmpty()) {
            sessions = sessionService.getSessionsByType(type);
        } else if (status != null && !status.isEmpty()) {
            sessions = sessionService.getSessionsByStatus(status);
        } else {
            sessions = sessionService.getAllSessions();
        }

        // Get statistics
        SessionService.SessionStats stats = sessionService.getSessionStatistics();

        model.addAttribute("sessions", sessions);
        model.addAttribute("stats", stats);
        model.addAttribute("currentType", type);
        model.addAttribute("currentStatus", status);

        return "admin/session-list";
    }

    /**
     * Show form to add new session
     * URL: GET /admin/sessions/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Session trainingSession = new Session();
        List<Instructor> instructors = instructorRepository.findAll();
        List<Trainee> trainees = traineeRepository.findAll();

        model.addAttribute("trainingSession", trainingSession);
        model.addAttribute("instructors", instructors);
        model.addAttribute("trainees", trainees);
        model.addAttribute("formMode", "add");

        return "admin/session-form";
    }

    /**
     * Process add session form
     * URL: POST /admin/sessions/add
     */
    @PostMapping("/add")
    public String addSession(@ModelAttribute Session trainingSession,
                             @RequestParam(required = false) List<Integer> traineeIds,
                             RedirectAttributes redirectAttributes) {
        try {
            sessionService.createSession(trainingSession, traineeIds);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Session scheduled successfully: " + trainingSession.getSessionType() + " session");
            return "redirect:/admin/sessions";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sessions/add";
        }
    }

    /**
     * Show form to edit existing session
     * URL: GET /admin/sessions/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Session trainingSession = sessionService.getSessionById(id);

        if (trainingSession == null) {
            return "redirect:/admin/sessions";
        }

        List<Instructor> instructors = instructorRepository.findAll();
        List<Trainee> trainees = traineeRepository.findAll();

        // Get enrolled trainees for theoretical sessions
        List<Integer> enrolledTraineeIds = null;
        if (trainingSession.isTheoretical()) {
            enrolledTraineeIds = sessionService.getTraineesForSession(id);
        }

        model.addAttribute("trainingSession", trainingSession);
        model.addAttribute("instructors", instructors);
        model.addAttribute("trainees", trainees);
        model.addAttribute("enrolledTraineeIds", enrolledTraineeIds);
        model.addAttribute("formMode", "edit");

        return "admin/session-form";
    }

    /**
     * Process edit session form
     * URL: POST /admin/sessions/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String editSession(@PathVariable Integer id,
                              @ModelAttribute Session trainingSession,
                              @RequestParam(required = false) List<Integer> traineeIds,
                              RedirectAttributes redirectAttributes) {
        try {
            trainingSession.setSessionId(id);
            sessionService.updateSession(trainingSession, traineeIds);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Session updated successfully");
            return "redirect:/admin/sessions";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/sessions/edit/" + id;
        }
    }

    /**
     * Delete session
     * URL: GET /admin/sessions/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteSession(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            Session trainingSession = sessionService.getSessionById(id);
            String sessionInfo = trainingSession != null ? trainingSession.getSessionType() + " session" : "Unknown";

            sessionService.deleteSession(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Session deleted successfully: " + sessionInfo);
            } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete session: " + e.getMessage());
            }

        return "redirect:/admin/sessions";
    }

    /**
     * Change session status
     * URL: GET /admin/sessions/{id}/status/{newStatus}
     */
    @GetMapping("/{id}/status/{newStatus}")
    public String changeStatus(@PathVariable Integer id, @PathVariable String newStatus,
                    RedirectAttributes redirectAttributes) {

        try {
            sessionService.changeStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Status changed to: " + newStatus);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }

        return "redirect:/admin/sessions";
    }

    /**
     * Show feedback form
     * URL: GET /admin/sessions/{id}/feedback
     */
    @GetMapping("/{id}/feedback")
    public String showFeedbackForm(@PathVariable Integer id, Model model) {
        Session trainingSession = sessionService.getSessionById(id);

        if (trainingSession == null) {
            return "redirect:/admin/sessions";
        }

        if (!trainingSession.isCompleted()) {
            return "redirect:/admin/sessions";
        }

        model.addAttribute("trainingSession", trainingSession);
        return "admin/session-feedback";
    }

    /**
     * Add feedback to session
     * URL: POST /admin/sessions/{id}/feedback
     */
    @PostMapping("/{id}/feedback")
    public String addFeedback(@PathVariable Integer id,
                              @RequestParam String feedback,
                              RedirectAttributes redirectAttributes) {

        try {
            sessionService.addFeedback(id, feedback);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Feedback added successfully");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }

        return "redirect:/admin/sessions";
    }
}

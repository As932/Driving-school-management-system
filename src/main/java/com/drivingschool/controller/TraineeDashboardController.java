package com.drivingschool.controller;

import com.drivingschool.model.*;
import com.drivingschool.repository.*;
import org.springframework.boot.Banner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trainee dashboard controller
 * Handles trainee-specific views and data
 */

@Controller
@RequestMapping("/trainee")
public class TraineeDashboardController {

    private final TraineeRepository traineeRepository;
    private final SessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final ExamRepository examRepository;
    private final AppUserRepository appUserRepository;

    public TraineeDashboardController(TraineeRepository traineeRepository,
                                      SessionRepository sessionRepository,
                                      PaymentRepository paymentRepository,
                                      ExamRepository examRepository,
                                      AppUserRepository appUserRepository) {
        this.traineeRepository = traineeRepository;
        this.sessionRepository = sessionRepository;
        this.paymentRepository = paymentRepository;
        this.examRepository = examRepository;
        this.appUserRepository = appUserRepository;
    }

    /**
     * Helper method to find trainee by username
     */
    private Trainee findTraineeByUsername(String username) {
        // find app user with specific username
        AppUser appUser = appUserRepository.findByUsername(username);

        if (appUser == null) {
            return null;
        }

        // get the user id
        Integer userId = appUser.getUserId();

        // find trainee with this user id
        List<Trainee> allTrainees = traineeRepository.findAll();

        return allTrainees.stream()
                .filter(t -> t.getUserId() != null && t.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Main trainee dashboard
     * URL: GET /trainee/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // gat logged-in username
        String username = authentication.getName();

        // find trainee by username
        Trainee trainee = findTraineeByUsername(username);
        if (trainee == null) {
            model.addAttribute("errorMessage",
                    "Trainee profile not found");
            return "error";
        }

        // get trainee's sessions
        List<Session> allSessions = sessionRepository.findByTraineeId(trainee.getTraineeId());

        // filter upcoming sessions
        List<Session> upcomingSessions = allSessions.stream()
                .filter(s -> s.isScheduled())
                .filter(s -> s.isUpcoming())
                .sorted((s1, s2) -> s1.getStartDateTime().compareTo(s2.getStartDateTime()))
                .limit(5)
                .toList();

        // filter completed sessions with feedback
        List<Session> sessionsWithFeedback = allSessions.stream()
                .filter(s -> s.isCompleted())
                .filter(s -> s.getInstructorFeedback() != null && !s.getInstructorFeedback().isEmpty())
                .sorted((s1, s2) -> s2.getEndDateTime().compareTo(s1.getEndDateTime()))
                .limit(5)
                .toList();

        // get payments
        List<Payment> payments = paymentRepository.findByTraineeId(trainee.getTraineeId());
        List<Payment> recentPayments = payments.stream()
                .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()))
                .limit(3)
                .toList();

        // get exams
        List<Exam> exams = examRepository.findByTraineeId(trainee.getTraineeId());
        List<Exam> upcomingExams = exams.stream()
                .filter(e -> e.isUpcoming())
                .sorted((e1, e2) -> e1.getScheduledDate().compareTo(e2.getScheduledDate()))
                .toList();

        // calculate stats
        Map<String, Object> stats = new HashMap<>();

        long completedSessionsCount = allSessions.stream()
                        .filter(s -> "Completed".equals(s.getStatus()))
                                .count();

        stats.put("totalSessions", allSessions.size());
        stats.put("completedSessions", (int) completedSessionsCount);
        stats.put("upcomingSessions", upcomingSessions.size());

        // hours - count practical completed sessions
        double hoursCompleted = allSessions.stream()
                .filter(s -> s.isCompleted())
                .filter(s -> s.isPractical())
                .mapToDouble(Session::getDurationHours)
                .sum();
        stats.put("hoursCompleted", hoursCompleted);

        // typical requirement is 30 hours for cat B
        double requiredHours = 30.0;
        double progressPercentage = Math.min((hoursCompleted / requiredHours) * 100, 100);
        stats.put("requiredHours", requiredHours);
        stats.put("progressPercentage", (int) progressPercentage);
        stats.put("hoursRemaining", Math.max(requiredHours-hoursCompleted, 0));

        // Payments
        BigDecimal totalPaidBD = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double totalPaid = totalPaidBD.doubleValue();

        double totalCost = 4000.0; // typical driving school cost
        double balance = totalCost - totalPaid;
        stats.put("totalPaid", totalPaid);
        stats.put("totalCost", totalCost);
        stats.put("balance", Math.max(balance, 0));

        // Exams
        long passedExams = exams.stream()
                .filter(e -> "Passed".equals(e.getStatus()))
                .count();
        stats.put("totalExams", exams.size());
        stats.put("passedExams", (int) passedExams);
        stats.put("upcomingExams", upcomingExams.size());

        // add to model
        model.addAttribute("trainee", trainee);
        model.addAttribute("upcomingSessions", upcomingSessions);
        model.addAttribute("sessionsWithFeedback", sessionsWithFeedback);
        model.addAttribute("recentPayments", recentPayments);
        model.addAttribute("upcomingExams", upcomingExams);
        model.addAttribute("stats", stats);
        model.addAttribute("username", username);

        return "trainee/dashboard";
    }

    /**
     * My sessions - all sessions for this trainee
     * URL: GET /trainee/sessions
     */
    @GetMapping("/sessions")
    public String sessions(Authentication authentication, Model model) {
        String username = authentication.getName();
        Trainee trainee = findTraineeByUsername(username);

        if (trainee == null) {
            model.addAttribute("errorMessage",
                    "Trainee profile not found");
            return "error";
        }

        List<Session> sessions = sessionRepository.findByTraineeId(trainee.getTraineeId());

        model.addAttribute("trainee", trainee);
        model.addAttribute("sessions", sessions);
        model.addAttribute("username", username);

        return "trainee/sessions";
    }

    /**
     * My payments - all payments for this trainee
     * URL: GET /trainee/payments
     */
    @GetMapping("/payments")
    public String payments(Authentication authentication, Model model) {
        String username = authentication.getName();
        Trainee trainee = findTraineeByUsername(username);

        if (trainee == null) {
            model.addAttribute("errorMessage",
                    "Trainee profile not found");
            return "error";
        }

        List<Payment> payments = paymentRepository.findByTraineeId(trainee.getTraineeId());

        // calculate totals
        BigDecimal totalPaidBD = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double totalPaid = totalPaidBD.doubleValue();
        double totalCost = 4000.0;
        double balance = Math.max(totalCost - totalPaid, 0);

        model.addAttribute("trainee", trainee);
        model.addAttribute("payments", payments);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("balance", balance);
        model.addAttribute("username", username);

        return "trainee/payments";
    }

    /**
     * My exams - all exams for this trainee
     * URL: GET /trainee/exams
     */
    @GetMapping("/exams")
    public String exams(Authentication authentication, Model model) {
        String username = authentication.getName();
        Trainee trainee = findTraineeByUsername(username);

        if (trainee == null) {
            model.addAttribute("errorMessage",
                    "Trainee profile not found");
            return "error";
        }

        List<Exam> exams = examRepository.findByTraineeId(trainee.getTraineeId());

        model.addAttribute("trainee", trainee);
        model.addAttribute("exams", exams);
        model.addAttribute("username", username);

        return "trainee/exams";
    }
}

package com.drivingschool.controller;

import com.drivingschool.model.*;
import com.drivingschool.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Dashboard Controller
 * Handles admin overview dashboard
 */

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final TraineeRepository traineeRepository;
    private final InstructorRepository instructorRepository;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;
    private final ExamRepository examRepository;
    private final SessionRepository sessionRepository;

    public AdminDashboardController(TraineeRepository traineeRepository,
                                    InstructorRepository instructorRepository,
                                    CarRepository carRepository,
                                    PaymentRepository paymentRepository,
                                    ExamRepository examRepository,
                                    SessionRepository sessionRepository) {
        this.traineeRepository = traineeRepository;
        this.instructorRepository = instructorRepository;
        this.carRepository = carRepository;
        this.paymentRepository = paymentRepository;
        this.examRepository = examRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Main admin dashboard
     * URL: GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();

        // get all data
        List<Trainee> trainees = traineeRepository.findAll();
        List<Instructor> instructors = instructorRepository.findAll();
        List<Car> cars = carRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        List<Exam> exams = examRepository.findAll();
        List<Session> sessions = sessionRepository.findAll();

        // calculate stats
        Map<String, Object> stats = new HashMap<>();

        // basic counts
        stats.put("totalTrainees", trainees.size());
        stats.put("totalInstructors", instructors.size());
        stats.put("totalCars", cars.size());
        stats.put("totalSessions", sessions.size());
        stats.put("totalExams", exams.size());

        // active counts
        long activeTrainees = trainees.stream()
                .filter(t -> "Active".equals(t.getStatus()))
                .count();
        stats.put("activeTrainees", (int) activeTrainees);

        // session statistics
        long completedSessions = sessions.stream()
                .filter(s -> s.isCompleted())
                .count();
        stats.put("completedSessions", (int) completedSessions);

        long upcomingSessions = sessions.stream()
                .filter(s -> s.isScheduled())
                .filter(s -> s.isUpcoming())
                .count();
        stats.put("upcomingSessions", (int) upcomingSessions);

        // exam stats
        long passedExams = exams.stream()
                .filter(e -> "Passed".equals(e.getStatus()))
                .count();
        stats.put("passedExams", (int) passedExams);

        long upcomingExams = exams.stream()
                .filter(e -> e.isUpcoming())
                .count();
        stats.put("upcomingExams", (int) upcomingExams);

        // revenue stats
        BigDecimal totalRevenue = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue.doubleValue());

        // this month's revenue
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        BigDecimal monthlyRevenue = payments.stream()
                .filter(p -> !p.getPaymentDate().isBefore(ChronoLocalDate.from(startOfMonth.atStartOfDay())))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthlyRevenue", monthlyRevenue.doubleValue());

        // recent enrollments
        List<Trainee> recentTrainees = trainees.stream()
                .sorted((t1, t2) -> t2.getEnrollmentDate().compareTo(t1.getEnrollmentDate()))
                .limit(5)
                .toList();

        // recent payments
        List<Payment> recentPayments = payments.stream()
                .sorted((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()))
                .limit(5)
                .toList();

        // upcoming sessions
        List<Session> upcomingSessionsList = sessions.stream()
                .filter(s -> s.isScheduled())
                .filter(s -> s.isUpcoming())
                .sorted((s1, s2) -> s1.getStartDateTime().compareTo(s2.getStartDateTime()))
                .limit(5)
                .toList();

        // add to model
        model.addAttribute("stats", stats);
        model.addAttribute("recentTrainees", recentTrainees);
        model.addAttribute("recentPayments", recentPayments);
        model.addAttribute("upcomingSessions", upcomingSessionsList);
        model.addAttribute("username", username);

        return "admin/dashboard";
    }
}

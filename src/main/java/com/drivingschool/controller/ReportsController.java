package com.drivingschool.controller;

import com.drivingschool.repository.ReportsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Reports Controller
 * Handles complex analytics and reporting with subqueries
 */

@Controller
@RequestMapping("/admin/reports")
public class ReportsController {

    private final ReportsRepository reportsRepository;

    public ReportsController(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    /**
     * Main reports page
     * URL: GET /admin/reports
     */
    @GetMapping
    public String reports(Model model) {
        return "/admin/reports";
    }

    /**
     * Report 1: Trainees with above-average completed sessions
     * URL: GET /admin/reports/above-average-sessions
     */
    @GetMapping("/above-average-sessions")
    public String aboveAverageSessions(Model model) {
        List<Map<String, Object>> results = reportsRepository.findTraineesWithAboveAverageHours();

        model.addAttribute("results", results);
        model.addAttribute("reportTitle", "Trainees with Above-Average Sessions");
        model.addAttribute("reportType", "above-average-sessions");

        return "admin/reports-result";
    }

    /**
     * Report 2: Top instructors by student pass rate
     * URL: GET /admin/reports/top-instructors
     */
    @GetMapping("/top-instructors")
    public String topInstructors(Model model) {
        List<Map<String, Object>> results = reportsRepository.findTopInstructorsByPassRate();

        model.addAttribute("results", results);
        model.addAttribute("reportTitle", "Top Instructors by Student Pass Rate");
        model.addAttribute("reportType", "top-instructors");

        return "admin/reports-result";
    }

    /**
     * Report 3: Most active instructors (by session count)
     * URL: GET /admin/reports/most-active-instructors
     */
    @GetMapping("/most-active-instructors")
    public String mostActiveInstructors(Model model) {
        List<Map<String, Object>> results = reportsRepository.findMostUtilizedCars();

        model.addAttribute("results", results);
        model.addAttribute("reportTitle", "Most Active Instructors (by Sessions)");
        model.addAttribute("reportType", "most-active-instructors");

        return "admin/reports-result";
    }

    /**
     * Report 4: Trainees behind schedule
     * URL: GET /admin/reports/behind-schedule
     */
    @GetMapping("/behind-schedule")
    public String behindSchedule(Model model) {
        List<Map<String, Object>> results = reportsRepository.findTraineesBehindSchedule();

        model.addAttribute("results", results);
        model.addAttribute("reportTitle", "Trainees Behind Schedule");
        model.addAttribute("reportType", "behind-schedule");

        return "admin/reports-result";
    }
}
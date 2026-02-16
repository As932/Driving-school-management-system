package com.drivingschool.controller;

import com.drivingschool.model.Exam;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.TraineeRepository;
import com.drivingschool.service.ExamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Exam Controller - handles HTTP requests for exam management
 */

@Controller
@RequestMapping("/admin/exams")
public class ExamController {
    private final ExamService examService;
    private final TraineeRepository traineeRepository;

    public ExamController(ExamService examService, TraineeRepository traineeRepository) {
        this.examService = examService;
        this.traineeRepository = traineeRepository;
    }

    /**
     * Display list of all exams
     * URL: GET /admin/exams
     */
    @GetMapping
    public String listExams(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            Model model) {

        List<Exam> exams;

        // Filter by type and/or status
        if (type != null && !type.isEmpty()) {
            exams = examService.getExamsByType(type);
        } else if (status != null && !status.isEmpty()) {
            exams = examService.getExamsByStatus(status);
        } else {
            exams = examService.getAllExams();
        }

        // Get statistics
        ExamService.ExamStats stats = examService.getExamStatistics();

        model.addAttribute("exams", exams);
        model.addAttribute("stats", stats);
        model.addAttribute("currentType", type);
        model.addAttribute("currentStatus", status);

        return "admin/exam-list";
    }

    /**
     * Show form to add new exam
     * URL: GET /admin/exams/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Exam exam = new Exam();
        List<Trainee> trainees = traineeRepository.findAll();

        model.addAttribute("exam", exam);
        model.addAttribute("trainees", trainees);
        model.addAttribute("formMode", "add");

        return "admin/exam-form";
    }

    /**
     * Process add exam form
     * URL: POST /admin/exams/add
     */
    @PostMapping("/add")
    public String addExam(
            @ModelAttribute Exam exam,
            RedirectAttributes redirectAttributes) {

        try {
            examService.createExam(exam);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Exam scheduled successfully: " + exam.getExamType() + " exam");
            return "redirect:/admin/exams";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/exams/add";
        }
    }

    /**
     * Show form to edit existing exam
     * URL: GET /admin/exams/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Exam exam = examService.getExamById(id);

        if (exam == null) {
            return "redirect:/admin/exams";
        }

        List<Trainee> trainees = traineeRepository.findAll();

        model.addAttribute("exam", exam);
        model.addAttribute("trainees", trainees);
        model.addAttribute("formMode", "edit");

        return "admin/exam-form";
    }

    /**
     * Process edit exam form
     * URL: POST /admin/exams/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String editExam(
            @PathVariable Integer id,
            @ModelAttribute Exam exam,
            RedirectAttributes redirectAttributes) {

        try {
            exam.setExamId(id);
            examService.updateExam(exam);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Exam updated successfully");
            return "redirect:/admin/exams";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/exams/edit/" + id;
        }
    }

    /**
     * Delete exam
     * URL: GET /admin/exams/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteExam(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Exam exam = examService.getExamById(id);
            String examInfo = exam != null ? exam.getExamType() + " exam" : "Unknown";

            examService.deleteExam(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Exam deleted successfully: " + examInfo);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete exam: " + e.getMessage());
        }

        return "redirect:/admin/exams";
    }

    /**
     * Change exam status
     * URL: /admin/exams/{id}/status/{newStatus}
     */
    @GetMapping("/{id}/status/{newStatus}")
    public String changeStatus(
            @PathVariable Integer id,
            @PathVariable String newStatus,
            RedirectAttributes redirectAttributes) {

        try {
            examService.changeStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Status changed to: " + newStatus);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/exams";
    }
}

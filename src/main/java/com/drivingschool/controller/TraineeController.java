package com.drivingschool.controller;

import com.drivingschool.model.Instructor;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.InstructorRepository;
import com.drivingschool.service.TraineeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Trainee Controller - handles HTTP requests for trainee management
 * URL Mapping:
 *  - GET /admin/trainees               -> List all trainees
 *  - GET /admin/trainees/add           -> Show add form
 *  - POST /admin/trainees/add          -> Process add form
 *  - GET /admin/trainees/edit/{id}     -> Show edit form
 *  - POST /admin/trainees/edit/{id}    -> Process edit form
 *  - GET /admin/trainees/delete/{id}   -> Delete trainee
 */

@Controller
@RequestMapping("/admin/trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final InstructorRepository instructorRepository;

    public TraineeController(TraineeService traineeService, InstructorRepository instructorRepository) {
        this.traineeService = traineeService;
        this.instructorRepository = instructorRepository;
    }

    /**
     * Display list of all trainees
     * URL: GET /admin/trainees
     */
    @GetMapping
    public String listTrainees(@RequestParam(required = false) String status, Model model) {
        List<Trainee> trainees;

        if (status != null && !status.isEmpty()){
            if (status.equals("Active")) {
                trainees = traineeService.getActiveTrainees();
            } else if (status.equals("Completed")) {
                trainees = traineeService.getCompletedTrainees();
            } else {
                trainees = traineeService.getAllTrainees();
            }
        } else {
            trainees = traineeService.getAllTrainees();
        }

        Integer activeCount = traineeService.getCountByStatus("Active");
        Integer completedCount = traineeService.getCountByStatus("Completed");

        model.addAttribute("trainees", trainees);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("currentStatus", status);

        return "admin/trainee-list";
    }

    /**
     * Show form to add new trainee
     * URL: GET /admin/trainees/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Trainee trainee = new Trainee();
        List<Instructor> instructors = instructorRepository.findAll();

        model.addAttribute("trainee", trainee);
        model.addAttribute("instructors", instructors);
        model.addAttribute("formMode", "add");

        return "admin/trainee-form";
    }

    /**
     * Process add trainee form
     * URL: POST /admin/trainees/add
     */
    @PostMapping("/add")
    public String addTrainee(@ModelAttribute Trainee trainee, @RequestParam String username,
                             @RequestParam String password, @RequestParam String email,
                             RedirectAttributes redirectAttributes) {
        try {
            traineeService.createTrainee(trainee, username, password, email);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Trainee added successfully: " + trainee.getFirstName() + " " + trainee.getLastName());

            return "redirect:/admin/trainees";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/admin/trainees/add";
        }
    }

    /**
     * Show form to edit existing trainee
     * URL: GET /admin/trainees/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Trainee trainee = traineeService.getTraineeById(id);

        if (trainee == null) {
            return "redirect:/admin/trainees";
        }

        List<Instructor> instructors = instructorRepository.findAll();

        model.addAttribute("trainee", trainee);
        model.addAttribute("instructors", instructors);
        model.addAttribute("formMode", "edit");

        return "admin/trainee-form";
    }

    /**
     * Process edit trainee form
     * URL: POST /admin/trainees/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String editTrainee(@PathVariable Integer id, @ModelAttribute Trainee trainee,
                              RedirectAttributes redirectAttributes) {
        try {
            trainee.setTraineeId(id);
            traineeService.updateTrainee(trainee);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Trainee updated successfully: " + trainee.getFirstName() + " " + trainee.getLastName());

            return "redirect:/admin/trainees";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/admin/trainees/edit/" + id;
        }
    }

    /**
     * Delete trainee
     * URL: GET /admin/trainees/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteTrainee(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Trainee trainee = traineeService.getTraineeById(id);
            String traineeName = trainee != null ? (trainee.getFirstName() + " " + trainee.getLastName()) : "Unknown";

            traineeService.deleteTrainee(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Trainee deleted successfully: " + traineeName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/trainees";
    }

    /**
     * Change trainee status
     * URL: GET /admin/trainees/{id}/status/{newStatus}
     */
    @GetMapping("/{id}/status/{newStatus}")
    public String changeStatus(@PathVariable Integer id, @PathVariable String newStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            traineeService.changeStatus(id, newStatus);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Status changed to " + newStatus);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/trainees";
    }
}

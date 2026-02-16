package com.drivingschool.controller;

import com.drivingschool.model.Instructor;
import com.drivingschool.service.InstructorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * InstructorController - Handles HTTP requests for instructor management
 * URL Mapping:
 *  - GET /admin/instructors                -> List all instructors
 *  - GET /admin/instructors/add            -> Show add form
 *  - POST /admin/instructors/add           -> Process add form
 *  - GET /admin/instructors/edit/{id}      -> Show edit form
 *  - POST /admin/instructors/edit/{id}     -> Process edit form
 *  - GET /admin/instructors/delete/{id}    -> Delete instructor
 */

@Controller
@RequestMapping("/admin/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    /**
     * Display list of all instructors
     * URL: GET /admin/instructors
     */
    @GetMapping()
    public String listInstructors(Model model) {
        List<Instructor> instructors = instructorService.getAllInstructors();
        Integer totalCount = instructorService.getTotalCount();

        model.addAttribute("instructors", instructors);
        model.addAttribute("totalCount", totalCount);

        return "admin/instructor-list";
    }

    /**
     * Show form to add new instructor
     * URL: GET /admin/instructors/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Instructor instructor = new Instructor();

        model.addAttribute("instructor", instructor);
        model.addAttribute("formMode", "add");

        return "admin/instructor-form";
    }

    /**
     * Process add instructor form
     * URL: POST /admin/instructors/add
     */
    @PostMapping("/add")
    public String addInstructor(@ModelAttribute Instructor instructor,
                                @RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        try {
            instructorService.createInstructor(instructor, username, password, email);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Instructor added successfully: " + instructor.getFirstName() + " " + instructor.getLastName());
            return "redirect:/admin/instructors";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/instructors/add";
        }
    }

    /**
     * Show form to edit existing instructor
     * URL: GET /admin/instructors/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Instructor instructor = instructorService.getInstructorById(id);

        if (instructor == null) {
            return "redirect:/admin/instructors";
        }

        model.addAttribute("instructor", instructor);
        model.addAttribute("formMode", "edit");

        return "admin/instructor-form";
    }

    /**
     * Process edit instructor form
     * URL: /admin/instructors/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String editInstructor(@PathVariable Integer id,
                                 @ModelAttribute Instructor instructor,
                                 RedirectAttributes redirectAttributes) {
        try {
            instructor.setInstructorId(id);
            instructorService.updateInstructor(instructor);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Instructor updated successfully: " + instructor.getFirstName() + " " + instructor.getLastName());
            return "redirect:/admin/instructors";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/instructors/edit/" + id;
        }
    }

    /**
     * Delete instructor
     * URL: /admin/instructors/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteInstructor(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Instructor instructor = instructorService.getInstructorById(id);
            String instructorName = instructor != null ? (instructor.getFirstName() + " " + instructor.getLastName()) : "Unknown";

            instructorService.deleteInstructor(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Instructor deleted successfully: " + instructorName);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete instructor: " + e.getMessage());
        }

        return "redirect:/admin/instructors";
    }
}

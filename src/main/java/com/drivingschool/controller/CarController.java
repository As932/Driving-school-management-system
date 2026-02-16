package com.drivingschool.controller;

import com.drivingschool.model.Car;
import com.drivingschool.model.Instructor;
import com.drivingschool.repository.InstructorRepository;
import com.drivingschool.service.CarService;
import com.drivingschool.service.InstructorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Car Controller - Handles HTTP requests for car management
 * URL Mapping:
 *  - GET /admin/cars               -> List all cars
 *  - GET /admin/cars/add           -> Show add form
 *  - POST /admin/cars/add          -> Process add form
 *  - GET /admin/cars/edit/{id}     -> Show edit form
 *  - POST /admin/cars/edit/{id}    -> Process edit form
 *  - GET /admin/cars/delete/{id}   -> Delete car
 */

@Controller
@RequestMapping("/admin/cars")
public class CarController {

    private final CarService carService;
    private final InstructorRepository instructorRepository;

    public CarController(CarService carService, InstructorRepository instructorRepository) {
        this.carService = carService;
        this.instructorRepository = instructorRepository;
    }

    /**
     * Display list of all cars
     * URL: GET /admin/cars
     */
    @GetMapping
    public String listCars(Model model) {
        List<Car> cars = carService.getAllCars();
        Integer totalCount = carService.getTotalCount();

        model.addAttribute("cars", cars);
        model.addAttribute("totalCount", totalCount);

        return "admin/car-list";
    }

    /**
     * Show form to add new car
     * URL: GET /admin/cars/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Car car = new Car();
        List<Instructor> instructors = instructorRepository.findAll();

        model.addAttribute("car", car);
        model.addAttribute("instructors", instructors);
        model.addAttribute("formMode", "add");

        return "admin/car-form";
    }

    /**
     * Process add car form
     * URL: POST /admin/cars/add
     */
    @PostMapping("/add")
    public String addCar(@ModelAttribute Car car,
                         RedirectAttributes redirectAttributes) {
        try {
            carService.createCar(car);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Car added successfully: " + car.getFullDescription());


            return "redirect:/admin/cars";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/cars/add";
        }
    }

    /**
     * Show form to edit existing car
     * URL: GET /admin/cars/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Car car = carService.getCarById(id);

        if (car == null) {
            return "redirect:/admin/cars";
        }

        List<Instructor> instructors = instructorRepository.findAll();

        model.addAttribute("car", car);
        model.addAttribute("instructors", instructors);
        model.addAttribute("formMode", "edit");

        return "admin/car-form";
    }

    /**
     * Process edit car form
     * URL: POST /admin/cars/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String editCar(@PathVariable Integer id,
                          @ModelAttribute Car car,
                          RedirectAttributes redirectAttributes) {

        try {
            car.setCarId(id);
            carService.updateCar(car);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Car updated successfully: " + car.getFullDescription());
            return "redirect:/admin/cars";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/cars/edit/" + id;
        }
    }

    /**
     * Delete car
     * URL: GET /admin/cars/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteCar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Car car = carService.getCarById(id);
            String carDescription = car != null ? car.getFullDescription() : "unknown";

            carService.deleteCar(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Car deleted successfully: " + carDescription);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete car: " + e.getMessage());
        }

        return "redirect:/admin/cars";
    }
}

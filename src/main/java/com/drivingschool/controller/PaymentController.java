package com.drivingschool.controller;

import com.drivingschool.model.Payment;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.TraineeRepository;
import com.drivingschool.service.PaymentService;
import com.drivingschool.service.TraineeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Payment Controller - handles HTTP requests for payments management
 */

@Controller
@RequestMapping("/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final TraineeRepository traineeRepository;

    public PaymentController(PaymentService paymentService, TraineeRepository traineeRepository) {
        this.paymentService = paymentService;
        this.traineeRepository = traineeRepository;
    }

    /**
     * Display list of all payments
     * URL: GET /admin/payments
     */
    @GetMapping
    public String listPayments(@RequestParam(required = false) String method,
                               Model model) {
        List<Payment> payments;

        if (method != null && !method.isEmpty()) {
            payments = paymentService.getPaymentsByMethod(method);
        } else {
            payments = paymentService.getAllPayments();
        }

        // get statistics
        PaymentService.PaymentStats stats = paymentService.getPaymentStatistics();

        model.addAttribute("payments", payments);
        model.addAttribute("stats", stats);
        model.addAttribute("currentMethod", method);

        return "admin/payment-list";
    }

    /**
     * Show form to add new payment
     * URL: GET /admin/payments/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Payment payment = new Payment();
        List<Trainee> trainees = traineeRepository.findAll();

        model.addAttribute("payment", payment);
        model.addAttribute("trainees", trainees);
        model.addAttribute("formMode", "add");

        return "admin/payment-form";
    }

    /**
     * Process add payment form
     * URL: POST /admin/payments/add
     */
    @PostMapping("/add")
    public String addPayment(@ModelAttribute Payment payment,
                             RedirectAttributes redirectAttributes) {
        try {
            paymentService.createPayment(payment);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment recorder successfully: " + payment.getAmount() + " RON");
            return "redirect:/admin/payments";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/payments/add";
        }
    }

    /**
     * Show form to edit existing payment
     * URL: GET /admin/payments/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Payment payment = paymentService.getPaymentById(id);

        if (payment == null) {
            return "redirect: /admin/payments";
        }

        List<Trainee> trainees = traineeRepository.findAll();

        model.addAttribute("payment", payment);
        model.addAttribute("trainees", trainees);
        model.addAttribute("formMode", "edit");

        return "admin/payment-form";
    }

    /**
     * Process edit payment form
     * URL: POST /admin/payments/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String editPayment(@PathVariable Integer id,
                              @ModelAttribute Payment payment,
                              RedirectAttributes redirectAttributes) {
        try {
            payment.setPaymentId(id);
            paymentService.updatePayment(payment);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment updated successfully");
            return "redirect:/admin/payments";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/payments/edit/" + id;
        }
    }

    /**
     * Delete payment
     * URL: GET /admin/payments/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            String paymentInfo = payment != null ? payment.getAmount() + " RON" : "Unknown";

            paymentService.deletePayment(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment deleted successfully: " + paymentInfo);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete payment: " + e.getMessage());
        }

        return "redirect:/admin/payments";
    }
}
package com.drivingschool.service;

import com.drivingschool.model.Payment;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.PaymentRepository;
import com.drivingschool.repository.TraineeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Payment Service - Business Logic Layer
 */

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TraineeRepository traineeRepository;

    public PaymentService(PaymentRepository paymentRepository, TraineeRepository traineeRepository) {
        this.paymentRepository = paymentRepository;
        this.traineeRepository = traineeRepository;
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payment by id
    public Payment getPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId);
    }

    // Get all payments for a trainee
    public List<Payment> getPaymentsByTrainee(Integer traineeId) {
        return paymentRepository.findByTraineeId(traineeId);
    }

    // Get payments by method
    public List<Payment> getPaymentsByMethod(String paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }

    // Create new payment
    @Transactional
    public Integer createPayment(Payment payment) {
        Trainee trainee = traineeRepository.findById(payment.getTraineeId());

        // validate trainee exists
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee not found: " + payment.getTraineeId());
        }

        // validate amount is positive
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        // set payment date to today if not provided
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }

        return paymentRepository.save(payment);
    }

    // Update existing payment
    @Transactional
    public void updatePayment(Payment payment) {
        // validate payment exists
        Payment existing = paymentRepository.findById(payment.getPaymentId());
        if (existing == null) {
            throw new IllegalArgumentException("Payment not found: " + payment.getPaymentId());
        }

        // validate trainee exists
        Trainee trainee = traineeRepository.findById(payment.getTraineeId());
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee not found: " + payment.getTraineeId());
        }

        paymentRepository.update(payment);
    }

    // Delete payment
    @Transactional
    public void deletePayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId);

        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }

        paymentRepository.delete(paymentId);
    }

    // Get total count
    public Integer getTotalCount() {
        return paymentRepository.count();
    }

    // Get total amount paid by a trainee
    public BigDecimal getTotalPaidByTrainee(Integer traineeId) {
        return paymentRepository.getTotalAmountByTrainee(traineeId);
    }

    // Get total revenue (all payments)
    public BigDecimal getTotalRevenue() {
        return paymentRepository.getTotalRevenue();
    }

    // Get revenue by payment method
    public BigDecimal getRevenueByMethod(String paymentMethod) {
        return paymentRepository.getRevenueByMethod(paymentMethod);
    }

    // Get payment statistics
    public PaymentStats getPaymentStatistics() {
        BigDecimal totalRevenue = getTotalRevenue();
        BigDecimal cashRevenue = getRevenueByMethod("Cash");
        BigDecimal cardRevenue = getRevenueByMethod("Card");
        Integer totalCount = getTotalCount();

        return new PaymentStats(totalRevenue, cashRevenue, cardRevenue, totalCount);
    }

    // Inner class for payment stats
    @Getter
    @AllArgsConstructor
    public static class PaymentStats {
        private final BigDecimal totalRevenue;
        private final BigDecimal cashRevenue;
        private final BigDecimal cardRevenue;
        private final Integer totalPayments;
    }
}

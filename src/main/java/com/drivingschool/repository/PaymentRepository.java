package com.drivingschool.repository;

import com.drivingschool.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Payment Repository - Data Access Layer using raw SQL
 */

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();

        payment.setPaymentId(rs.getInt("PaymentID"));
        payment.setAmount(rs.getBigDecimal("Amount"));
        payment.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
        payment.setPaymentMethod(rs.getString("PaymentMethod"));
        payment.setDetails(rs.getString("Details"));
        payment.setTraineeId(rs.getInt("TraineeID"));

        try {
            payment.setTraineeName(rs.getString("TraineeName"));
        } catch (Exception e) {

        }

        return payment;
    };

    // Find all payments with trainee information
    public List<Payment> findAll() {
        String sql = """
                SELECT
                    p.PaymentID, p.Amount, p.PaymentDate, p.PaymentMethod,
                    p.Details, p.TraineeID,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
                FROM Payment p
                LEFT JOIN Trainee t ON p.TraineeID = t.TraineeID
                ORDER BY p.PaymentDate DESC
                """;

        return jdbcTemplate.query(sql, paymentRowMapper);
    }

    // Find payment by id
    public Payment findById(Integer paymentId) {
        String sql = """
                SELECT
                    p.PaymentID, p.Amount, p.PaymentDate, p.PaymentMethod,
                    p.Details, p.TraineeID,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
                FROM Payment p
                LEFT JOIN Trainee t ON p.TraineeID = t.TraineeID
                WHERE p.PaymentID = ?
                """;

        List<Payment> payments = jdbcTemplate.query(sql, paymentRowMapper, paymentId);
        return payments.isEmpty() ? null : payments.getFirst();
    }

    // Find all payments for a specific trainee
    public List<Payment> findByTraineeId(Integer traineeId) {
        String sql = """
                SELECT
                    p.PaymentID, p.Amount, p.PaymentDate, p.PaymentMethod,
                    p.Details, p.TraineeID
                FROM Payment p
                WHERE p.TraineeID = ?
                ORDER BY p.PaymentDate DESC
                """;

        return jdbcTemplate.query(sql, paymentRowMapper, traineeId);
    }

    // Find payments by payment method
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        String sql = """
                SELECT
                    p.PaymentID, p.Amount, p.PaymentDate, p.PaymentMethod,
                    p.Details, p.TraineeID,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
                FROM Payment p
                LEFT JOIN Trainee t ON p.TraineeID = t.TraineeID
                WHERE p.PaymentMethod = ?
                ORDER BY p.PaymentDate DESC
                """;

        return jdbcTemplate.query(sql, paymentRowMapper, paymentMethod);
    }

    // Save new payment (INSERT)
    public Integer save(Payment payment) {
        String sql = """
                INSERT INTO Payment (Amount, PaymentDate, PaymentMethod,
                    Details, TraineeID) VALUES (?, ?, ?, ?, ?);
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setBigDecimal(1, payment.getAmount());
            ps.setDate(2, Date.valueOf(payment.getPaymentDate()));
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getDetails());
            ps.setInt(5, payment.getTraineeId());

            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("PAYMENTID")) {
            return ((Number) keys.get("PAYMENTID")).intValue();
        }

        throw new IllegalArgumentException("Failed to retrieve generated payment ID");
    }

    // Update existing payment
    public void update(Payment payment) {
        String sql = """
                UPDATE Payment SET
                    Amount = ?,
                    PaymentDate = ?,
                    PaymentMethod = ?,
                    Details = ?,
                    TraineeID = ?
                WHERE PaymentID = ?
                """;

        jdbcTemplate.update(sql,
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getPaymentMethod(),
                payment.getDetails(),
                payment.getTraineeId(),
                payment.getPaymentId());
    }

    // Delete payment by id
    public void delete(Integer paymentId) {
        String sql = "DELETE FROM Payment WHERE PaymentID = ?";
        jdbcTemplate.update(sql, paymentId);
    }

    // Count total payments
    public Integer count() {
        String sql = "SELECT COUNT(*) FROM Payment";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Calculate total amount paid by a trainee
    public BigDecimal getTotalAmountByTrainee(Integer traineeId) {
        String sql = "SELECT COALESCE(SUM(Amount), 0) FROM Payment WHERE TraineeID = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, traineeId);
    }

    // Calculate total revenue (all payments)
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(Amount), 0) FROM Payment";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    // calculate revenue by payment method
    public BigDecimal getRevenueByMethod(String paymentMethod) {
        String sql = "SELECT COALESCE(SUM(Amount), 0) FROM Payment WHERE PaymentMethod = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, paymentMethod);
    }
}
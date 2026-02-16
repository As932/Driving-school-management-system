package com.drivingschool.repository;

import com.drivingschool.model.Exam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Exam Repository - Data Access Layer using raw SQL
 */
@Repository
public class ExamRepository {
    private final JdbcTemplate jdbcTemplate;

    public ExamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Exam> examRowMapper = (rs, rowNum) -> {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("ExamID"));
        exam.setExamType(rs.getString("ExamType"));
        exam.setScheduledDate(rs.getDate("ScheduledDate").toLocalDate());
        exam.setStatus(rs.getString("Status"));
        exam.setTraineeId(rs.getInt("TraineeID"));

        // trainee name might not exist in all queries
        try {
            exam.setTraineeName(rs.getString("TraineeName"));
        } catch (Exception e) {
            // ignore if column doesn't exist
        }

        return exam;
    };

    // Find all exams with trainee information
    public List<Exam> findAll() {
        String sql = """
            SELECT
                e.ExamID, e.ExamType, e.ScheduledDate, e.Status, e.TraineeID,
                CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
            FROM Exam e
            LEFT JOIN Trainee t ON e.TraineeID = t.TraineeID
            ORDER BY e.ScheduledDate DESC
            """;

        return jdbcTemplate.query(sql, examRowMapper);
    }

    // Find exam by id
    public Exam findById(Integer examId) {
        String sql = """
            SELECT
                e.ExamID, e.ExamType, e.ScheduledDate, e.Status, e.TraineeID,
                CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
            FROM Exam e
            LEFT JOIN Trainee t ON e.TraineeID = t.TraineeID
            WHERE e.ExamID = ?
            """;

        List<Exam> exams = jdbcTemplate.query(sql, examRowMapper, examId);
        return exams.isEmpty() ? null : exams.getFirst();
    }

    // Find all exams for a specific trainee
    public List<Exam> findByTraineeId(Integer traineeId) {
        String sql = """
            SELECT
                e.ExamID, e.ExamType, e.ScheduledDate, e.Status, e.TraineeID
            FROM Exam e
            WHERE e.TraineeID = ?
            ORDER BY e.ScheduledDate DESC
            """;

        return jdbcTemplate.query(sql, examRowMapper, traineeId);
    }

    // Find exams by type
    public List<Exam> findByExamType(String examType) {
        String sql = """
            SELECT
                e.ExamID, e.ExamType, e.ScheduledDate, e.Status, e.TraineeID,
                CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
            FROM Exam e
            LEFT JOIN Trainee t ON e.TraineeID = t.TraineeID
            WHERE e.ExamType = ?
            ORDER BY e.ScheduledDate DESC
            """;

        return jdbcTemplate.query(sql, examRowMapper, examType);
    }

    // Find exams by status
    public List<Exam> findByStatus(String status) {
        String sql = """
            SELECT
                e.ExamID, e.ExamType, e.ScheduledDate, e.Status, e.TraineeID,
                CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName
            FROM Exam e
            LEFT JOIN Trainee t ON e.TraineeID = t.TraineeID
            WHERE e.Status = ?
            ORDER BY e.ScheduledDate DESC
            """;

        return jdbcTemplate.query(sql, examRowMapper, status);
    }

    // Save new exam (INSERT)
    public Integer save(Exam exam) {
        String sql = """
            INSERT INTO Exam (ExamType, ScheduledDate, Status, TraineeID)
            VALUES (?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, exam.getExamType());
            ps.setDate(2, java.sql.Date.valueOf(exam.getScheduledDate()));
            ps.setString(3, exam.getStatus());
            ps.setInt(4, exam.getTraineeId());
            return ps;
        }, keyHolder);


        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("EXAMID")) {
            return ((Number) keys.get("EXAMID")).intValue();
        }

        throw new IllegalStateException("Failed to retrieve generated exam ID");
    }

    // Update existing exam
    public void update(Exam exam) {
        String sql = """
            UPDATE Exam SET
                ExamType = ?,
                ScheduledDate = ?,
                Status = ?,
                TraineeID = ?
            WHERE ExamID = ?
            """;

        jdbcTemplate.update(sql,
                exam.getExamType(),
                exam.getScheduledDate(),
                exam.getStatus(),
                exam.getTraineeId(),
                exam.getExamId()
        );
    }

    // Delete exam by id
    public void delete(Integer examId) {
        String sql = "DELETE FROM Exam WHERE ExamID = ?";
        jdbcTemplate.update(sql, examId);
    }

    // Count total exams
    public Integer count() {
        String sql = "SELECT COUNT(*) FROM Exam";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Count exams by status
    public Integer countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Exam WHERE Status = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    // Count exams by type
    public Integer countByType(String examType) {
        String sql = "SELECT COUNT(*) FROM Exam WHERE ExamType = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, examType);
    }
}

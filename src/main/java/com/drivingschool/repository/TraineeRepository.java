package com.drivingschool.repository;

import com.drivingschool.model.Trainee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class TraineeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TraineeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper to convert database rows to Trainee objects
    private final RowMapper<Trainee> traineeRowMapper = (rs, rowNum) -> {
        Trainee trainee = new Trainee();

        trainee.setTraineeId(rs.getInt("TraineeID"));
        trainee.setUserId(rs.getInt("UserID"));
        trainee.setFirstName(rs.getString("FirstName"));
        trainee.setLastName(rs.getString("LastName"));
        trainee.setSsn(rs.getString("SSN"));
        trainee.setAddress(rs.getString("Address"));
        trainee.setPhone(rs.getString("Phone"));
        trainee.setEnrollmentDate(rs.getDate("EnrollmentDate").toLocalDate());
        trainee.setLicenseCategory(rs.getString("LicenseCategory"));
        trainee.setStatus(rs.getString("Status"));
        trainee.setAssignedInstructorId(rs.getInt("AssignedInstructorID"));

        // these fields might be null in simple queries
        try {
            trainee.setUsername(rs.getString("Username"));
            trainee.setEmail(rs.getString("Email"));
            trainee.setInstructorName(rs.getString("InstructorName"));
        } catch (Exception e) {
            // ignore if columns do not exist in the query
        }

        return trainee;
    };

    // Find all trainees with their user and instructor information
    public List<Trainee> findAll() {
        String sql = """
                SELECT
                    t.TraineeID, t.UserID, t.FirstName, t.LastName, t.SSN, t.Address,
                    t.Phone, t.EnrollmentDate, t.LicenseCategory, t.Status, t.AssignedInstructorID,
                    u.Username, u.Email, CONCAT(i.FirstName, ' ', i.LastName) as InstructorName
                FROM Trainee t
                LEFT JOIN AppUser u ON t.UserID = u.UserID
                LEFT JOIN Instructor i ON t.AssignedInstructorID = i.InstructorID
                ORDER BY t.EnrollmentDate DESC
                """;

        return jdbcTemplate.query(sql, traineeRowMapper);
    }

    // Find trainee by id
    public Trainee findById(Integer traineeId) {
        String sql = """
                SELECT
                    t.TraineeID, t.UserID, t.FirstName, t.LastName, t.SSN, t.Address,
                    t.Phone, t.EnrollmentDate, t.LicenseCategory, t.Status, t.AssignedInstructorID,
                    u.Username, u.Email, CONCAT(i.FirstName, ' ', i.LastName) as InstructorName
                FROM Trainee t
                LEFT JOIN AppUser u ON t.UserID = u.UserID
                LEFT JOIN Instructor i ON t.AssignedInstructorID = i.InstructorID
                WHERE t.traineeID = ?
                """;

        List<Trainee> trainees = jdbcTemplate.query(sql, traineeRowMapper, traineeId);
        return trainees.isEmpty() ? null : trainees.getFirst();
    }

    // Find trainees by status (Active, Completed)
    public List<Trainee> findByStatus(String status) {
        String sql = """
                SELECT
                    t.TraineeID, t.UserID, t.FirstName, t.LastName, t.SSN, t.Address,
                    t.Phone, t.EnrollmentDate, t.LicenseCategory, t.Status, t.AssignedInstructorID,
                    u.Username, u.Email, CONCAT(i.FirstName, ' ', i.LastName) as InstructorName
                FROM Trainee t
                LEFT JOIN AppUser u ON t.UserID = u.UserID
                LEFT JOIN Instructor i ON t.AssignedInstructorID = i.InstructorID
                WHERE t.Status = ?
                ORDER BY t.EnrollmentDate DESC
                """;

        return jdbcTemplate.query(sql, traineeRowMapper, status);
    }

    // Find trainees assigned to a specific instructor
    public List<Trainee> findByInstructorId(Integer instructorId) {
        String sql = """
                SELECT
                    t.TraineeID, t.UserID, t.FirstName, t.LastName, t.SSN, t.Address,
                    t.Phone, t.EnrollmentDate, t.LicenseCategory, t.Status, t.AssignedInstructorID,
                    u.Username, u.Email
                FROM Trainee t
                LEFT JOIN AppUser u ON t.UserID = u.UserID
                WHERE t.AssignedInstructorID = ?
                ORDER BY t.EnrollmentDate DESC
                """;

        return jdbcTemplate.query(sql, traineeRowMapper, instructorId);
    }

    // Save new trainee (INSERT)
    // Return the generated traineeID
    public Integer save(Trainee trainee){
        String sql = """
                INSERT INTO Trainee (UserID, FirstName, LastName, SSN, Address, Phone,
                                    EnrollmentDate, LicenseCategory, Status, AssignedInstructorID)
                                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, trainee.getUserId());
            ps.setString(2, trainee.getFirstName());
            ps.setString(3, trainee.getLastName());
            ps.setString(4, trainee.getSsn());
            ps.setString(5, trainee.getAddress());
            ps.setString(6, trainee.getPhone());
            ps.setDate(7, java.sql.Date.valueOf(trainee.getEnrollmentDate()));
            ps.setString(8, trainee.getLicenseCategory());
            ps.setString(9, trainee.getStatus());
            ps.setInt(10, trainee.getAssignedInstructorId());

            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("TRAINEEID")) {
            return ((Number) keys.get("TRAINEEID")).intValue();
        }

        throw new IllegalArgumentException("Failed to retrieve generated trainee ID");
    }

    // Update existing trainee
    public void update(Trainee trainee) {
        String sql = """
                UPDATE Trainee SET
                    FirstName = ?,
                    LastName = ?,
                    SSN = ?,
                    Address = ?,
                    Phone = ?,
                    EnrollmentDate = ?,
                    LicenseCategory = ?,
                    Status = ?,
                    AssignedInstructorID = ?
                WHERE TraineeID = ?
                """;

        jdbcTemplate.update(sql,
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getSsn(),
                trainee.getAddress(),
                trainee.getPhone(),
                trainee.getEnrollmentDate(),
                trainee.getLicenseCategory(),
                trainee.getStatus(),
                trainee.getAssignedInstructorId(),
                trainee.getTraineeId());
    }

    // Delete trainee by id
    public void delete(Integer traineeId) {
        String sql = "DELETE FROM Trainee WHERE TraineeID = ?";

        jdbcTemplate.update(sql, traineeId);
    }

    // Count trainees by status
    public Integer countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Trainee WHERE Status = ?";

        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    // Check if SSN already exists (for validation)
    public boolean existsBySsn(String ssn) {
        String sql = "SELECT COUNT(*) FROM Trainee WHERE SSN = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ssn);
        return count != null && count > 0;
    }
}

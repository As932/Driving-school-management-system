package com.drivingschool.repository;

import com.drivingschool.model.Instructor;
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
public class InstructorRepository {

    private final JdbcTemplate jdbcTemplate;

    public InstructorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Instructor> instructorRowMapper = (rs, rowNum) -> {
        Instructor instructor = new Instructor();

        instructor.setInstructorId(rs.getInt("InstructorID"));
        instructor.setUserId(rs.getInt("UserID"));
        instructor.setFirstName(rs.getString("FirstName"));
        instructor.setLastName(rs.getString("LastName"));
        instructor.setPhone(rs.getString("Phone"));

        // HireDate can be null
        if(rs.getDate("HireDate") != null) {
            instructor.setHireDate(rs.getDate("HireDate").toLocalDate());
        }

        // these might not exist in all queries
        try {
            instructor.setUsername(rs.getString("Username"));
            instructor.setEmail(rs.getString("Email"));
        } catch (Exception e) {
            // ignore if columns don't exist
        }

        return instructor;
    };

    // Find all instructors
    public List<Instructor> findAll() {
        String sql = """
                SELECT
                    i.InstructorID, i.UserID, i.FirstName, i.LastName, i.Phone,
                    i.HireDate,
                    u.Username, u.Email
                FROM Instructor i
                LEFT JOIN AppUser u ON i.UserID = u.UserID
                ORDER BY i.FirstName, i.LastName
                """;

        return jdbcTemplate.query(sql, instructorRowMapper);
    }

    // Find instructor by id
    public Instructor findById(Integer instructorId) {
        String sql = """
                SELECT
                    i.instructorID, i.UserID, i.FirstName, i.LastName,
                    i.Phone, i.HireDate,
                    u.Username, u.Email
                FROM Instructor i
                LEFT JOIN AppUser u ON i.UserID = u.UserID
                WHERE i.InstructorID = ?
                """;

        List<Instructor> instructors = jdbcTemplate.query(sql, instructorRowMapper, instructorId);

        return instructors.isEmpty() ? null : instructors.getFirst();
    }

    // Save new instructor, return generated instructor ID
    public Integer save(Instructor instructor) {
        String sql = """
                    INSERT INTO Instructor (UserID, FirstName, LastName, Phone, HireDate) VALUES
                        (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, instructor.getUserId());
            ps.setString(2, instructor.getFirstName());
            ps.setString(3, instructor.getLastName());
            ps.setString(4, instructor.getPhone());
            ps.setObject(5, instructor.getHireDate());

            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if(keys != null && keys.containsKey("INSTRUCTORID")){
            return ((Number) keys.get("INSTRUCTORID")).intValue();
        }

        throw new IllegalArgumentException("Failed to retrieve generated instructor ID");
    }

    // Update existing instructor
    public void update(Instructor instructor){
        String sql = """
                UPDATE Instructor SET
                    FirstName = ?,
                    LastName = ?,
                    Phone = ?,
                    HireDate = ?
                WHERE InstructorID = ?
                """;

        jdbcTemplate.update(sql,
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getPhone(),
                instructor.getHireDate(),
                instructor.getInstructorId());
    }

    // Delete instructor by ID
    public void delete(Integer instructorId) {
        String sql = "DELETE FROM Instructor WHERE InstructorID = ?";
        jdbcTemplate.update(sql, instructorId);
    }

    // Count total instructors
    public Integer count() {
        String sql = "SELECT COUNT(*) FROM Instructor";

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Check if instructor has assigned trainees
    public boolean hasAssignedTrainees(Integer instructorId){
        String sql = "SELECT COUNT(*) FROM Trainee WHERE AssignedInstructorID = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, instructorId);
        return (count != null && count > 0);
    }

    // Get count of assigned trainees
    public Integer getAssignedTraineesCount(Integer instructorId) {
        String sql = "SELECT COUNT(*) FROM Trainee WHERE AssignedInstructorID = ?";

        return jdbcTemplate.queryForObject(sql, Integer.class, instructorId);
    }

    // Check if instructor has assigned car
    public boolean hasAssignedCar(Integer instructorId) {
        String sql = "SELECT COUNT(*) FROM Car WHERE AssignedInstructorID = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, instructorId);
        return (count != null && count > 0);
    }
}
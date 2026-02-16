package com.drivingschool.repository;

import com.drivingschool.model.Session;
import jdk.jshell.Snippet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Session Repository - Data Access Layer using raw SQL
 */

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Session> sessionRowMapper = (rs, rowNum) -> {
        Session session = new Session();

        session.setSessionId(rs.getInt("SessionID"));
        session.setSessionType(rs.getString("SessionType"));

        if (rs.getTimestamp("StartDateTime") != null) {
            session.setStartDateTime(rs.getTimestamp("StartDateTime").toLocalDateTime());
        }
        if (rs.getTimestamp("EndDateTime") != null) {
            session.setEndDateTime(rs.getTimestamp("EndDateTime").toLocalDateTime());
        }

        session.setStatus(rs.getString("Status"));
        session.setInstructorFeedback(rs.getString("InstructorFeedback"));
        session.setInstructorId(rs.getInt("InstructorID"));

        // traineeId might be null for theoretical sessions
        try {
            int traineeId = rs.getInt("TraineeID");
            if (!rs.wasNull()) {
                session.setTraineeId(traineeId);
            }
        } catch (Exception e) {

        }

        // these might not exist in all queries
        try {
            session.setInstructorName(rs.getString("InstructorName"));
        } catch (Exception e) {

        }

        try {
            session.setTraineeName(rs.getString("TraineeName"));
        } catch (Exception e) {

        }

        try {
            session.setTraineeCount(rs.getInt("TraineeCount"));
        } catch (Exception e) {

        }

        return session;
    };

    // Find all sessions with instructor and trainee information
    public List<Session> findAll() {
        String sql = """
                SELECT
                    s.SessionID, s.SessionType, s.StartDateTime, s.EndDateTime,
                    s.Status, s.InstructorFeedback, s.InstructorID, s.TraineeID,
                    CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName,
                    (SELECT COUNT(*) FROM Trainee_Session WHERE SessionID = s.SessionID) AS TraineeCount
                FROM Session s
                LEFT JOIN Instructor i ON s.InstructorID = i.InstructorID
                LEFT JOIN Trainee t ON s.TraineeID = t.TraineeID
                ORDER BY s.StartDateTime DESC
                """;

        return jdbcTemplate.query(sql, sessionRowMapper);
    }

    // Find session by id
    public Session findById(Integer sessionId) {
        String sql = """
            SELECT
                s.SessionID, s.SessionType, s.StartDateTime, s.EndDateTime,
                s.Status, s.InstructorFeedback, s.InstructorID, s.TraineeID,
                CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName,
                CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName,
                (SELECT COUNT(*) FROM Trainee_Session WHERE SessionID = s.SessionID) AS TraineeCount
            FROM Session s
            LEFT JOIN Instructor i ON s.InstructorID = i.InstructorID
            LEFT JOIN Trainee t ON s.TraineeID = t.TraineeID
            WHERE s.SessionID = ?
            """;

        List<Session> sessions = jdbcTemplate.query(sql, sessionRowMapper, sessionId);
        return sessions.isEmpty() ? null : sessions.getFirst();
    }

    // Find sessions by instructor id
    public List<Session> findByInstructorId(Integer instructorId) {
        String sql = """
                SELECT
                    s.SessionID, s.SessionType, s.StartDateTime, s.EndDateTime,
                    s.Status, s.InstructorFeedback, s.InstructorID, s.TraineeID,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName,
                    (SELECT COUNT(*) FROM Trainee_Session WHERE SessionID = s.SessionID) AS TraineeCount
                FROM Session s
                LEFT JOIN Trainee t ON s.TraineeID = t.TraineeID
                WHERE s.InstructorID = ?
                ORDER BY s.StartDateTime DESC
                """;

        return jdbcTemplate.query(sql, sessionRowMapper, instructorId);
    }

    // Find sessions by trainee id (both practical and theoretical)
    public List<Session> findByTraineeId(Integer traineeId) {
        String sql = """
                SELECT DISTINCT
                    s.SessionID, s.SessionType, s.StartDateTime, s.EndDateTime,
                    s.Status, s.InstructorFeedback, s.InstructorID, s.TraineeID,
                    CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName
                FROM Session s
                LEFT JOIN Instructor i ON s.InstructorID = i.InstructorID
                LEFT JOIN Trainee_Session ts ON s.SessionID = ts.SessionID
                WHERE s.TraineeID = ? OR ts.TraineeID = ?
                ORDER BY s.StartDateTime DESC
                """;

        return jdbcTemplate.query(sql, sessionRowMapper, traineeId, traineeId);
    }

    // Find sessions by type
    public List<Session> findByType(String sessionType) {
        String sql = """
                SELECT
                    s.SessionID, s.SessionType, s.StartDateTime, s.EndDateTime,
                    s.Status, s.InstructorFeedback, s.InstructorID, s.TraineeID,
                    CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName,
                    (SELECT COUNT(*) FROM Trainee_Session WHERE SessionID = s.SessionID) AS TraineeCount
                FROM Session s
                LEFT JOIN Instructor i ON s.InstructorID = i.InstructorID
                LEFT JOIN Trainee t ON s.TraineeID = t.TraineeID
                WHERE s.SessionType = ?
                ORDER BY s.StartDateTime DESC
                """;

        return jdbcTemplate.query(sql, sessionRowMapper, sessionType);
    }

    // Find session by status
    public List<Session> findByStatus(String status) {
        String sql = """
                SELECT
                    s.SessionID, s.SessionType, s.StartDateTime, s.EndDateTime,
                    s.Status, s.InstructorFeedback, s.InstructorID, s.TraineeID,
                    CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName,
                    CONCAT(t.FirstName, ' ', t.LastName) AS TraineeName,
                    (SELECT COUNT(*) FROM Trainee_Session WHERE SessionID = s.SessionID) AS TraineeCount
                FROM Session s
                LEFT JOIN Instructor i ON s.InstructorID = i.InstructorID
                LEFT JOIN Trainee t ON s.TraineeID = t.TraineeID
                WHERE s.Status = ?
                ORDER BY s.StartDateTime DESC
                """;

        return jdbcTemplate.query(sql, sessionRowMapper, status);
    }

    // Save new session (INSERT)
    public Integer save(Session session) {
        String sql = """
                INSERT INTO Session (SessionType, StartDateTime, EndDateTime, Status,
                    InstructorFeedback, InstructorID, TraineeID)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, session.getSessionType());
            ps.setTimestamp(2, Timestamp.valueOf(session.getStartDateTime()));
            ps.setTimestamp(3, Timestamp.valueOf(session.getEndDateTime()));
            ps.setString(4, session.getStatus());

            if (session.getInstructorFeedback() != null && !session.getInstructorFeedback().isEmpty()) {
                ps.setString(5, session.getInstructorFeedback());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }

            ps.setInt(6, session.getInstructorId());

            // TraineeID can be null for theoretical sessions
            if (session.getTraineeId() != null) {
                ps.setInt(7, session.getTraineeId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("SESSIONID")) {
            return ((Number) keys.get("SESSIONID")).intValue();
        }

        throw new IllegalStateException("Failed to retrieve generated session ID");
    }

    // Update existing session
    public void update(Session session) {
        String sql = """
            UPDATE Session SET
                SessionType = ?,
                StartDateTime = ?,
                EndDateTime = ?,
                Status = ?,
                InstructorFeedback = ?,
                InstructorID = ?,
                TraineeID = ?
            WHERE SessionID = ?
            """;

        jdbcTemplate.update(sql,
                session.getSessionType(),
                Timestamp.valueOf(session.getStartDateTime()),
                Timestamp.valueOf(session.getEndDateTime()),
                session.getStatus(),
                session.getInstructorFeedback(),
                session.getInstructorId(),
                session.getTraineeId(),
                session.getSessionId());
    }

    // Delete session by id
    public void delete(Integer sessionId) {
        String sql = "DELETE FROM Session WHERE SessionID = ?";
        jdbcTemplate.update(sql, sessionId);
    }

    // Count total sessions
    public Integer count() {
        String sql = "SELECT COUNT(*) FROM Session";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Count sessions by status
    public Integer countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Session WHERE Status = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }

    // Count sessions by type
    public Integer countByType(String sessionType) {
        String sql = "SELECT COUNT(*) FROM Session WHERE SessionType = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, sessionType);
    }

    // Add trainee to theoretical session
    public void addTraineeToSession(Integer traineeId, Integer sessionId) {
        String sql = "INSERT INTO Trainee_Session (TraineeID, SessionId) " +
                "VALUES (?, ?)";
        jdbcTemplate.update(sql, traineeId, sessionId);
    }

    // Remove trainee from theoretical session
    public void removeTraineeFromSession(Integer traineeId, Integer sessionId) {
        String sql = "DELETE FROM Trainee_Session WHERE " +
                "TraineeID = ? AND SessionID = ?";
        jdbcTemplate.update(sql, traineeId, sessionId);
    }

    // Get all trainees enrolled in a theoretical session
    public List<Integer> getTraineeIdsForSession(Integer sessionId) {
        String sql = "SELECT TraineeID FROM Trainee_Session WHERE SessionID = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, sessionId);
    }

    // Calculate total practical hours completed by a trainee
    public Double getTotalPracticalHoursByTrainee(Integer traineeId) {
        String sql = """
                SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, StartDateTime, EndDateTime)) / 60.0, 0)
                FROM Session
                WHERE TraineeID = ? AND SessionType = 'Practical' AND Status = 'Completed'
                """;

        return jdbcTemplate.queryForObject(sql, Double.class, traineeId);
    }
}

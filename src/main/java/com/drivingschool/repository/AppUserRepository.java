package com.drivingschool.repository;

import com.drivingschool.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AppUser repository - Data Access Layer using raw SQL
 */

@Repository
public class AppUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AppUser> userRowMapper = (rs, rowNum) -> {
        AppUser appUser = new AppUser();

        appUser.setUserId(rs.getInt("UserID"));
        appUser.setUsername(rs.getString("Username"));
        appUser.setPassword(rs.getString("Password"));
        appUser.setEmail(rs.getString("Email"));
        appUser.setRole(rs.getString("Role"));
        appUser.setIsActive(rs.getBoolean("IsActive"));

        if(rs.getTimestamp("CreatedAt") != null){
            appUser.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        }

        return appUser;
    };

    // Create new user account, return generated user id
    public Integer save(AppUser user) {
        String sql = """
                INSERT INTO
                    AppUser (UserName, Password, Email, Role, IsActive) VALUES
                    (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.getIsActive() != null ? user.getIsActive() : true);

            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("USERID")) {
            return ((Number) keys.get("USERID")).intValue();
        }

        throw new IllegalArgumentException("Failed to retrieve generated user ID");
    }

    // Find user by username
    public AppUser findByUsername(String username) {
        String sql = "SELECT * FROM AppUser WHERE Username = ?";

        List<AppUser> users = jdbcTemplate.query(sql, userRowMapper, username);
        return users.isEmpty() ? null : users.getFirst();
    }

    // Find user by id
    public AppUser findById(Integer userId) {
        String sql = "SELECT * FROM AppUser WHERE UserID = ?";

        List<AppUser> users = jdbcTemplate.query(sql, userRowMapper, userId);
        return users.isEmpty() ? null : users.getFirst();
    }

    // Check if username already exists
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM AppUser WHERE Username = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    // Check if email already exists
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM AppUser WHERE Email = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    // Update user information
    public void update(AppUser user) {
        String sql = """
                UPDATE AppUser SET
                    Username = ?,
                    Email = ?,
                    Role = ?,
                    IsActive = ?
                WHERE UserID = ?
                """;

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getUserId());
    }

    // Delete user by id
    public void delete(Integer userId){
        String sql = "DELETE FROM AppUser WHERE UserID = ?";
        jdbcTemplate.update(sql, userId);
    }
}
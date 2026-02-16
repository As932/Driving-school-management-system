package com.drivingschool.repository;

import com.drivingschool.model.Car;
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
 * Car repository - Data Access Layer using raw SQL
 */
@Repository
public class CarRepository {

    private final JdbcTemplate jdbcTemplate;

    public CarRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Car> carRowMapper = (rs, rowNum) -> {
        Car car = new Car();

        car.setCarId(rs.getInt("CarID"));
        car.setLicensePlate(rs.getString("LicensePlate"));
        car.setBrand(rs.getString("Brand"));
        car.setModel(rs.getString("Model"));
        car.setTransmissionType(rs.getString("TransmissionType"));
        car.setAssignedInstructorId(rs.getInt("AssignedInstructorID"));

        // instructor name might not exist in all queries
        try {
            car.setInstructorName(rs.getString("InstructorName"));
        } catch (Exception e) {
            // ignore if column does not exist
        }

        return car;
    };

    // find all cars with instructor information
    public List<Car> findAll() {
        String sql = """
                SELECT
                    c.CarID, c.LicensePlate, c.Brand, c.Model,
                    c.TransmissionType, c.AssignedInstructorID,
                    CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName
                FROM Car c
                LEFT JOIN Instructor i ON c.AssignedInstructorID = i.InstructorID
                ORDER BY c.Brand, c.Model
                """;

        return jdbcTemplate.query(sql, carRowMapper);
    }

    // Find car by id
    public Car findById(Integer carId) {
        String sql = """
                SELECT
                    c.CarID, c.LicensePlate, c.Brand, c.Model,
                    c.TransmissionType, c.AssignedInstructorID,
                    CONCAT(i.FirstName, ' ', i.LastName) AS InstructorName
                FROM Car c
                LEFT JOIN Instructor i ON c.AssignedInstructorID = i.InstructorID
                WHERE c.CarID = ?
                """;

        List<Car> cars = jdbcTemplate.query(sql, carRowMapper, carId);
        return cars.isEmpty() ? null : cars.getFirst();
    }

    // Find car by license plate
    public Car findByLicensePlate(String licensePlate) {
        String sql = """
                SELECT
                    c.CarID, c.LicensePlate, c.Brand, c.Model,
                    c.TransmissionType, c.AssignedInstructorID
                FROM Car c
                WHERE c.LicensePlate = ?
                """;

        List<Car> cars = jdbcTemplate.query(sql, carRowMapper, licensePlate);
        return cars.isEmpty() ? null : cars.getFirst();
    }

    // Find car by assigned instructor
    public Car findByInstructorId(Integer instructorId) {
        String sql = """
                SELECT
                    c.CarID, c.LicensePlate, c.Brand, c.Model,
                    c.TransmissionType, c.AssignedInstructorID
                FROM Car c
                WHERE c.AssignedInstructorID = ?
                """;

        List<Car> cars = jdbcTemplate.query(sql, carRowMapper, instructorId);
        return cars.isEmpty() ? null : cars.getFirst();
    }

    // Save new car (INSERT)
    public Integer save(Car car) {
        String sql = """
                INSERT INTO Car (LicensePlate, Brand, Model, TransmissionType, AssignedInstructorID)
                    VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, car.getLicensePlate());
            ps.setString(2, car.getBrand());
            ps.setString(3, car.getModel());
            ps.setString(4, car.getTransmissionType());
            ps.setInt(5, car.getAssignedInstructorId());

            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("CARID")) {
            return ((Number) keys.get("CARID")).intValue();
        }

        throw new IllegalArgumentException("Failed to retrieve generated car ID");
    }

    // Update existing car
    public void update(Car car) {
        String sql = """
                UPDATE Car SET
                    LicensePlate = ?,
                    Brand = ?,
                    Model = ?,
                    TransmissionType = ?,
                    AssignedInstructorID = ?
                WHERE CarID = ?
                """;

        jdbcTemplate.update(sql,
                car.getLicensePlate(),
                car.getBrand(),
                car.getModel(),
                car.getTransmissionType(),
                car.getAssignedInstructorId(),
                car.getCarId());
    }

    // Delete car by id
    public void delete(Integer carId) {
        String sql = "DELETE FROM Car WHERE CarID = ?";
        jdbcTemplate.update(sql, carId);
    }

    // Count total cars
    public Integer count() {
        String sql = "SELECT COUNT(*) FROM Car";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    // Check if license plate already exists
    public boolean existsByLicensePlate(String licensePlate) {
        String sql = "SELECT COUNT(*) FROM Car WHERE LicensePlate = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, licensePlate);
        return count != null && count > 0;
    }

    // Check if instructor already has a car assigned
    public boolean instructorHasCar(Integer instructorId) {
        String sql = "SELECT COUNT(*) FROM Car WHERE AssignedInstructorID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, instructorId);
        return count != 0 && count > 0;
    }
}

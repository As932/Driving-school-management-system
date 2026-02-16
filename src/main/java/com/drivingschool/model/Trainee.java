package com.drivingschool.model;

import java.time.LocalDate;
import lombok.*;

/**
 * Trainee model class
 * Corresponds to the Trainee table in the database
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trainee {

    private Integer traineeId;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String ssn;     // Social Security Number (CNP in Romania)
    private String address;
    private String phone;
    private LocalDate enrollmentDate;
    private String licenseCategory;
    private String status;
    private Integer assignedInstructorId;

    // for joins - not in database
    private String username;
    private String email;
    private String instructorName;
}

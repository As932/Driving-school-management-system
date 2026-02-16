package com.drivingschool.model;

import java.time.LocalDate;
import lombok.*;

/**
 * Instructor model class
 * Corresponds to the Instructor table in the database
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instructor {

    private Integer instructorId;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate hireDate;

    // for joins - not in database
    private String username;
    private String email;
}

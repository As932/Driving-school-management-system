package com.drivingschool.model;

import java.time.LocalDateTime;
import lombok.*;

/**
 * AppUser model class
 * Corresponds to the AppUser table in the database
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    private Integer userId;
    private String username;
    private String password;
    private String email;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

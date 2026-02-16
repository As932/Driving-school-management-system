package com.drivingschool.model;

import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Session model class
 * Corresponds to the Session table in the database
 * Supports both Practical (1 to 1 with trainee) and Theoretical (many to many) sessions
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    private Integer sessionId;
    private String sessionType;         // Practical or Theoretical
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;              // Scheduled or Completed
    private String instructorFeedback;
    private Integer instructorId;
    private Integer traineeId;          // null for Theoretical group sessions

   // for joins - not in database
   private String instructorName;
   private String traineeName;
   private Integer traineeCount;

   // Check if this is a practical session
    public boolean isPractical() {
        return "Practical".equals(sessionType);
    }

    // Check if this is a theoretical session
    public boolean isTheoretical() {
        return "Theoretical".equals(sessionType);
    }

    // Check if session is completed
    public boolean isCompleted() {
        return "Completed".equals(status);
    }

    // Check if session is scheduled
    public boolean isScheduled() {
        return "Scheduled".equals(status);
    }

    // Get session duration in hours
    public Double getDurationHours() {
        if (startDateTime != null && endDateTime != null) {
            long minutes = Duration.between(startDateTime, endDateTime).toMinutes();
            return minutes / 60.0;
        }
        return 0.0;
    }

    // Check if session is in the past
    public  boolean isPast() {
        return startDateTime != null && startDateTime.isBefore(LocalDateTime.now());
    }

    // Check if session id upcoming
    public boolean isUpcoming() {
        return startDateTime != null && startDateTime.isAfter(LocalDateTime.now());
    }
}

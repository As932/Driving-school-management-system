package com.drivingschool.model;

import java.time.LocalDate;
import lombok.*;

/**
 * Exam model class
 * Corresponds to the Exam table in the database
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {

    private Integer examId;
    private String examType;
    private LocalDate scheduledDate;
    private String status;
    private Integer traineeId;

    // For joins - not in database
    private String traineeName;

    public boolean isUpcoming() {
        return "Scheduled".equals(status) && scheduledDate != null &&
                (scheduledDate.isAfter(LocalDate.now()) || scheduledDate.isEqual(LocalDate.now()));
    }

    public boolean isPast() {
        return scheduledDate != null && scheduledDate.isBefore(LocalDate.now());
    }
}

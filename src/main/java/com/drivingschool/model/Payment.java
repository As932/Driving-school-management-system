package com.drivingschool.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

/**
 * Payment model class
 * Corresponds to the Payment table in the database
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    private Integer paymentId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String details;
    private Integer traineeId;

    // for joins - not in database
    private String traineeName;
}

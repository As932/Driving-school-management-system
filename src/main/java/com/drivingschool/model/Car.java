package com.drivingschool.model;

import lombok.*;

/**
 * Car model class
 * Corresponds to the Car table in the database
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    private Integer carId;
    private String licensePlate;
    private String brand;
    private String model;
    private String TransmissionType;
    private Integer assignedInstructorId;

    // for joins - not in database
    private String instructorName;

    public String getFullDescription() {
        return brand + " " + model + " (" + licensePlate + ")";
    }
}

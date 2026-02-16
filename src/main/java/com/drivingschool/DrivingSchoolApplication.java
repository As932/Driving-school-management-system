package com.drivingschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DrivingSchoolApplication {

    public static void main(String[] args){
        SpringApplication.run(DrivingSchoolApplication.class, args);

        System.out.println("\n========================================");
        System.out.println("Driving School Management System Started!");
        System.out.println("========================================");
        System.out.println("Application URL: http://localhost:8080");
        System.out.println("H2 Console URL: http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}

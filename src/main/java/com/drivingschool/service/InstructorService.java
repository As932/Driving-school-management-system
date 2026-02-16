package com.drivingschool.service;

import com.drivingschool.model.AppUser;
import com.drivingschool.model.Instructor;
import com.drivingschool.repository.AppUserRepository;
import com.drivingschool.repository.InstructorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Instructor Service - Business Logic Layer
 */

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public InstructorService(InstructorRepository instructorRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.instructorRepository = instructorRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all instructors
    public List<Instructor> getAllInstructors() {
        return instructorRepository.findAll();
    }

    // Get instructor by id
    public Instructor getInstructorById(Integer instructorId) {
        return instructorRepository.findById(instructorId);
    }

    // Create new instructor with user account (with password encryption)
    @Transactional
    public Integer createInstructor(Instructor instructor, String username, String password, String email) {
        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        AppUser newUser = new AppUser();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setRole("INSTRUCTOR");
        newUser.setIsActive(true);

        Integer userId = appUserRepository.save(newUser);

        instructor.setUserId(userId);

        if (instructor.getHireDate() == null) {
            instructor.setHireDate(LocalDate.now());
        }

        return instructorRepository.save(instructor);
    }

    // Update existing instructor
    @Transactional
    public void updateInstructor(Instructor instructor) {
        Instructor existing = instructorRepository.findById(instructor.getInstructorId());

        if (existing == null) {
            throw new IllegalArgumentException("Instructor not found: " + instructor.getInstructorId());
        }

        instructorRepository.update(instructor);
    }

    // Delete instructor and associated user account
    @Transactional
    public void deleteInstructor(Integer instructorId) {
        Instructor existing = instructorRepository.findById(instructorId);

        if(existing == null) {
            throw new IllegalArgumentException("Instructor not found: " + instructorId);
        }

        if (instructorRepository.hasAssignedTrainees(instructorId)) {
            throw new IllegalArgumentException("Cannot delete instructor. " +
                    "They have " + instructorRepository.getAssignedTraineesCount(instructorId) + " trainee(s). " +
                    "Please reassign trainees first.");
        }

        if (instructorRepository.hasAssignedCar(instructorId)) {
            throw new IllegalArgumentException("Cannot delete instructor. They have an assigned car. " +
                    "Please reassign car first.");
        }

        instructorRepository.delete(instructorId);
        appUserRepository.delete(existing.getUserId());
    }

    // Get total count
    public Integer getTotalCount() {
        return instructorRepository.count();
    }
}
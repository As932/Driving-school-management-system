package com.drivingschool.service;

import com.drivingschool.model.AppUser;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.AppUserRepository;
import com.drivingschool.repository.TraineeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Trainee Service - Business Logic Layer
 */

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public TraineeService(TraineeRepository traineeRepository, AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all trainees
    public List<Trainee> getAllTrainees() {
        return traineeRepository.findAll();
    }

    // Get trainee by id
    public Trainee getTraineeById(Integer traineeId) {
        return traineeRepository.findById(traineeId);
    }

    // Get Active trainees
    public List<Trainee> getActiveTrainees() {
        return traineeRepository.findByStatus("Active");
    }

    // Get Completed trainees
    public List<Trainee> getCompletedTrainees() {
        return traineeRepository.findByStatus("Completed");
    }

    // Get trainees by instructor id
    public List<Trainee> getTraineesByInstructorId(Integer instructorId) {
        return traineeRepository.findByInstructorId(instructorId);
    }

    // Create new trainee with user account (with password encription)
    @Transactional
    public Integer createTrainee(Trainee trainee, String username, String password, String email) {

        if (appUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username " + username + " already exists!");
        }

        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email " + email + " already exists!");
        }

        if (traineeRepository.existsBySsn(trainee.getSsn())) {
            throw new IllegalArgumentException("SSN " + trainee.getSsn() + " already exists!");
        }

        AppUser newUser = new AppUser();

        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setRole("TRAINEE");
        newUser.setIsActive(true);

        Integer userId = appUserRepository.save(newUser);

        trainee.setUserId(userId);

        if (trainee.getEnrollmentDate() == null){
            trainee.setEnrollmentDate(LocalDate.now());
        }

        if (trainee.getStatus() == null || trainee.getStatus().isEmpty()) {
            trainee.setStatus("Active");
        }

        return traineeRepository.save(trainee);
    }

    // Update existing trainee
    @Transactional
    public void updateTrainee(Trainee trainee) {
        Trainee existing = traineeRepository.findById(trainee.getTraineeId());

        if (existing == null) {
            throw new IllegalArgumentException("Trainee : " + trainee.getTraineeId() + " not found!");
        }

        if (!existing.getSsn().equals(trainee.getSsn())) {
            if (traineeRepository.existsBySsn(trainee.getSsn())) {
                throw new IllegalArgumentException("SSN " + trainee.getSsn() + " already exists!");
            }
        }

        traineeRepository.update(trainee);
    }

    // Delete trainee and associated user account
    @Transactional
    public void deleteTrainee(Integer traineeId) {
        Trainee existing = traineeRepository.findById(traineeId);

        if (existing == null) {
            throw new IllegalArgumentException("Trainee " + traineeId + " not found!");
        }

        traineeRepository.delete(traineeId);
        appUserRepository.delete(existing.getUserId());
    }

    // Change trainee status
    public void changeStatus(Integer traineeId, String newStatus) {
        Trainee existing = traineeRepository.findById(traineeId);

        if (existing == null) {
            throw new IllegalArgumentException("Trainee " + traineeId + " not found!");
        }

        existing.setStatus(newStatus);
        traineeRepository.update(existing);
    }

    // Reassign trainee to different instructor
    public void reassignInstructor(Integer traineeId, Integer newInstructorId) {
        Trainee existing = traineeRepository.findById(traineeId);

        if (existing == null) {
            throw new IllegalArgumentException("Trainee " + traineeId + " not found!");
        }

        existing.setAssignedInstructorId(newInstructorId);
        traineeRepository.update(existing);
    }

    // Get count by status for reporting
    public Integer getCountByStatus(String status) {
        return traineeRepository.countByStatus(status);
    }
}
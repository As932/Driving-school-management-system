package com.drivingschool.service;

import com.drivingschool.model.Exam;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.ExamRepository;
import com.drivingschool.repository.TraineeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Exam Service - Business Logic Layer
 */

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final TraineeRepository traineeRepository;

    public ExamService(ExamRepository examRepository, TraineeRepository traineeRepository) {
        this.examRepository = examRepository;
        this.traineeRepository = traineeRepository;
    }

    // Get all exams
    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    // Get exam by id
    public Exam getExamById(Integer examId) {
        return examRepository.findById(examId);
    }

    // Get all exams for a trainee
    public List<Exam> getExamsByTrainee(Integer traineeId) {
        return examRepository.findByTraineeId(traineeId);
    }

    // Get exams by type
    public List<Exam> getExamsByType(String examType) {
        return examRepository.findByExamType(examType);
    }

    // Get exams by status
    public List<Exam> getExamsByStatus(String status) {
        return examRepository.findByStatus(status);
    }

    // Create new exam
    @Transactional
    public Integer createExam(Exam exam) {

        // Validate trainee exists
        Trainee trainee = traineeRepository.findById(exam.getTraineeId());
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee not found: " + exam.getTraineeId());
        }

        // Validate scheduled date is not in the past
        if (exam.getScheduledDate() != null && exam.getScheduledDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Exam date cannot be in the past");
        }

        // Set default status if not provided
        if (exam.getStatus() == null || exam.getStatus().isEmpty()) {
            exam.setStatus("Scheduled");
        }

        return examRepository.save(exam);
    }

    // Update existing exam
    @Transactional
    public void updateExam(Exam exam) {

        // Validate exam exists
        Exam existing = examRepository.findById(exam.getExamId());
        if (existing == null) {
            throw new IllegalArgumentException("Exam not found: " + exam.getExamId());
        }

        // Validate trainee exists
        Trainee trainee = traineeRepository.findById(exam.getTraineeId());
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee not found: " + exam.getTraineeId());
        }

        examRepository.update(exam);
    }

    // Delete exam
    @Transactional
    public void deleteExam(Integer examId) {
        Exam exam = examRepository.findById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Exam not found: " + examId);
        }

        examRepository.delete(examId);
    }

    // Change exam status
    public void changeStatus(Integer examId, String newStatus) {
        Exam exam = examRepository.findById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("Exam not found: " + examId);
        }

        exam.setStatus(newStatus);
        examRepository.update(exam);
    }

    // Get total count
    public Integer getTotalCount() {
        return examRepository.count();
    }

    // Get count by status
    public Integer getCountByStatus(String status) {
        return examRepository.countByStatus(status);
    }

    // Get count by type
    public Integer getCountByType(String examType) {
        return examRepository.countByType(examType);
    }

    // Get exam statistics
    public ExamStats getExamStatistics() {
        Integer totalExams = getTotalCount();
        Integer scheduledExams = getCountByStatus("Scheduled");
        Integer completedExams = getCountByStatus("Completed");
        Integer theoreticalExams = getCountByType("Theoretical");
        Integer practicalExams = getCountByType("Practical");

        return new ExamStats(totalExams, scheduledExams, completedExams,
                theoreticalExams, practicalExams);
    }

    // Inner class for exam statistics
    @Getter
    @AllArgsConstructor
    public static class ExamStats {
        private final Integer totalExams;
        private final Integer scheduledExams;
        private final Integer completedExams;
        private final Integer theoreticalExams;
        private final Integer practicalExams;
    }
}

package com.drivingschool.service;

import com.drivingschool.model.Instructor;
import com.drivingschool.model.Session;
import com.drivingschool.model.Trainee;
import com.drivingschool.repository.InstructorRepository;
import com.drivingschool.repository.SessionRepository;
import com.drivingschool.repository.TraineeRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Session Service - Business Logic Layer
 */

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final InstructorRepository instructorRepository;
    private final TraineeRepository traineeRepository;

    public SessionService(SessionRepository sessionRepository, InstructorRepository instructorRepository, TraineeRepository traineeRepository) {
        this.sessionRepository = sessionRepository;
        this.instructorRepository = instructorRepository;
        this.traineeRepository = traineeRepository;
    }

    // Get all sessions
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    // Get session by id
    public Session getSessionById(Integer sessionId) {
        return sessionRepository.findById(sessionId);
    }

    // Get sessions by instructor
    public List<Session> getSessionsByInstructor(Integer instructorId) {
        return sessionRepository.findByInstructorId(instructorId);
    }

    // Get sessions by trainee
    public List<Session> getSessionsByTrainee(Integer traineeId) {
        return sessionRepository.findByTraineeId(traineeId);
    }

    // Get sessions by type
    public List<Session> getSessionsByType(String sessionType) {
        return sessionRepository.findByType(sessionType);
    }

    // Get sessions by status
    public List<Session> getSessionsByStatus(String status) {
        return sessionRepository.findByStatus(status);
    }

    // Create new session
    @Transactional
    public Integer createSession(Session session, List<Integer> traineeIds) {
        // validate instructor exists
        Instructor instructor = instructorRepository.findById(session.getInstructorId());
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor not found: " + session.getInstructorId());
        }

        // validate trainee for practical sessions
        if (session.isPractical()) {
            if (session.getTraineeId() == null) {
                throw new IllegalArgumentException("Practical sessions must have a trainee assigned");
            }

            Trainee trainee = traineeRepository.findById(session.getTraineeId());
            if (trainee == null) {
                throw new IllegalArgumentException("Trainee not found: " + session.getTraineeId());
            }
        }

        // validate theoretical sessions have trainees
        if (session.isTheoretical()) {
            if (traineeIds == null || traineeIds.isEmpty()) {
                throw new IllegalArgumentException("Theoretical sessions must have at least one trainee");
            }

            // set trainee id to null for theoretical sessions
            session.setTraineeId(null);
        }

        // validate date time
        if (session.getStartDateTime() == null || session.getEndDateTime() == null) {
            throw new IllegalArgumentException("Start and end date/time are required");
        }

        if (session.getEndDateTime().isBefore(session.getStartDateTime())) {
            throw new IllegalArgumentException("End date/time must be after start date/time");
        }

        if (session.getStartDateTime().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalArgumentException("Cannot schedule sessions in the past");
        }

        // set default status if not provided
        if (session.getStatus() == null || session.getStatus().isEmpty()) {
            session.setStatus("Scheduled");
        }

        // save session
        Integer sessionId = sessionRepository.save(session);

        // for theoretical session, add trainees to junction table
        if (session.isTheoretical() && traineeIds != null) {
            for (Integer traineeId: traineeIds) {
                // validate each trainee exists
                Trainee trainee = traineeRepository.findById(traineeId);
                if (trainee == null) {
                    throw new IllegalArgumentException("Trainee not found: " + traineeId);
                }
                sessionRepository.addTraineeToSession(traineeId, sessionId);
            }
        }

        return sessionId;
    }

    // Update existing session
    @Transactional
    public void updateSession(Session session, List<Integer> traineeIds) {
        // validate session exists
        Session existing = sessionRepository.findById(session.getSessionId());
        if (existing == null) {
            throw new IllegalArgumentException("Session not found: " + session.getSessionId());
        }

        // validate instructor exists
        Instructor instructor = instructorRepository.findById(session.getInstructorId());
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor not found: " + session.getInstructorId());
        }

        // validate trainee for practical session
        if (session.isPractical()) {
            if (session.getTraineeId() == null) {
                throw new IllegalArgumentException("Practical session must have a trainee assigned.");
            }

            Trainee trainee = traineeRepository.findById(session.getTraineeId());
            if (trainee == null) {
                throw new IllegalArgumentException("Trainee not found: " + session.getTraineeId());
            }
        }

        // validate date/time
        if (session.getEndDateTime().isBefore(session.getStartDateTime())) {
            throw new IllegalArgumentException("End date/time must be after start date/time");
        }

        // update session
        sessionRepository.update(session);

        // for theoretical sessions, update trainee assignments
        if (session.isTheoretical() && traineeIds != null) {
            // remove all existing trainee assignments
            List<Integer> existingTraineeIds = sessionRepository.getTraineeIdsForSession(session.getSessionId());
            for (Integer traineeId: existingTraineeIds) {
                sessionRepository.removeTraineeFromSession(traineeId, session.getSessionId());
            }

            // add new trainee assignments
            for (Integer traineeId: traineeIds) {
                Trainee trainee = traineeRepository.findById(traineeId);
                if (trainee == null) {
                    throw new IllegalArgumentException("Trainee not found: " + traineeId);
                }
                sessionRepository.addTraineeToSession(traineeId, session.getSessionId());
            }
        }
    }

    // Delete session
    @Transactional
    public void deleteSession(Integer sessionId) {
        Session session = sessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        // Trainee_Session records will be deleted automatically due to ON DELETE CASCADE
        sessionRepository.delete(sessionId);
    }

    // Change session status
    @Transactional
    public void changeStatus(Integer sessionId, String newStatus) {
        Session session = sessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        session.setStatus(newStatus);
        sessionRepository.update(session);
    }

    // Add feedback to completed session
    @Transactional
    public void addFeedback(Integer sessionId, String feedback) {
        Session session = sessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        if (!session.isCompleted()) {
            throw new IllegalArgumentException("Can only add feedback to completed sessions");
        }

        session.setInstructorFeedback(feedback);
        sessionRepository.update(session);
    }

    // Get trainees enrolled in a theoretical session
    public List<Integer> getTraineesForSession(Integer sessionId) {
        return sessionRepository.getTraineeIdsForSession(sessionId);
    }

    // Get total practical hours completed by a trainee
    public Double getTotalPracticalHours(Integer traineeId) {
        return sessionRepository.getTotalPracticalHoursByTrainee(traineeId);
    }

    // Get total count
    public Integer getTotalCount() {
        return sessionRepository.count();
    }

    // Get count by status
    public Integer getCountByStatus(String status) {
        return sessionRepository.countByStatus(status);
    }

    // Get count by type
    public Integer getCountByType(String sessionType) {
        return sessionRepository.countByType(sessionType);
    }

    // Get session statistics
    public SessionStats getSessionStatistics() {
        Integer totalSessions = getTotalCount();
        Integer scheduledSessions = getCountByStatus("Scheduled");
        Integer completedSessions = getCountByStatus("Completed");
        Integer practicalSessions = getCountByType("Practical");
        Integer theoreticalSessions = getCountByType("Theoretical");

        return new SessionStats(totalSessions, scheduledSessions, completedSessions,
                practicalSessions, theoreticalSessions);
    }

    // Inner class for session statistics
    @Getter
    @AllArgsConstructor
    public static class SessionStats {
        private final Integer totalSessions;
        private final Integer scheduledSessions;
        private final Integer completedSessions;
        private final Integer practicalSessions;
        private final Integer theoreticalSessions;
    }
}

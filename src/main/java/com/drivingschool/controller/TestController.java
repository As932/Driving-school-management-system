package com.drivingschool.controller;

import com.drivingschool.model.Trainee;
import com.drivingschool.repository.TraineeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class TestController {

    private final TraineeRepository traineeRepository;

    public TestController(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @GetMapping("/test/trainees")
    public List<Trainee> getAllTrainees() {
        return traineeRepository.findAll();
    }

    @GetMapping("/test/active-trainees")
    public List<Trainee> getActiveTrainees() {
        return traineeRepository.findByStatus("Active");
    }
}

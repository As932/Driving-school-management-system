package com.drivingschool.service;

import com.drivingschool.model.Car;
import com.drivingschool.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Car Service - Business Logic Layer
 */

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    // Get all cars
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    // Get car by id
    public Car getCarById(Integer carId) {
        return carRepository.findById(carId);
    }

    // Get car by license plate
    public Car getCarByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlate(licensePlate);
    }

    // Get car assigned to instructor
    public Car getCarByInstructor(Integer instructorId) {
        return carRepository.findByInstructorId(instructorId);
    }

    // Create new car
    @Transactional
    public Integer createCar(Car car) {
        if (carRepository.existsByLicensePlate(car.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already exists: " + car.getLicensePlate());
        }

        if (carRepository.instructorHasCar(car.getAssignedInstructorId())) {
            throw new IllegalArgumentException("This instructor already has a car assigned. " +
                    "Each instructor can only have one car.");
        }

        return carRepository.save(car);
    }

    // Update existing car
    @Transactional
    public void updateCar(Car car) {
        Car existing = carRepository.findById(car.getCarId());

        if (existing == null) {
            throw new IllegalArgumentException("Car not found: " + car.getCarId());
        }

        if(!existing.getLicensePlate().equals(car.getLicensePlate())) {
            if (carRepository.existsByLicensePlate(car.getLicensePlate())) {
                throw new IllegalArgumentException("License plate already exists: " + car.getLicensePlate());
            }
        }

        if (!existing.getAssignedInstructorId().equals(car.getAssignedInstructorId())) {
            if (carRepository.instructorHasCar(car.getAssignedInstructorId())) {
                throw new IllegalArgumentException("This instructor already has a car assigned. " +
                        "Each instructor can only have one car.");
            }
        }

        carRepository.update(car);
    }

    // Delete car
    @Transactional
    public void deleteCar(Integer carId) {
        Car existing = carRepository.findById(carId);

        if (existing == null) {
            throw new IllegalArgumentException("Car not found: " + carId);
        }

        carRepository.delete(carId);
    }

    // Get total count
    public Integer getTotalCount() {
        return carRepository.count();
    }

    // Reassign car to different instructor
    public void reassignCar(Integer carId, Integer newInstructorId) {
        Car existing = carRepository.findById(carId);

        if (existing == null) {
            throw new IllegalArgumentException("Car not found: " + carId);
        }

        if (carRepository.instructorHasCar(newInstructorId)) {
            throw new IllegalArgumentException("This instructor already has a car assigned!");
        }

        existing.setAssignedInstructorId(newInstructorId);
        carRepository.update(existing);
    }
}

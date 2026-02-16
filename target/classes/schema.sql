SET MODE MySQL;

CREATE TABLE AppUser (
    UserID INTEGER PRIMARY KEY AUTO_INCREMENT,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Role VARCHAR(20) NOT NULL CHECK(Role IN ('ADMIN', 'INSTRUCTOR', 'TRAINEE')),
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Administrator (
    AdminID INTEGER PRIMARY KEY AUTO_INCREMENT,
    UserID INTEGER UNIQUE NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    Phone VARCHAR(20) CHECK(LENGTH(Phone) = 10 AND Phone REGEXP '^[0-9]+$'),
    FOREIGN KEY (UserID) REFERENCES AppUser(UserID) ON DELETE CASCADE
);

CREATE TABLE Instructor (
    InstructorID INTEGER PRIMARY KEY AUTO_INCREMENT,
    UserID INTEGER UNIQUE NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    Phone VARCHAR(20) NOT NULL CHECK(LENGTH(Phone) = 10 AND Phone REGEXP '^[0-9]+$'),
    HireDate DATE,
    FOREIGN KEY (UserID) REFERENCES AppUser(UserID) ON DELETE CASCADE
);

CREATE TABLE Trainee (
    TraineeID INTEGER PRIMARY KEY AUTO_INCREMENT,
    UserID INTEGER UNIQUE NOT NULL,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    SSN CHAR(13) UNIQUE NOT NULL, -- Social Security Number (CNP in Romania)
    Address VARCHAR(100),
    Phone VARCHAR(20) NOT NULL CHECK(LENGTH(Phone) = 10 AND Phone REGEXP '^[0-9]+$'),
    EnrollmentDate DATE,
    LicenseCategory VARCHAR(5) NOT NULL CHECK(LicenseCategory IN
        ('A', 'A1', 'A2', 'B', 'B1', 'C', 'C1', 'C+E', 'D', 'D1', 'D+E')
    ),
    Status VARCHAR(20) CHECK(Status IN ('Active', 'Completed')),
    AssignedInstructorID INTEGER NOT NULL,
    FOREIGN KEY (UserID) REFERENCES AppUser(UserID) ON DELETE CASCADE,
    FOREIGN KEY (AssignedInstructorID) REFERENCES Instructor(InstructorID)
);

CREATE TABLE Car (
    CarID INTEGER PRIMARY KEY AUTO_INCREMENT,
    LicensePlate VARCHAR(10) UNIQUE NOT NULL CHECK(
        LicensePlate REGEXP '^[A-Z]-[0-9]{2}-[A-Z]{3}$' OR
        LicensePlate REGEXP '^[A-Z]-[0-9]{3}-[A-Z]{3}$' OR
        LicensePlate REGEXP '^[A-Z]{2}-[0-9]{2}-[A-Z]{3}$' OR
        LicensePlate REGEXP '^[A-Z]{2}-[0-9]{3}-[A-Z]{3}$'
    ),
    Brand VARCHAR(20),
    Model VARCHAR(20),
    TransmissionType VARCHAR(20) CHECK(TransmissionType IN ('Manual', 'Automatic')),
    AssignedInstructorID INTEGER UNIQUE NOT NULL, -- 1-to-1 relationship
    FOREIGN KEY (AssignedInstructorID) REFERENCES Instructor(InstructorID)
);

CREATE TABLE Payment (
    PaymentID INTEGER PRIMARY KEY AUTO_INCREMENT,
    Amount DECIMAL(10,2) NOT NULL,
    PaymentDate DATE,
    PaymentMethod VARCHAR(15) CHECK(PaymentMethod IN ('Cash', 'Card')),
    Details VARCHAR(100),
    TraineeID INTEGER NOT NULL,
    FOREIGN KEY (TraineeID) REFERENCES Trainee(TraineeID)
);

CREATE TABLE Exam (
    ExamID INTEGER PRIMARY KEY AUTO_INCREMENT,
    ExamType VARCHAR(20) NOT NULL CHECK(ExamType IN ('Practical', 'Theoretical')),
    ScheduledDate DATE NOT NULL,
    Status VARCHAR(20) CHECK(Status IN ('Scheduled', 'Completed')),
    TraineeID INTEGER NOT NULL,
    FOREIGN KEY (TraineeID) REFERENCES Trainee(TraineeID)
);

CREATE TABLE Session (
    SessionID INTEGER PRIMARY KEY AUTO_INCREMENT,
    SessionType VARCHAR(20) NOT NULL CHECK(SessionType IN ('Practical', 'Theoretical')),
    StartDateTime TIMESTAMP NOT NULL,
    EndDateTime TIMESTAMP NOT NULL,
    Status VARCHAR(20) CHECK(Status IN ('Scheduled', 'Completed')),
    InstructorFeedback VARCHAR(255),
    InstructorID INTEGER NOT NULL,
    TraineeID INTEGER, -- NULL for theoretical group sessions
    FOREIGN KEY (InstructorID) REFERENCES Instructor(InstructorID),
    FOREIGN KEY (TraineeID) REFERENCES Trainee(TraineeID)
);

-- TRAINEE_SESSION (Many-to-Many Junction Table)
CREATE TABLE Trainee_Session (
    TraineeID INTEGER NOT NULL,
    SessionID INTEGER NOT NULL,
    PRIMARY KEY (TraineeID, SessionID),
    FOREIGN KEY (TraineeID) REFERENCES Trainee(TraineeID) ON DELETE CASCADE,
    FOREIGN KEY (SessionID) REFERENCES Session(SessionID) ON DELETE CASCADE
);

-- Password: "password123" (in production, these would be hashed with BCrypt)
INSERT INTO AppUser (Username, Password, Email, Role, IsActive) VALUES
    -- Administrators
    ('admin', 'password123', 'admin@drivingschool.com', 'ADMIN', TRUE),
    ('admin2', 'password123', 'admin2@drivingschool.com', 'ADMIN', TRUE),
    -- Instructors
    ('popescu.marian', 'password123', 'marian.popescu@drivingschool.com', 'INSTRUCTOR', TRUE),
    ('ionescu.alex', 'password123', 'alex.ionescu@drivingschool.com', 'INSTRUCTOR', TRUE),
    ('stanciu.vasile', 'password123', 'vasile.stanciu@drivingschool.com', 'INSTRUCTOR', TRUE),
    ('mihai.victor', 'password123', 'victor.mihai@drivingschool.com', 'INSTRUCTOR', TRUE),
    ('dragan.ionel', 'password123', 'ionel.dragan@drivingschool.com', 'INSTRUCTOR', TRUE),
    -- Trainees
    ('stanciu.andreea', 'password123', 'andreea.stanciu@email.com', 'TRAINEE', TRUE),
    ('mosu.alexia', 'password123', 'alexia.mosu@email.com', 'TRAINEE', TRUE),
    ('predescu.ionut', 'password123', 'ionut.predescu@email.com', 'TRAINEE', TRUE),
    ('ionescu.maria', 'password123', 'maria.ionescu@email.com', 'TRAINEE', TRUE),
    ('popa.andrei', 'password123', 'andrei.popa@email.com', 'TRAINEE', TRUE),
    ('georgescu.elena', 'password123', 'elena.georgescu@email.com', 'TRAINEE', TRUE),
    ('dumitrescu.vlad', 'password123', 'vlad.dumitrescu@email.com', 'TRAINEE', TRUE),
    ('stan.ioana', 'password123', 'ioana.stan@email.com', 'TRAINEE', TRUE);

INSERT INTO Administrator (UserID, FirstName, LastName, Phone) VALUES
    (1, 'Mihai', 'Popescu', '0721234567'),
    (2, 'Ana', 'Ionescu', '0732345678');

INSERT INTO Instructor (UserID, FirstName, LastName, Phone, HireDate) VALUES
    (3, 'Marian', 'Popescu', '0765865248', '2015-01-03'),
    (4, 'Alexandru', 'Ionescu', '0751256872', '2015-01-03'),
    (5, 'Vasile', 'Stanciu', '0757090125', '2015-01-03'),
    (6, 'Victor', 'Mihai', '0762458961', '2016-02-05'),
    (7, 'Ionel', 'Dragan', '0728445669', '2017-01-23');

INSERT INTO Trainee (UserID, FirstName, LastName, SSN, Address, Phone, EnrollmentDate, LicenseCategory, Status, AssignedInstructorID) VALUES
    (8, 'Andreea', 'Stanciu', '6040521430057', 'Strada 1907, 122', '0762387062', '2025-08-20', 'B', 'Active', 3),
    (9, 'Alexia', 'Mosu', '6041025485596', 'Strada Dunarii, 115', '0768123554', '2025-10-12', 'B', 'Active', 5),
    (10, 'Ionut', 'Predescu', '5000630786459', 'Mihai Bravu, 200', '0794125684', '2024-02-13', 'A', 'Completed', 1),
    (11, 'Maria', 'Ionescu', '6020512345678', 'Str. Libertății, 12', '0723344556', '2024-03-05', 'B', 'Completed', 2),
    (12, 'Andrei', 'Popa', '5011212345678', 'Bd. Unirii, 45', '0734455667', '2024-01-20', 'A', 'Completed', 1),
    (13, 'Elena', 'Georgescu', '6020709876543', 'Str. Mihai Eminescu, 9', '0745566778', '2025-02-28', 'B', 'Active', 3),
    (14, 'Vlad', 'Dumitrescu', '5010312345678', 'Str. Victoriei, 7', '0756677889', '2024-02-13', 'C', 'Completed', 4),
    (15, 'Ioana', 'Stan', '6020812345678', 'Str. Carol, 21', '0767788990', '2025-03-01', 'A1', 'Active', 2);

INSERT INTO Car (LicensePlate, Brand, Model, TransmissionType, AssignedInstructorID) VALUES
    ('B-12-ABC', 'Dacia', 'Logan', 'Manual', 1),
    ('CJ-345-XYZ', 'Renault', 'Clio', 'Automatic', 2),
    ('IF-78-DEF', 'Volkswagen', 'Golf', 'Manual', 3),
    ('TM-90-GHA', 'Ford', 'Focus', 'Automatic', 4),
    ('AR-23-JKL', 'Opel', 'Corsa', 'Manual', 5);

INSERT INTO Payment (Amount, PaymentDate, PaymentMethod, Details, TraineeID) VALUES
    (3200.00, '2025-08-21', 'Card', 'Full payment', 1),
    (2000.00, '2025-10-12', 'Cash', 'First installment', 2),
    (1000.00, '2024-02-13', 'Card', 'First installment', 3),
    (1800.00, '2024-07-01', 'Card', 'Second installment', 3),
    (3000.00, '2024-03-06', 'Cash', 'Full payment', 4),
    (1000.00, '2024-01-20', 'Cash', 'First installment', 5),
    (1200.00, '2024-08-12', 'Card', 'Second installment', 5),
    (3120.00, '2025-02-27', 'Cash', 'Full payment', 6),
    (1500.00, '2024-02-13', 'Card', 'First installment', 7),
    (1800.00, '2024-10-01', 'Cash', 'Second installment', 7),
    (900.00, '2025-04-01', 'Card', 'First installment', 8);

INSERT INTO Exam (ExamType, ScheduledDate, Status, TraineeID) VALUES
    ('Theoretical', '2025-12-15', 'Scheduled', 1),
    ('Theoretical', '2026-01-20', 'Scheduled', 2),
    ('Theoretical', '2024-06-10', 'Completed', 3),
    ('Practical', '2024-07-25', 'Completed', 3),
    ('Theoretical', '2024-06-01', 'Completed', 4),
    ('Practical', '2024-08-10', 'Completed', 4),
    ('Theoretical', '2024-04-18', 'Completed', 5),
    ('Practical', '2024-05-05', 'Completed', 5),
    ('Theoretical', '2025-04-30', 'Completed', 6),
    ('Practical', '2025-11-10', 'Scheduled', 6),
    ('Theoretical', '2024-04-18', 'Completed', 7),
    ('Practical', '2024-08-21', 'Completed', 7),
    ('Theoretical', '2025-06-14', 'Completed', 8),
    ('Practical', '2025-12-02', 'Scheduled', 8);

INSERT INTO Session (SessionType, StartDateTime, EndDateTime, Status, InstructorFeedback, InstructorID, TraineeID) VALUES
    ('Practical', '2025-08-21 09:00:00', '2025-08-21 10:30:00', 'Completed', 'Good progress', 3, 1),
    ('Practical', '2025-10-22 11:00:00', '2025-10-22 12:30:00', 'Completed', NULL, 5, 2),
    ('Practical', '2024-02-28 14:00:00', '2024-02-28 15:30:00', 'Completed', 'Very attentive', 1, 3),
    ('Practical', '2024-03-24 10:00:00', '2024-03-24 11:30:00', 'Completed', 'Progressed well', 2, 4),
    ('Practical', '2024-01-27 09:30:00', '2024-01-27 11:00:00', 'Completed', NULL, 1, 5),
    ('Practical', '2025-11-26 13:00:00', '2025-11-26 14:30:00', 'Scheduled', NULL, 3, 6),
    ('Practical', '2024-03-30 08:30:00', '2024-03-30 10:00:00', 'Completed', 'Excellent', 4, 7),
    ('Practical', '2025-04-25 14:00:00', '2025-04-25 15:30:00', 'Completed', NULL, 2, 8),
    ('Theoretical', '2025-09-01 09:00:00', '2025-09-01 11:00:00', 'Scheduled', NULL, 1, NULL),
    ('Theoretical', '2025-08-02 14:00:00', '2025-08-02 16:00:00', 'Completed', 'All trainees understood the lesson', 2, NULL),
    ('Theoretical', '2024-03-03 10:00:00', '2024-03-03 12:00:00', 'Completed', NULL, 3, NULL),
    ('Theoretical', '2025-11-04 13:00:00', '2025-11-04 15:00:00', 'Scheduled', NULL, 4, NULL);

INSERT INTO Trainee_Session (TraineeID, SessionID) VALUES
    (1, 9),
    (2, 9),
    (6, 9),
    (8, 9),
    (1, 10),
    (2, 10),
    (3, 11),
    (4, 11),
    (5, 11),
    (7, 11),
    (6, 12),
    (8, 12);
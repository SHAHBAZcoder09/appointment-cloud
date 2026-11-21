CREATE DATABASE IF NOT EXISTS appointment_db;
USE appointment_db;

CREATE TABLE IF NOT EXISTS t_patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    age INT
);

CREATE TABLE IF NOT EXISTS t_schedule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_name VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    schedule_date VARCHAR(20) NOT NULL,
    time_slot VARCHAR(30) NOT NULL,
    total_quota INT NOT NULL,
    available_quota INT NOT NULL,
    fee DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS t_appointment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_no VARCHAR(50) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    schedule_id INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO t_patient (username, password, name, phone, age)
VALUES ('Denji', '12345', 'Denji', '13800001111', 26);

INSERT INTO t_schedule (doctor_name, department, schedule_date, time_slot, total_quota, available_quota, fee)
VALUES
  ('Dr. Malik', 'Cardiology', '2026-11-22', '09:00-11:00', 10, 5, 50.00),
  ('Dr. Sharry', 'Dermatology', '2026-11-22', '14:00-16:00', 8, 3, 35.00),
  ('Dr. Nasir', 'Pediatrics', '2026-11-23', '09:00-11:00', 5, 1, 40.00);

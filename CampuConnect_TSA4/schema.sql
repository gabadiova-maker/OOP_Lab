-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 25, 2026 at 05:23 PM
-- Server version: 10.11.19-MariaDB
-- PHP Version: 8.3.37

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `campusconnect_tsa4_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `service_request`
--

CREATE TABLE `service_request` (
  `ticket_id` varchar(20) NOT NULL,
  `student_id` varchar(30) NOT NULL,
  `student_name` varchar(100) NOT NULL,
  `category` enum('ENROLLMENT','DOCUMENT','PAYMENT','ADVISING') NOT NULL,
  `priority` int(11) NOT NULL,
  `concern` varchar(255) NOT NULL,
  `estimated_days` int(11) NOT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ticket_sequence`
--

CREATE TABLE `ticket_sequence` (
  `id` int(11) NOT NULL,
  `next_ticket` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `service_request`
--
ALTER TABLE `service_request`
  ADD PRIMARY KEY (`ticket_id`);

--
-- Indexes for table `ticket_sequence`
--
ALTER TABLE `ticket_sequence`
  ADD PRIMARY KEY (`id`);

ALTER TABLE service_request
ADD CONSTRAINT chk_priority
CHECK (priority BETWEEN 1 AND 5);

ALTER TABLE service_request
ADD CONSTRAINT chk_estimated_days
CHECK (estimated_days >= 1);

INSERT INTO ticket_sequence (id, next_ticket)
VALUES (1, 1001);

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

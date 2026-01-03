-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: coursemgmt_test
-- ------------------------------------------------------
-- Server version	9.5.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ROLE_ADMIN'),(2,'ROLE_LECTURER'),(3,'ROLE_STUDENT');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt encoded',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` longtext COLLATE utf8mb4_unicode_ci,
  `expertise` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `linkedin` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `github` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `twitter` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_notification_enabled` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_enabled` tinyint(1) DEFAULT '0',
  `lock_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES 
(1,'admin','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','admin@example.com','Quản trị viên',NULL,'Quản trị hệ thống',NULL,NULL,NULL,NULL,NULL,'2025-12-08 00:54:39',1,NULL),
(2,'lecturer1','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','lecturer1@example.com','Nguyễn Văn A',NULL,'Giảng viên Lập trình Java','Java, Spring Boot','https://linkedin.com/in/nguyenvana','https://github.com/nguyenvana',NULL,NULL,'2025-12-08 00:54:39',1,NULL),
(3,'lecturer2','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','lecturer2@example.com','Trần Thị B',NULL,'Giảng viên Web Development','React, Node.js','https://linkedin.com/in/tranthib','https://github.com/tranthib',NULL,NULL,'2025-12-08 00:54:39',1,NULL),
(4,'student1','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','student1@example.com','Lê Văn C',NULL,'Học viên',NULL,NULL,NULL,NULL,NULL,'2025-12-08 00:54:39',1,NULL),
(5,'student2','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','student2@example.com','Phạm Thị D',NULL,'Học viên',NULL,NULL,NULL,NULL,NULL,'2025-12-08 00:54:39',1,NULL),
(6,'student3','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','student3@example.com','Hoàng Văn E',NULL,'Học viên',NULL,NULL,NULL,NULL,NULL,'2025-12-08 00:54:39',1,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,1),(2,2),(3,2),(4,3),(5,3),(6,3);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES 
(1,'Back-end','Các khóa học về lập trình phía server, API, database'),
(2,'Front-end','Các khóa học về giao diện người dùng, HTML, CSS, JavaScript'),
(3,'Full-stack','Các khóa học kết hợp cả Front-end và Back-end'),
(4,'Mobile Development','Phát triển ứng dụng di động iOS, Android, Flutter'),
(5,'Data Science','Khoa học dữ liệu, Machine Learning, AI'),
(6,'DevOps','Triển khai, CI/CD, Docker, Kubernetes'),
(7,'Database','Cơ sở dữ liệu SQL, NoSQL, thiết kế database'),
(8,'UI/UX Design','Thiết kế giao diện và trải nghiệm người dùng'),
(9,'Kiến thức nền tảng','Các kiến thức cơ bản về IT, lập trình cho người mới'),
(10,'Kỹ năng mềm','Kỹ năng làm việc, giao tiếp, quản lý dự án');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `price` double NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_duration_in_hours` int DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT 'DRAFT',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `instructor_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `is_featured` tinyint(1) DEFAULT '0',
  `is_published` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_courses_instructor` (`instructor_id`),
  KEY `idx_courses_category` (`category_id`),
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`instructor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `courses_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES 
(1,'Java Spring Boot Cơ bản','Khóa học về Spring Boot framework cho Java',500000,'https://files.f8.edu.vn/f8-prod/courses/7.png',40,'PUBLISHED','2025-11-08 00:54:39','2025-12-26 19:27:49',2,1,0,1),
(2,'React.js từ Zero đến Hero','Học React.js từ cơ bản đến nâng cao',600000,'https://files.f8.edu.vn/f8-prod/courses/7.png',50,'PUBLISHED','2025-11-13 00:54:39','2025-12-26 19:27:49',3,2,0,1),
(3,'Node.js Backend Development','Xây dựng backend với Node.js và Express',550000,'https://files.f8.edu.vn/f8-prod/courses/7.png',45,'PUBLISHED','2025-11-18 00:54:39','2025-12-26 19:27:49',3,1,0,1),
(4,'Python cho Data Science','Phân tích dữ liệu với Python',700000,'https://files.f8.edu.vn/f8-prod/courses/7.png',60,'PUBLISHED','2025-11-23 00:54:39','2025-12-26 19:06:16',2,5,0,1),
(5,'Flutter Mobile App','Phát triển ứng dụng di động với Flutter',650000,'https://files.f8.edu.vn/f8-prod/courses/7.png',55,'PUBLISHED','2025-11-28 00:54:39','2025-12-26 17:43:03',3,4,0,1),
(6,'Kiến Thức Nhập Môn IT','Để có cái nhìn tổng quan về ngành IT - Lập trình web các bạn nên xem các videos tại khóa này trước nhé.',0,'https://files.f8.edu.vn/f8-prod/courses/7.png',3,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 19:27:49',1,9,1,1),
(7,'Lập trình C++ cơ bản, nâng cao','Khóa học lập trình C++ từ cơ bản tới nâng cao dành cho người mới bắt đầu.',0,'https://files.f8.edu.vn/f8-prod/courses/21/63e1bcbaed1dd.png',10,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 19:27:49',1,9,1,1),
(8,'HTML CSS từ Zero đến Hero','Trong khóa này chúng ta sẽ cùng nhau xây dựng giao diện 2 trang web là The Band & Shopee.',0,'https://files.f8.edu.vn/f8-prod/courses/2.png',29,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 19:27:49',1,2,1,1),
(9,'Responsive Với Grid System','Trong khóa này chúng ta sẽ học về cách xây dựng giao diện web responsive với Grid System.',0,'https://files.f8.edu.vn/f8-prod/courses/3.png',6,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 19:27:49',1,2,1,1),
(10,'Lập Trình JavaScript Cơ Bản','Học Javascript cơ bản phù hợp cho người chưa từng học lập trình.',0,'https://files.f8.edu.vn/f8-prod/courses/1.png',24,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,2,0,1),
(11,'Lập Trình JavaScript Nâng Cao','Hiểu sâu hơn về cách Javascript hoạt động, tìm hiểu về IIFE, closure, reference types...',0,'https://files.f8.edu.vn/f8-prod/courses/12.png',8,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,2,0,1),
(12,'Làm việc với Terminal & Ubuntu','Sở hữu một Terminal hiện đại, mạnh mẽ trong tùy biến và học cách làm việc với Ubuntu.',0,'https://files.f8.edu.vn/f8-prod/courses/14/624faac11d109.png',4,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,6,0,1),
(13,'Xây Dựng Website với ReactJS','Khóa học ReactJS từ cơ bản tới nâng cao, làm dự án giống Tiktok.com.',0,'https://files.f8.edu.vn/f8-prod/courses/13/13.png',27,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,2,0,1),
(14,'Node & ExpressJS','Học Back-end với Node & ExpressJS framework, xây dựng RESTful API.',0,'https://files.f8.edu.vn/f8-prod/courses/6.png',12,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,1,0,1),
(15,'App \"Đừng Chạm Tay Lên Mặt\"','Xây dựng ứng dụng AI cảnh báo sờ tay lên mặt với ReactJS & Tensorflow.',0,'https://files.f8.edu.vn/f8-prod/courses/4/61a9e9e701506.png',1,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,5,0,1),
(16,'HTML CSS Pro','Khóa học HTML CSS Pro phù hợp cho cả người mới bắt đầu, lên tới 8 dự án trên Figma.',1299000,'https://files.f8.edu.vn/f8-prod/courses/15/62f13d2424a47.png',116,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,2,0,1),
(17,'JavaScript Pro','Khóa học JavaScript Pro này là nền tảng vững chắc để học tiếp React, Vue.js, Node.js.',1399000,'https://files.f8.edu.vn/f8-prod/courses/19/66aa28194b52b.png',49,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,2,0,1),
(18,'Ngôn ngữ Sass','Kiến thức về Sass trong khóa này bạn có thể áp dụng ngay vào các dự án của bạn.',299000,'https://files.f8.edu.vn/f8-prod/courses/27/64e184ee5d7a2.png',6,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,2,0,1),
(19,'Fullstack Web','Lớp học Fullstack Zoom Online chuyên sâu.',18900000,'https://files.f8.edu.vn/f8-prod/courses/31/67f4c93c28d4b.png',222,'PUBLISHED','2025-12-26 16:39:38','2025-12-26 16:39:38',1,3,0,1);
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapters`
--

DROP TABLE IF EXISTS `chapters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapters` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `position` int DEFAULT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapters`
--

LOCK TABLES `chapters` WRITE;
/*!40000 ALTER TABLE `chapters` DISABLE KEYS */;
INSERT INTO `chapters` VALUES 
(1,'Giới thiệu Spring Boot',1,1),
(2,'Spring Boot Core',2,1),
(3,'Spring Data JPA',3,1),
(4,'Spring Security',4,1),
(5,'React Fundamentals',1,2),
(6,'Components & Props',2,2),
(7,'State & Hooks',3,2),
(8,'Routing & Navigation',4,2),
(9,'Node.js Basics',1,3),
(10,'Express Framework',2,3),
(11,'Database Integration',3,3),
(12,'Python Basics',1,4),
(13,'NumPy & Pandas',2,4),
(14,'Data Visualization',3,4),
(15,'Flutter Introduction',1,5),
(16,'Widgets & Layouts',2,5),
(17,'State Management',3,5),
(18,'Chương 1: Giới thiệu tổng quan',1,6),
(19,'Chương 1: Giới thiệu tổng quan',1,7),
(20,'Chương 1: Giới thiệu tổng quan',1,8),
(21,'Chương 1: Giới thiệu tổng quan',1,9),
(22,'Chương 1: Giới thiệu tổng quan',1,10),
(23,'Chương 1: Giới thiệu tổng quan',1,11),
(24,'Chương 1: Giới thiệu tổng quan',1,12),
(25,'Chương 1: Giới thiệu tổng quan',1,13),
(26,'Chương 1: Giới thiệu tổng quan',1,14),
(27,'Chương 1: Giới thiệu tổng quan',1,15),
(28,'Chương 1: Giới thiệu tổng quan',1,16),
(29,'Chương 1: Giới thiệu tổng quan',1,17),
(30,'Chương 1: Giới thiệu tổng quan',1,18),
(31,'Chương 1: Giới thiệu tổng quan',1,19);
/*!40000 ALTER TABLE `chapters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lessons`
--

DROP TABLE IF EXISTS `lessons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lessons` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `video_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `document_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` longtext COLLATE utf8mb4_unicode_ci,
  `position` int DEFAULT NULL,
  `duration_in_minutes` int DEFAULT NULL,
  `chapter_id` bigint NOT NULL,
  `is_preview` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `chapter_id` (`chapter_id`),
  CONSTRAINT `lessons_ibfk_1` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lessons`
--

LOCK TABLES `lessons` WRITE;
/*!40000 ALTER TABLE `lessons` DISABLE KEYS */;
INSERT INTO `lessons` VALUES 
(1,'Bài 1: Tổng quan Spring Boot','VIDEO','/videos/intro.mp4',NULL,'Nội dung bài học về Spring Boot',1,30,1,1),
(2,'Bài 2: Cài đặt môi trường','TEXT','/videos/intro.mp4',NULL,'Hướng dẫn cài đặt JDK, Maven, IDE',2,20,1,1),
(3,'Bài 3: Dependency Injection','VIDEO','/videos/intro.mp4',NULL,'Tìm hiểu về DI trong Spring',1,45,2,1),
(4,'Bài 4: Configuration','TEXT','/videos/intro.mp4',NULL,'Cấu hình ứng dụng Spring Boot',2,25,2,1),
(5,'Bài 1: Giới thiệu React','VIDEO','/videos/intro.mp4',NULL,'Tổng quan về React.js',1,40,5,1),
(6,'Bài 2: JSX Syntax','TEXT','/videos/intro.mp4',NULL,'Học về JSX trong React',2,30,5,1),
(7,'Bài 1: Node.js là gì?','VIDEO','/videos/intro.mp4',NULL,'Giới thiệu Node.js',1,35,9,1),
(8,'Bài 2: NPM và Modules','TEXT','/videos/intro.mp4',NULL,'Quản lý packages với NPM',2,25,9,1),
(9,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,18,1),
(10,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,18,1),
(11,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,19,1),
(12,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,19,1),
(13,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,20,1),
(14,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,20,1),
(15,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,21,1),
(16,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,21,1),
(17,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,22,1),
(18,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,22,1),
(19,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,23,1),
(20,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,23,1),
(21,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,24,1),
(22,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,24,1),
(23,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,25,1),
(24,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,25,1),
(25,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,26,1),
(26,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,26,1),
(27,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,27,1),
(28,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,27,1),
(29,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,28,1),
(30,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,28,1),
(31,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,29,1),
(32,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,29,1),
(33,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,30,1),
(34,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,30,1),
(35,'Bài 1: Giới thiệu khóa học','VIDEO','/videos/intro.mp4',NULL,NULL,1,5,31,1),
(36,'Bài 2: Cài đặt môi trường','VIDEO','/videos/intro.mp4',NULL,NULL,2,10,31,1);
/*!40000 ALTER TABLE `lessons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enrolled_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `progress` double DEFAULT '0',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'IN_PROGRESS',
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_enrollment` (`user_id`,`course_id`),
  KEY `idx_enrollments_user` (`user_id`),
  KEY `idx_enrollments_course` (`course_id`),
  CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollments`
--

LOCK TABLES `enrollments` WRITE;
/*!40000 ALTER TABLE `enrollments` DISABLE KEYS */;
INSERT INTO `enrollments` VALUES 
(1,'2025-11-18 00:54:39',45.5,'IN_PROGRESS',4,1),
(2,'2025-11-23 00:54:39',100,'COMPLETED',4,2),
(3,'2025-11-28 00:54:39',30,'IN_PROGRESS',4,3),
(4,'2025-11-20 00:54:39',75,'IN_PROGRESS',5,1),
(5,'2025-11-26 00:54:39',100,'COMPLETED',5,4),
(6,'2025-11-30 00:54:39',20,'IN_PROGRESS',6,2),
(7,'2025-12-03 00:54:39',10,'IN_PROGRESS',6,5);
/*!40000 ALTER TABLE `enrollments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `certificates`
--

DROP TABLE IF EXISTS `certificates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `certificates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certificate_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pdf_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issued_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `enrollment_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `certificate_code` (`certificate_code`),
  UNIQUE KEY `enrollment_id` (`enrollment_id`),
  CONSTRAINT `certificates_ibfk_1` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `certificates`
--

LOCK TABLES `certificates` WRITE;
/*!40000 ALTER TABLE `certificates` DISABLE KEYS */;
INSERT INTO `certificates` VALUES 
(1,'CERT-2024-001','/certificates/cert-2024-001.pdf','2025-12-03 00:54:39',2),
(2,'CERT-2024-002','/certificates/cert-2024-002.pdf','2025-12-06 00:54:39',5);
/*!40000 ALTER TABLE `certificates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_progress`
--

DROP TABLE IF EXISTS `user_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_completed` tinyint(1) DEFAULT '0',
  `completed_at` datetime DEFAULT NULL,
  `enrollment_id` bigint NOT NULL,
  `lesson_id` bigint NOT NULL,
  `last_watched_time` int DEFAULT NULL,
  `total_duration` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_progress_enrollment` (`enrollment_id`),
  KEY `idx_user_progress_lesson` (`lesson_id`),
  CONSTRAINT `user_progress_ibfk_1` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_progress_ibfk_2` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_progress`
--

LOCK TABLES `user_progress` WRITE;
/*!40000 ALTER TABLE `user_progress` DISABLE KEYS */;
INSERT INTO `user_progress` VALUES 
(1,1,'2025-11-20 00:54:39',1,1,NULL,NULL),
(2,1,'2025-11-21 00:54:39',1,2,NULL,NULL),
(3,1,'2025-11-22 00:54:39',1,3,NULL,NULL),
(4,0,NULL,1,4,NULL,NULL),
(5,1,'2025-11-26 00:54:39',2,5,NULL,NULL),
(6,1,'2025-11-27 00:54:39',2,6,NULL,NULL),
(7,1,'2025-11-23 00:54:39',4,1,NULL,NULL),
(8,1,'2025-11-24 00:54:39',4,2,NULL,NULL),
(9,1,'2025-11-25 00:54:39',4,3,NULL,NULL),
(10,1,'2025-11-26 00:54:39',4,4,NULL,NULL);
/*!40000 ALTER TABLE `user_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `payment_gateway` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transaction_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_transactions_user` (`user_id`),
  KEY `idx_transactions_course` (`course_id`),
  KEY `idx_transactions_status` (`status`),
  CONSTRAINT `transactions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `transactions_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES 
(1,500000,'SUCCESS','VNPAY','VNPAY123456','2025-11-18 00:54:39',4,1),
(2,600000,'SUCCESS','MOMO','MOMO789012','2025-11-23 00:54:39',4,2),
(3,550000,'PENDING','VNPAY','VNPAY345678','2025-11-28 00:54:39',4,3),
(4,500000,'SUCCESS','VNPAY','VNPAY901234','2025-11-20 00:54:39',5,1),
(5,700000,'SUCCESS','MOMO','MOMO567890','2025-11-26 00:54:39',5,4),
(6,600000,'SUCCESS','VNPAY','VNPAY111222','2025-11-30 00:54:39',6,2),
(7,650000,'FAILED','MOMO','MOMO333444','2025-12-03 00:54:39',6,5);
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recommendations`
--

DROP TABLE IF EXISTS `recommendations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `generated_at` datetime(6) DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `recommendation_score` double DEFAULT NULL,
  `course_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnc0purpvedykwqq5b77vi9kik` (`course_id`),
  KEY `FK3c9w1lipqdutm65a9inevwfp0` (`user_id`),
  CONSTRAINT `FK3c9w1lipqdutm65a9inevwfp0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKnc0purpvedykwqq5b77vi9kik` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recommendations`
--

LOCK TABLES `recommendations` WRITE;
/*!40000 ALTER TABLE `recommendations` DISABLE KEYS */;
/*!40000 ALTER TABLE `recommendations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_token`
--

DROP TABLE IF EXISTS `password_reset_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg0guo4k8krgpwuagos61oc06j` (`token`),
  UNIQUE KEY `UKf90ivichjaokvmovxpnlm5nin` (`user_id`),
  CONSTRAINT `FK83nsrttkwkb6ym0anu051mtxn` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_token`
--

LOCK TABLES `password_reset_token` WRITE;
/*!40000 ALTER TABLE `password_reset_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `carts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `added_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_course` (`cart_id`,`course_id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint NOT NULL,
  `course_id` bigint DEFAULT NULL,
  `transaction_id` bigint DEFAULT NULL,
  `action_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Link để chuyển đến trang liên quan',
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user` (`user_id`),
  KEY `idx_notifications_course` (`course_id`),
  KEY `idx_notifications_transaction` (`transaction_id`),
  KEY `idx_notifications_is_read` (`is_read`),
  KEY `idx_notifications_created_at` (`created_at`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE SET NULL,
  CONSTRAINT `notifications_ibfk_3` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
-- Sample notification data
INSERT INTO `notifications` VALUES 
(1,'Chào mừng bạn đến với hệ thống! Hãy khám phá các khóa học ngay.','WELCOME',0,'2025-12-08 00:54:39',4,NULL,NULL),
(2,'Bạn đã đăng ký thành công khóa học Java Spring Boot Cơ bản.','COURSE_PURCHASED',1,'2025-11-18 00:54:39',4,1,1),
(3,'Bạn đã đăng ký thành công khóa học React.js từ Zero đến Hero.','COURSE_PURCHASED',1,'2025-11-23 00:54:39',4,2,2),
(4,'Chúc mừng! Bạn đã hoàn thành khóa học React.js từ Zero đến Hero và nhận được chứng chỉ.','COURSE_COMPLETED',0,'2025-12-03 00:54:39',4,2,NULL),
(5,'Chào mừng bạn đến với hệ thống! Hãy khám phá các khóa học ngay.','WELCOME',1,'2025-12-08 00:54:39',5,NULL,NULL),
(6,'Bạn đã đăng ký thành công khóa học Python cho Data Science.','COURSE_PURCHASED',1,'2025-11-26 00:54:39',5,4,5),
(7,'Chúc mừng! Bạn đã hoàn thành khóa học Python cho Data Science và nhận được chứng chỉ.','COURSE_COMPLETED',0,'2025-12-06 00:54:39',5,4,NULL);
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rating` int NOT NULL COMMENT 'Rating from 1 to 5 stars',
  `comment` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `course_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `instructor_reply` longtext COLLATE utf8mb4_unicode_ci COMMENT 'Instructor response to review',
  `replied_at` datetime DEFAULT NULL COMMENT 'When instructor replied',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_course_review` (`course_id`,`user_id`),
  KEY `idx_reviews_course` (`course_id`),
  KEY `idx_reviews_user` (`user_id`),
  KEY `idx_reviews_rating` (`rating`),
  KEY `idx_reviews_unreplied` (`course_id`, `instructor_reply`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` (`id`,`rating`,`comment`,`created_at`,`updated_at`,`course_id`,`user_id`,`instructor_reply`,`replied_at`) VALUES 
(1,5,'Khóa học rất hay và dễ hiểu! Giảng viên giải thích rất chi tiết.','2025-11-25 10:30:00','2025-11-25 10:30:00',1,4,'Cảm ơn bạn đã đánh giá! Rất vui khi khóa học hữu ích với bạn.','2025-11-26 09:00:00'),
(2,4,'Nội dung tốt, nhưng có một số phần hơi khó theo dõi.','2025-11-28 14:15:00','2025-11-28 14:15:00',2,4,'Cảm ơn góp ý! Mình sẽ cập nhật thêm ví dụ cho các phần khó hiểu.','2025-11-29 10:30:00'),
(3,5,'Tuyệt vời! Đã hoàn thành khóa học và học được rất nhiều.','2025-12-05 09:00:00','2025-12-05 09:00:00',2,5,NULL,NULL),
(4,4,'Khóa học có chất lượng tốt, phù hợp cho người mới bắt đầu.','2025-11-22 16:45:00','2025-11-22 16:45:00',1,5,'Cảm ơn bạn! Chúc bạn học tập tốt!','2025-11-23 08:00:00'),
(5,3,'Bình thường, mong giảng viên bổ sung thêm bài tập thực hành.','2025-12-01 11:20:00','2025-12-01 11:20:00',2,6,NULL,NULL);
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `newsletter_subscriptions`
--

DROP TABLE IF EXISTS `newsletter_subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `newsletter_subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `subscribed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint DEFAULT NULL COMMENT 'Null nếu là guest, có giá trị nếu là user đã đăng nhập',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_newsletter_user` (`user_id`),
  KEY `idx_newsletter_active` (`is_active`),
  CONSTRAINT `newsletter_subscriptions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `newsletter_subscriptions`
--

LOCK TABLES `newsletter_subscriptions` WRITE;
/*!40000 ALTER TABLE `newsletter_subscriptions` DISABLE KEYS */;
/*!40000 ALTER TABLE `newsletter_subscriptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('DIRECT','GROUP') NOT NULL DEFAULT 'DIRECT',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_message_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_updated_at` (`updated_at`),
  KEY `idx_last_message_at` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conversation_participants`
--

DROP TABLE IF EXISTS `conversation_participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation_participants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` enum('STUDENT','INSTRUCTOR') NOT NULL,
  `joined_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_read_at` datetime DEFAULT NULL,
  `is_muted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_participant` (`conversation_id`,`user_id`),
  KEY `idx_user_conversation` (`user_id`,`conversation_id`),
  CONSTRAINT `conversation_participants_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `conversation_participants_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_type` enum('TEXT','IMAGE','FILE','SYSTEM') DEFAULT 'TEXT',
  `file_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `is_edited` tinyint(1) DEFAULT '0',
  `edited_at` datetime DEFAULT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `deleted_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_created` (`conversation_id`,`created_at`),
  KEY `idx_sender` (`sender_id`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `message_reads`
--

DROP TABLE IF EXISTS `message_reads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_reads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `read_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_read` (`message_id`,`user_id`),
  KEY `idx_user_read` (`user_id`,`read_at`),
  CONSTRAINT `message_reads_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`) ON DELETE CASCADE,
  CONSTRAINT `message_reads_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-29

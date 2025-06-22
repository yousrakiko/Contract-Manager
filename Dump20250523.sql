-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bea
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `action_log`
--

DROP TABLE IF EXISTS `action_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `action_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action_type` varchar(255) NOT NULL,
  `contract_id` int DEFAULT NULL,
  `description` text,
  `new_value` text,
  `old_value` text,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `contract_id` (`contract_id`),
  CONSTRAINT `action_log_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`contract_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `action_log`
--

LOCK TABLES `action_log` WRITE;
/*!40000 ALTER TABLE `action_log` DISABLE KEYS */;
INSERT INTO `action_log` VALUES (1,'STATUS_UPDATE',137,'Contract automatically changed from Signed to Active.','Active','Signed','2025-05-23 12:25:06'),(2,'STATUS_UPDATE',138,'Contract automatically changed from Signed to Active.','Active','Signed','2025-05-23 12:25:06'),(3,'STATUS_UPDATE',145,'Contract automatically expired after end date.','Expired','Active','2025-05-23 13:51:00');
/*!40000 ALTER TABLE `action_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `approval_stages`
--

DROP TABLE IF EXISTS `approval_stages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval_stages` (
  `stage_code` varchar(50) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`stage_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approval_stages`
--

LOCK TABLES `approval_stages` WRITE;
/*!40000 ALTER TABLE `approval_stages` DISABLE KEYS */;
INSERT INTO `approval_stages` VALUES ('FINAL_APPROVAL','Final Approval Stage'),('INITIAL','Initial Submission'),('MANAGER_REVIEW','Manager Review Stage');
/*!40000 ALTER TABLE `approval_stages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id_Comment` int NOT NULL,
  `contract_id` int NOT NULL,
  `matricule` varchar(20) NOT NULL,
  `content` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_Comment`),
  KEY `contract_id_idx` (`contract_id`),
  KEY `matricule_idx` (`matricule`),
  CONSTRAINT `fk_contract_id` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`contract_id`),
  CONSTRAINT `fk_matricule` FOREIGN KEY (`matricule`) REFERENCES `users_info` (`matricule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contract_actions`
--

DROP TABLE IF EXISTS `contract_actions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contract_actions` (
  `action_code` varchar(50) NOT NULL,
  `description` varchar(255) NOT NULL,
  `stage_code` varchar(50) NOT NULL,
  PRIMARY KEY (`action_code`),
  KEY `stage_code` (`stage_code`),
  CONSTRAINT `contract_actions_ibfk_1` FOREIGN KEY (`stage_code`) REFERENCES `approval_stages` (`stage_code`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contract_actions`
--

LOCK TABLES `contract_actions` WRITE;
/*!40000 ALTER TABLE `contract_actions` DISABLE KEYS */;
INSERT INTO `contract_actions` VALUES ('active','Contract Running','FINAL_APPROVAL'),('approved','Approved by Manager','MANAGER_REVIEW'),('canceled','Canceled by User/Admin','FINAL_APPROVAL'),('draft','Contract in the making','INITIAL'),('expired','Expired Automatically','FINAL_APPROVAL'),('modified','Contract Modified','INITIAL'),('pending','Pending Approval','INITIAL'),('rejected','Rejected by Manager','MANAGER_REVIEW'),('signed','Signed by Responsible Party','MANAGER_REVIEW');
/*!40000 ALTER TABLE `contract_actions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contract_history`
--

DROP TABLE IF EXISTS `contract_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contract_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `contract_id` int NOT NULL,
  `performed_by` varchar(20) NOT NULL,
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `action_code` varchar(50) NOT NULL,
  PRIMARY KEY (`history_id`),
  KEY `contract_id` (`contract_id`),
  KEY `performed_by` (`performed_by`),
  KEY `fk_action_code` (`action_code`),
  CONSTRAINT `contract_history_ibfk_1` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`contract_id`) ON DELETE CASCADE,
  CONSTRAINT `contract_history_ibfk_2` FOREIGN KEY (`performed_by`) REFERENCES `users_info` (`matricule`),
  CONSTRAINT `fk_action_code` FOREIGN KEY (`action_code`) REFERENCES `contract_actions` (`action_code`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contract_history`
--

LOCK TABLES `contract_history` WRITE;
/*!40000 ALTER TABLE `contract_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `contract_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contracts`
--

DROP TABLE IF EXISTS `contracts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contracts` (
  `contract_id` int NOT NULL AUTO_INCREMENT,
  `contract_title` varchar(255) NOT NULL,
  `created_by` varchar(20) NOT NULL,
  `signed_by` varchar(20) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `is_signed` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `signed_at` timestamp NULL DEFAULT NULL,
  `starts_at` timestamp NULL DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `contractor_name` varchar(255) DEFAULT NULL,
  `type` enum('supply','service') NOT NULL,
  `approval_stage` varchar(50) DEFAULT NULL,
  `budget_amount` decimal(15,2) DEFAULT NULL,
  `guarantee_amount` decimal(15,2) DEFAULT NULL,
  `file_data` longblob,
  PRIMARY KEY (`contract_id`),
  KEY `created_by_idx` (`created_by`),
  KEY `signed_by_idx` (`signed_by`),
  KEY `fk_approval_stage` (`approval_stage`),
  KEY `fk_contract_status` (`status`),
  CONSTRAINT `created_by` FOREIGN KEY (`created_by`) REFERENCES `users_info` (`matricule`) ON UPDATE CASCADE,
  CONSTRAINT `fk_approval_stage` FOREIGN KEY (`approval_stage`) REFERENCES `approval_stages` (`stage_code`) ON UPDATE CASCADE,
  CONSTRAINT `fk_contract_status` FOREIGN KEY (`status`) REFERENCES `contract_actions` (`action_code`),
  CONSTRAINT `signed_by` FOREIGN KEY (`signed_by`) REFERENCES `users_info` (`matricule`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contracts`
--

LOCK TABLES `contracts` WRITE;
/*!40000 ALTER TABLE `contracts` DISABLE KEYS */;
INSERT INTO `contracts` VALUES (3,'Website Development Contract','342361','363274','A contract to develop a corporate website.','pending',0,'2025-04-06 09:30:00',NULL,NULL,NULL,'2025-10-06 00:00:00.000000','2025-05-22 19:34:19','Tech Solutions Inc.','supply','INITIAL',50000.00,45000.00,NULL),(10,'Consulting service agreement','342361','363274',NULL,'active',1,'2025-01-05 17:43:04','2025-01-10 17:43:04','2025-01-25 17:43:04',NULL,'2025-08-29 18:43:04.000000','2025-05-23 00:12:43','Tech Solutions Inc.','service','FINAL_APPROVAL',19000.00,15000.00,NULL),(90,'Software License Agreement','342361','363274','A contract for providing software licenses.','approved',1,'2025-05-04 17:43:04','2025-05-10 17:43:04','2025-05-25 17:43:04',NULL,'2025-11-02 18:43:04.000000','2025-05-23 00:12:43','Tech Solutions Inc.','service','MANAGER_REVIEW',22000.00,20000.00,NULL),(132,'Software License Agreement','342361','363274','A contract to develop a corporate website.','draft',0,'2025-05-05 17:43:04',NULL,NULL,NULL,'2025-08-29 18:43:04.000000','2025-05-22 19:34:19','Creative Works LLC.','service','INITIAL',NULL,NULL,NULL),(136,'Generated Contract 136','342361','363274','Auto-generated contract about supply services.','Expired',1,'2025-04-13 09:00:00','2025-04-14 09:00:00','2025-04-30 09:00:00',NULL,'2025-05-15 00:00:00.000000','2025-05-23 02:00:01','Nova Technologies','supply','FINAL_APPROVAL',15000.00,13000.00,NULL),(137,'Generated Contract 137','342361','363274','Auto-generated contract about service services.','Active',1,'2025-03-14 09:00:00','2025-03-16 09:00:00','2025-04-10 09:00:00',NULL,'2025-08-18 00:00:00.000000','2025-05-23 11:25:04','Creative Works LLC.','service','FINAL_APPROVAL',12000.00,10000.00,NULL),(138,'Generated Contract 138','342361','363274','Auto-generated contract about supply services.','Active',1,'2025-02-12 09:00:00','2025-02-15 09:00:00','2025-03-20 09:00:00',NULL,'2025-07-07 00:00:00.000000','2025-05-23 11:25:04','Nova Technologies','supply','FINAL_APPROVAL',20000.00,15000.00,NULL),(145,'Consulting service agreement','342361','363274',NULL,'Expired',1,'2025-01-05 17:43:04','2025-01-10 17:43:04','2025-01-25 17:43:04',NULL,'2025-05-22 00:00:00.000000','2025-05-23 12:51:00','Tech Solutions Inc.','service','FINAL_APPROVAL',19000.00,15000.00,_binary '?'),(153,'Generated Contract 153','342361','363274','Auto-generated contract about service services.','approved',1,'2025-02-21 09:00:00','2025-02-24 09:00:00','2025-03-20 09:00:00',NULL,'2025-05-26 10:00:00.000000','2025-05-23 00:12:43','Nova Technologies','service','MANAGER_REVIEW',22000.00,22000.00,NULL),(154,'Generated Contract 154','342361','363274','Auto-generated contract about service services.','rejected',0,'2025-03-05 09:00:00',NULL,NULL,'2025-03-09 09:00:00','2025-05-24 10:00:00.000000','2025-05-22 19:34:19','NextGen Ltd.','supply','MANAGER_REVIEW',15000.00,15000.00,NULL),(155,'Generated Contract 155','342361','363274','Auto-generated contract about supply services.','approved',1,'2025-04-13 09:00:00','2025-04-16 09:00:00','2025-05-21 09:00:00',NULL,'2025-05-21 10:00:00.000000','2025-05-23 00:12:43','Innova Corp.','supply','MANAGER_REVIEW',15000.00,14000.00,NULL);
/*!40000 ALTER TABLE `contracts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `recipient_id` varchar(20) NOT NULL,
  `message` varchar(255) NOT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `seen` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `contract_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `recipient_id` (`recipient_id`),
  KEY `fk_notification_contract_id` (`contract_id`),
  CONSTRAINT `fk_notification_contract_id` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`contract_id`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`recipient_id`) REFERENCES `users_info` (`matricule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `structure`
--

DROP TABLE IF EXISTS `structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `structure` (
  `struct_id` int NOT NULL,
  `department_name` varchar(100) NOT NULL,
  `departement_name` varchar(255) NOT NULL,
  PRIMARY KEY (`struct_id`),
  UNIQUE KEY `UK3llel21fch819f1ycawsn0nxm` (`departement_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `structure`
--

LOCK TABLES `structure` WRITE;
/*!40000 ALTER TABLE `structure` DISABLE KEYS */;
INSERT INTO `structure` VALUES (1,'Departement systéme d\'information','');
/*!40000 ALTER TABLE `structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_info`
--

DROP TABLE IF EXISTS `users_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_info` (
  `matricule` varchar(20) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `user_mail` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role_id` int NOT NULL,
  `struct_id` int NOT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  PRIMARY KEY (`matricule`),
  UNIQUE KEY `user_mail_UNIQUE` (`user_mail`),
  KEY `role_id_idx` (`role_id`),
  KEY `id_struct_idx` (`struct_id`),
  CONSTRAINT `role_id` FOREIGN KEY (`role_id`) REFERENCES `users_roles` (`role_id`),
  CONSTRAINT `struct_id` FOREIGN KEY (`struct_id`) REFERENCES `structure` (`struct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_info`
--

LOCK TABLES `users_info` WRITE;
/*!40000 ALTER TABLE `users_info` DISABLE KEYS */;
INSERT INTO `users_info` VALUES ('342361','Yousra','Oudina','yousra@gmail.com','$2a$12$d.RokogfDofkOq4rKwrAlOC6EBAVh2dc2MDXtpwRfRahYPc9LPm46',1,1,'ACTIVE'),('363274','Malak','Saidi','malak@gmail.com','$2a$12$aofrESmftMhRHpaQYdUV6esbt5qzaCY0AbaMbdpp8H/8.GUeZQsGy',2,1,'ACTIVE'),('392576','Ryad','Hasn','ryad@gmail.com','$2a$12$NKYHoCfggyM4pUsAJIFvduGxm7l6PgTr5rBlz1R.RaznLs5kpAaia',3,1,'ACTIVE');
/*!40000 ALTER TABLE `users_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_roles`
--

DROP TABLE IF EXISTS `users_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_roles` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_name_UNIQUE` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_roles`
--

LOCK TABLES `users_roles` WRITE;
/*!40000 ALTER TABLE `users_roles` DISABLE KEYS */;
INSERT INTO `users_roles` VALUES (3,'ADMIN'),(1,'VALIDATOR1'),(2,'VALIDATOR2');
/*!40000 ALTER TABLE `users_roles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-23 14:28:49

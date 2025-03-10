create schema creditcard;
use creditcard;

-- creditcard.credit_card_app_request definition

CREATE TABLE `credit_card_app_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(256) NOT NULL,
  `created_time` bigint(20) NOT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `updated_time` bigint(20) DEFAULT NULL,
  `emirates_id` varchar(256) NOT NULL,
  `name` varchar(256) DEFAULT NULL,
  `mobile_number` varchar(32) DEFAULT NULL,
  `nationality` varchar(32) DEFAULT NULL,
  `address` varchar(4096) DEFAULT NULL,
  `annual_income` decimal(36,9) DEFAULT NULL,
  `employment_details` text,
  `requested_credit_limit` decimal(36,9) DEFAULT NULL,
  `idempotent_id` varchar(256) DEFAULT NULL,
  `status` varchar(16) NOT NULL,
  `file_id` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- creditcard.score_board definition

CREATE TABLE `score_board` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(256) NOT NULL,
  `created_time` bigint(20) NOT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `updated_time` bigint(20) DEFAULT NULL,
  `emirates_id` varchar(256) NOT NULL,
  `identity_verified` tinyint(1) DEFAULT NULL,
  `employment_submitted` tinyint(1) DEFAULT NULL,
  `employment_verified` tinyint(1) DEFAULT NULL,
  `compliance_submitted` tinyint(1) DEFAULT NULL,
  `compliance_checked` tinyint(1) DEFAULT NULL,
  `risk_submitted` tinyint(1) DEFAULT NULL,
  `risk_percentage` decimal(36,9) DEFAULT NULL,
  `behavior_submitted` tinyint(1) DEFAULT NULL,
  `behavior_percentage` decimal(36,9) DEFAULT NULL,
  `total_score` decimal(36,9) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


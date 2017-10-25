DROP TABLE IF EXISTS `server_logging`.`blockedip`;
DROP TABLE IF EXISTS `server_logging`.`logfile`;

DROP SCHEMA IF EXISTS `server_logging`;
CREATE SCHEMA IF NOT EXISTS `server_logging`;

CREATE  TABLE IF NOT EXISTS `server_logging`.`logfile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `rqdate` DATETIME NULL ,
  `ip` VARCHAR(15) NULL ,
  `request` VARCHAR(45) NULL ,
  `status` VARCHAR(3) NULL ,
  `user_agent` VARCHAR(256) NULL ,
  PRIMARY KEY (`id`) );
  
CREATE  TABLE IF NOT EXISTS `server_logging`.`blockedip` (
  `id` BIGINT NOT NULL AUTO_INCREMENT ,
  `ip` VARCHAR(15) NULL ,
  `comments` VARCHAR(256) NULL ,
  PRIMARY KEY (`id`) );





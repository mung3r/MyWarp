-- -----------------------------------------------------
-- Table `${schema}`.`player`
-- -----------------------------------------------------
CREATE TABLE `${schema}`.`player` (
  `player_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `uuid`      BINARY(16)   NOT NULL,
  PRIMARY KEY (`player_id`),
  UNIQUE INDEX `player_uuid_uq` (`uuid`)
)
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `${schema}`.`world`
-- -----------------------------------------------------
CREATE TABLE `${schema}`.`world` (
  `world_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `uuid`     BINARY(16)   NOT NULL,
  PRIMARY KEY (`world_id`),
  UNIQUE INDEX `world_uuid_uq` (`uuid`)
)
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `${schema}`.`group`
-- -----------------------------------------------------
CREATE TABLE `${schema}`.`group` (
  `group_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`     VARCHAR(32)  NOT NULL,
  PRIMARY KEY (`group_id`),
  UNIQUE INDEX `group_name_uq` (`name`)
)
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `${schema}`.`warp`
-- -----------------------------------------------------
CREATE TABLE `${schema}`.`warp` (
  `warp_id`         INT UNSIGNED       NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(32)
                    CHARACTER SET 'utf8'
                    COLLATE 'utf8_bin' NOT NULL,
  `player_id`       INT UNSIGNED       NOT NULL,
  `x`               DOUBLE             NOT NULL,
  `y`               DOUBLE             NOT NULL,
  `z`               DOUBLE             NOT NULL,
  `pitch`           FLOAT              NOT NULL,
  `yaw`             FLOAT              NOT NULL,
  `world_id`        INT UNSIGNED       NOT NULL,
  `creation_date`   DATETIME           NOT NULL,
  `type`            TINYINT UNSIGNED   NOT NULL,
  `visits`          INT UNSIGNED       NOT NULL DEFAULT 0,
  `welcome_message` TINYTEXT           NULL     DEFAULT NULL,
  PRIMARY KEY (`warp_id`),
  UNIQUE INDEX `warp_name_uq` (`name`),
  INDEX `warp_player_id_idx` (`player_id`),
  INDEX `warp_world_id_idx` (`world_id`),
  CONSTRAINT `warp_player_id_fk`
  FOREIGN KEY (`player_id`)
  REFERENCES `${schema}`.`player` (`player_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `warp_world_id_fk`
  FOREIGN KEY (`world_id`)
  REFERENCES `${schema}`.`world` (`world_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `${schema}`.`warp_player_map`
-- -----------------------------------------------------
CREATE TABLE `${schema}`.`warp_player_map` (
  `warp_id`   INT UNSIGNED NOT NULL,
  `player_id` INT UNSIGNED NOT NULL,
  INDEX `warp_player_map_player_id_idx` (`player_id`),
  INDEX `warp_player_map_warp_id_idx` (`warp_id`),
  PRIMARY KEY (`warp_id`, `player_id`),
  CONSTRAINT `warp_player_map_player_id_fk`
  FOREIGN KEY (`player_id`)
  REFERENCES `${schema}`.`player` (`player_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `warp_player_map_warp_id_fk`
  FOREIGN KEY (`warp_id`)
  REFERENCES `${schema}`.`warp` (`warp_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `${schema}`.`warp_group_map`
-- -----------------------------------------------------
CREATE TABLE `${schema}`.`warp_group_map` (
  `warp_id`  INT UNSIGNED NOT NULL,
  `group_id` INT UNSIGNED NOT NULL,
  INDEX `warp_group_map_group_id_idx` (`group_id`),
  INDEX `warp_group_map_warp_id_idx` (`warp_id`),
  PRIMARY KEY (`warp_id`, `group_id`),
  CONSTRAINT `warp_group_map_group_id_fk`
  FOREIGN KEY (`group_id`)
  REFERENCES `${schema}`.`group` (`group_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `warp_group_map_warp_id_fk`
  FOREIGN KEY (`warp_id`)
  REFERENCES `${schema}`.`warp` (`warp_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
)
  ENGINE = InnoDB;

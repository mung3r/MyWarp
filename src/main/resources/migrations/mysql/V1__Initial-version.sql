-- -----------------------------------------------------
-- Table `player`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `player` (
  `player-id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `player` BINARY(16) NOT NULL,
  PRIMARY KEY (`player-id`),
  UNIQUE INDEX `U_player` (`player` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `world`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `world` (
  `world-id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `world` BINARY(16) NOT NULL,
  PRIMARY KEY (`world-id`),
  UNIQUE INDEX `U_world` (`world` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `warp`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `warp` (
  `warp-id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(32) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL,
  `player-id` INT UNSIGNED NOT NULL,
  `x` DOUBLE NOT NULL,
  `y` DOUBLE NOT NULL,
  `z` DOUBLE NOT NULL,
  `pitch` FLOAT NOT NULL,
  `yaw` FLOAT NOT NULL,
  `world-id` INT UNSIGNED NOT NULL,
  `creation-date` DATETIME NOT NULL,
  `type` TINYINT UNSIGNED NOT NULL,
  `visits` INT UNSIGNED NOT NULL DEFAULT 0,
  `fee` DOUBLE UNSIGNED NULL DEFAULT NULL,
  `welcome-message` TINYTEXT NULL DEFAULT NULL,
  UNIQUE INDEX `U_name` (`name` ASC),
  PRIMARY KEY (`warp-id`),
  INDEX `fk_warp_player_idx` (`player-id` ASC),
  INDEX `fk_warp_world1_idx` (`world-id` ASC),
  CONSTRAINT `fk_warp_player`
    FOREIGN KEY (`player-id`)
    REFERENCES `player` (`player-id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_warp_world1`
    FOREIGN KEY (`world-id`)
    REFERENCES `world` (`world-id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `group`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `group` (
  `group-id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `group` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`group-id`),
  UNIQUE INDEX `U_group` (`group` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `warp2player`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `warp2player` (
  `player-id` INT UNSIGNED NOT NULL,
  `warp-id` INT UNSIGNED NOT NULL,
  INDEX `fk_table1_player1_idx` (`player-id` ASC),
  INDEX `fk_table1_warp1_idx` (`warp-id` ASC),
  PRIMARY KEY (`player-id`, `warp-id`),
  CONSTRAINT `fk_table1_player1`
    FOREIGN KEY (`player-id`)
    REFERENCES `player` (`player-id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_table1_warp1`
    FOREIGN KEY (`warp-id`)
    REFERENCES `warp` (`warp-id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `warp2group`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `warp2group` (
  `group-id` INT UNSIGNED NOT NULL,
  `warp-id` INT UNSIGNED NOT NULL,
  INDEX `fk_table1_group1_idx` (`group-id` ASC),
  INDEX `fk_table1_warp2_idx` (`warp-id` ASC),
  PRIMARY KEY (`group-id`, `warp-id`),
  CONSTRAINT `fk_table1_group1`
    FOREIGN KEY (`group-id`)
    REFERENCES `group` (`group-id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_table1_warp2`
    FOREIGN KEY (`warp-id`)
    REFERENCES `warp` (`warp-id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS `player`(
  `player-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`player-id`>=0),
  `player` BINARY(16) NOT NULL,
  CONSTRAINT `U_player`
    UNIQUE(`player`)
);
CREATE TABLE IF NOT EXISTS `world`(
  `world-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`world-id`>=0),
  `world` BINARY(16) NOT NULL,
  CONSTRAINT `U_world`
    UNIQUE(`world`)
);
CREATE TABLE IF NOT EXISTS `group`(
  `group-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`group-id`>=0),
  `group` VARCHAR(32) NOT NULL,
  CONSTRAINT `U_group`
    UNIQUE(`group`)
);
CREATE TABLE IF NOT EXISTS `warp`(
  `warp-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`warp-id`>=0),
  `name` VARCHAR(32) NOT NULL,
  `player-id` INTEGER NOT NULL CHECK(`player-id`>=0),
  `x` DOUBLE NOT NULL,
  `y` DOUBLE NOT NULL,
  `z` DOUBLE NOT NULL,
  `pitch` FLOAT NOT NULL,
  `yaw` FLOAT NOT NULL,
  `world-id` INTEGER NOT NULL CHECK(`world-id`>=0),
  `creation-date` DATETIME NOT NULL,
  `type` INTEGER NOT NULL CHECK(`type`>=0),
  `visits` INTEGER NOT NULL CHECK(`visits`>=0) DEFAULT 0,
  `fee` DOUBLE DEFAULT NULL,
  `welcome-message` TINYTEXT DEFAULT NULL,
  CONSTRAINT `U_name`
    UNIQUE(`name`),
  CONSTRAINT `fk_warp_player`
    FOREIGN KEY(`player-id`)
    REFERENCES `player`(`player-id`),
  CONSTRAINT `fk_warp_world1`
    FOREIGN KEY(`world-id`)
    REFERENCES `world`(`world-id`)
);
CREATE INDEX IF NOT EXISTS `warp.fk_warp_player_idx` ON `warp`(`player-id`);
CREATE INDEX IF NOT EXISTS `warp.fk_warp_world1_idx` ON `warp`(`world-id`);
CREATE TABLE IF NOT EXISTS `warp2player`(
  `player-id` INTEGER NOT NULL CHECK(`player-id`>=0),
  `warp-id` INTEGER NOT NULL CHECK(`warp-id`>=0),
  PRIMARY KEY(`player-id`,`warp-id`),
  CONSTRAINT `fk_table1_player1`
    FOREIGN KEY(`player-id`)
    REFERENCES `player`(`player-id`),
  CONSTRAINT `fk_table1_warp1`
    FOREIGN KEY(`warp-id`)
    REFERENCES `warp`(`warp-id`)
);
CREATE INDEX IF NOT EXISTS `warp2player.fk_table1_player1_idx` ON `warp2player`(`player-id`);
CREATE INDEX IF NOT EXISTS `warp2player.fk_table1_warp1_idx` ON `warp2player`(`warp-id`);
CREATE TABLE IF NOT EXISTS `warp2group`(
  `group-id` INTEGER NOT NULL CHECK(`group-id`>=0),
  `warp-id` INTEGER NOT NULL CHECK(`warp-id`>=0),
  PRIMARY KEY(`group-id`,`warp-id`),
  CONSTRAINT `fk_table1_group1`
    FOREIGN KEY(`group-id`)
    REFERENCES `group`(`group-id`),
  CONSTRAINT `fk_table1_warp2`
    FOREIGN KEY(`warp-id`)
    REFERENCES `warp`(`warp-id`)
);
CREATE INDEX IF NOT EXISTS `warp2group.fk_table1_group1_idx` ON `warp2group`(`group-id`);
CREATE INDEX IF NOT EXISTS `warp2group.fk_table1_warp2_idx` ON `warp2group`(`warp-id`);

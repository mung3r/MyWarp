-- -----------------------------------------------------
-- Table `${schema}`.`warp`
-- -----------------------------------------------------
ALTER TABLE `${schema}`.`warp`
DROP FOREIGN KEY `warp_player_id_fk`;

ALTER TABLE `${schema}`.`warp`
ADD CONSTRAINT `warp_player_id_fk`
FOREIGN KEY (`player_id`)
REFERENCES `${schema}`.`player` (`player_id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `${schema}`.`warp`
DROP FOREIGN KEY `warp_world_id_fk`;

ALTER TABLE `${schema}`.`warp`
ADD CONSTRAINT `warp_world_id_fk`
FOREIGN KEY (`world_id`)
REFERENCES `${schema}`.`world` (`world_id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- -----------------------------------------------------
-- Table `${schema}`.`warp_player_map`
-- -----------------------------------------------------
ALTER TABLE `${schema}`.`warp_player_map`
DROP FOREIGN KEY `warp_player_map_player_id_fk`;

ALTER TABLE `${schema}`.`warp_player_map`
ADD CONSTRAINT `warp_player_map_player_id_fk`
FOREIGN KEY (`player_id`)
REFERENCES `${schema}`.`player` (`player_id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `${schema}`.`warp_player_map`
DROP FOREIGN KEY `warp_player_map_warp_id_fk`;

ALTER TABLE `warp_player_map`
ADD CONSTRAINT `warp_player_map_warp_id_fk`
FOREIGN KEY (`warp_id`)
REFERENCES `${schema}`.`warp` (`warp_id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- -----------------------------------------------------
-- Table `${schema}`.`warp_group_map`
-- -----------------------------------------------------
ALTER TABLE `${schema}`.`warp_group_map`
DROP FOREIGN KEY `warp_group_map_group_id_fk`;

ALTER TABLE `${schema}`.`warp_group_map`
ADD CONSTRAINT `warp_group_map_group_id_fk`
FOREIGN KEY (`group_id`)
REFERENCES `${schema}`.`group` (`group_id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `${schema}`.`warp_group_map`
DROP FOREIGN KEY `warp_group_map_warp_id_fk`;

ALTER TABLE `${schema}`.`warp_group_map`
ADD CONSTRAINT `warp_group_map_warp_id_fk`
FOREIGN KEY (`warp_id`)
REFERENCES `${schema}`.`warp` (`warp_id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- -----------------------------------------------------
-- Table "${schema}"."player"
-- -----------------------------------------------------
CREATE TABLE "${schema}"."player" (
  "player_id" IDENTITY,
  "uuid"      UUID NOT NULL UNIQUE
);

-- -----------------------------------------------------
-- Table "${schema}"."world"
-- -----------------------------------------------------
CREATE TABLE "${schema}"."world" (
  "world_id" IDENTITY,
  "uuid"     UUID NOT NULL UNIQUE
);

-- -----------------------------------------------------
-- Table "${schema}"."group"
-- -----------------------------------------------------
CREATE TABLE "${schema}"."group" (
  "group_id" IDENTITY,
  "name"     VARCHAR(32) NOT NULL UNIQUE
);

-- -----------------------------------------------------
-- Table "${schema}"."warp"
-- -----------------------------------------------------
CREATE TABLE "${schema}"."warp" (
  "warp_id"         IDENTITY,
  "name"            VARCHAR(32) NOT NULL,
  "player_id"       BIGINT,
  "x"               DOUBLE      NOT NULL,
  "y"               DOUBLE      NOT NULL,
  "z"               DOUBLE      NOT NULL,
  "pitch"           FLOAT       NOT NULL,
  "yaw"             FLOAT       NOT NULL,
  "world_id"        BIGINT,
  "creation_date"   DATETIME    NOT NULL,
  "type"            TINYINT     NOT NULL,
  "visits"          INT         NOT NULL DEFAULT '0',
  "welcome_message" VARCHAR(255)         DEFAULT NULL,
  FOREIGN KEY ("player_id")
  REFERENCES "${schema}"."player" ("player_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  FOREIGN KEY ("world_id")
  REFERENCES "${schema}"."world" ("world_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
CREATE INDEX "warp_player_id_idx"
  ON "${schema}"."warp" ("player_id");
CREATE INDEX "warp_world_id_idx"
  ON "${schema}"."warp" ("world_id");

-- -----------------------------------------------------
-- Table "${schema}"."warp_player_map"
-- -----------------------------------------------------
CREATE TABLE "${schema}"."warp_player_map" (
  "warp_id"   BIGINT,
  "player_id" BIGINT,
  PRIMARY KEY ("warp_id", "player_id"),
  FOREIGN KEY ("player_id")
  REFERENCES "${schema}"."player" ("player_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  FOREIGN KEY ("warp_id")
  REFERENCES "${schema}"."warp" ("warp_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
CREATE INDEX "warp_player_map_player_id_idx"
  ON "${schema}"."warp_player_map" ("player_id");
CREATE INDEX "warp_player_map_warp_id_idx"
  ON "${schema}"."warp_player_map" ("warp_id");

-- -----------------------------------------------------
-- Table "${schema}"."warp_group_map"
-- -----------------------------------------------------
CREATE TABLE "${schema}"."warp_group_map" (
  "warp_id"  BIGINT,
  "group_id" BIGINT,
  PRIMARY KEY ("warp_id", "group_id"),
  FOREIGN KEY ("group_id")
  REFERENCES "${schema}"."group" ("group_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  FOREIGN KEY ("warp_id")
  REFERENCES "${schema}"."warp" ("warp_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
CREATE INDEX "warp_group_map_group_id_idx"
  ON "${schema}"."warp_group_map" ("group_id");
CREATE INDEX "warp_group_map_warp_id_idx"
  ON "${schema}"."warp_group_map" ("warp_id");

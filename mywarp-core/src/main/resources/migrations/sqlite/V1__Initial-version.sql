-- -----------------------------------------------------
-- Table "player"
-- -----------------------------------------------------
CREATE TABLE "player" (
  "player_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK ("player_id" >= 0),
  "uuid"      BINARY(16)                        NOT NULL,
  CONSTRAINT "player_uuid_uq"
  UNIQUE ("uuid")
);

-- -----------------------------------------------------
-- Table "world"
-- -----------------------------------------------------
CREATE TABLE "world" (
  "world_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK ("world_id" >= 0),
  "uuid"     BINARY(16)                        NOT NULL,
  CONSTRAINT "world_uuid_uq"
  UNIQUE ("uuid")
);

-- -----------------------------------------------------
-- Table "group"
-- -----------------------------------------------------
CREATE TABLE "group" (
  "group_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK ("group_id" >= 0),
  "name"     VARCHAR(32)                       NOT NULL,
  CONSTRAINT "group_name_uq"
  UNIQUE ("name")
);

-- -----------------------------------------------------
-- Table "warp"
-- -----------------------------------------------------
CREATE TABLE "warp" (
  "warp_id"         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK ("warp_id" >= 0),
  "name"            VARCHAR(32)                       NOT NULL,
  "player_id"       INTEGER                           NOT NULL CHECK ("player_id" >= 0),
  "x"               DOUBLE                            NOT NULL,
  "y"               DOUBLE                            NOT NULL,
  "z"               DOUBLE                            NOT NULL,
  "pitch"           FLOAT                             NOT NULL,
  "yaw"             FLOAT                             NOT NULL,
  "world_id"        INTEGER                           NOT NULL CHECK ("world_id" >= 0),
  "creation_date"   DATETIME                          NOT NULL,
  "type"            INTEGER                           NOT NULL CHECK ("type" >= 0),
  "visits"          INTEGER                           NOT NULL CHECK ("visits" >= 0) DEFAULT 0,
  "welcome_message" TINYTEXT                                                         DEFAULT NULL,
  CONSTRAINT "warp_name_uq"
  UNIQUE ("name"),
  CONSTRAINT "warp_player_id_fk"
  FOREIGN KEY ("player_id")
  REFERENCES "player" ("player_id"),
  CONSTRAINT "warp_world_id_fk"
  FOREIGN KEY ("world_id")
  REFERENCES "world" ("world_id")
);
CREATE INDEX "warp.warp_player_id_idx" ON "warp" ("player_id");
CREATE INDEX "warp.warp_world_id_idx" ON "warp" ("world_id");

-- -----------------------------------------------------
-- Table "warp_player_map"
-- -----------------------------------------------------
CREATE TABLE "warp_player_map" (
  "warp_id"   INTEGER NOT NULL CHECK ("warp_id" >= 0),
  "player_id" INTEGER NOT NULL CHECK ("player_id" >= 0),
  PRIMARY KEY ("warp_id", "player_id"),
  CONSTRAINT "warp_player_map_player_id_fk"
  FOREIGN KEY ("player_id")
  REFERENCES "player" ("player_id"),
  CONSTRAINT "warp_player_map_warp_id_fk"
  FOREIGN KEY ("warp_id")
  REFERENCES "warp" ("warp_id")
);
CREATE INDEX "warp_player_map.warp_player_map_player_id_idx" ON "warp_player_map" ("player_id");
CREATE INDEX "warp_player_map.warp_player_map_warp_id_idx" ON "warp_player_map" ("warp_id");

-- -----------------------------------------------------
-- Table "warp_group_map"
-- -----------------------------------------------------
CREATE TABLE "warp_group_map" (
  "warp_id"  INTEGER NOT NULL CHECK ("warp_id" >= 0),
  "group_id" INTEGER NOT NULL CHECK ("group_id" >= 0),
  PRIMARY KEY ("warp_id", "group_id"),
  CONSTRAINT "warp_group_map_group_id_fk"
  FOREIGN KEY ("group_id")
  REFERENCES "group" ("group_id"),
  CONSTRAINT "warp_group_map_warp_id_fk"
  FOREIGN KEY ("warp_id")
  REFERENCES "warp" ("warp_id")
);
CREATE INDEX "warp_group_map.warp_group_map_group_id_idx" ON "warp_group_map" ("group_id");
CREATE INDEX "warp_group_map.warp_group_map_warp_id_idx" ON "warp_group_map" ("warp_id");

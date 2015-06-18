-- -----------------------------------------------------
-- Table "warp"
-- -----------------------------------------------------
CREATE TEMPORARY TABLE "warp_backup" (
  "warp_id",
  "name",
  "player_id",
  "x",
  "y",
  "z",
  "pitch",
  "yaw",
  "world_id",
  "creation_date",
  "type",
  "visits",
  "welcome_message"
);
INSERT INTO "warp_backup" SELECT *
                          FROM "warp";
DROP TABLE "warp";
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
  REFERENCES "player" ("player_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  CONSTRAINT "warp_world_id_fk"
  FOREIGN KEY ("world_id")
  REFERENCES "world" ("world_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
CREATE INDEX "warp.warp_player_id_idx" ON "warp" ("player_id");
CREATE INDEX "warp.warp_world_id_idx" ON "warp" ("world_id");
INSERT INTO "warp" SELECT *
                   FROM "warp_backup";
DROP TABLE "warp_backup";

-- -----------------------------------------------------
-- Table "warp_player_map"
-- -----------------------------------------------------
CREATE TEMPORARY TABLE "warp_player_map_backup" (
  "warp_id",
  "player_id"
);
INSERT INTO "warp_player_map_backup" SELECT *
                                     FROM "warp_player_map";
DROP TABLE "warp_player_map";
CREATE TABLE "warp_player_map" (
  "warp_id"   INTEGER NOT NULL CHECK ("warp_id" >= 0),
  "player_id" INTEGER NOT NULL CHECK ("player_id" >= 0),
  PRIMARY KEY ("warp_id", "player_id"),
  CONSTRAINT "warp_player_map_player_id_fk"
  FOREIGN KEY ("player_id")
  REFERENCES "player" ("player_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  CONSTRAINT "warp_player_map_warp_id_fk"
  FOREIGN KEY ("warp_id")
  REFERENCES "warp" ("warp_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
CREATE INDEX "warp_player_map.warp_player_map_player_id_idx" ON "warp_player_map" ("player_id");
CREATE INDEX "warp_player_map.warp_player_map_warp_id_idx" ON "warp_player_map" ("warp_id");
INSERT INTO "warp_player_map" SELECT *
                              FROM "warp_player_map_backup";
DROP TABLE "warp_player_map_backup";

-- -----------------------------------------------------
-- Table "warp_group_map"
-- -----------------------------------------------------
CREATE TEMPORARY TABLE "warp_group_map_backup" (
  "warp_id",
  "group_id"
);
INSERT INTO "warp_group_map_backup" SELECT *
                                    FROM "warp_group_map";
DROP TABLE "warp_group_map";
CREATE TABLE "warp_group_map" (
  "warp_id"  INTEGER NOT NULL CHECK ("warp_id" >= 0),
  "group_id" INTEGER NOT NULL CHECK ("group_id" >= 0),
  PRIMARY KEY ("warp_id", "group_id"),
  CONSTRAINT "warp_group_map_group_id_fk"
  FOREIGN KEY ("group_id")
  REFERENCES "group" ("group_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  CONSTRAINT "warp_group_map_warp_id_fk"
  FOREIGN KEY ("warp_id")
  REFERENCES "warp" ("warp_id")
  ON DELETE CASCADE
  ON UPDATE CASCADE
);
CREATE INDEX "warp_group_map.warp_group_map_group_id_idx" ON "warp_group_map" ("group_id");
CREATE INDEX "warp_group_map.warp_group_map_warp_id_idx" ON "warp_group_map" ("warp_id");
INSERT INTO "warp_group_map" SELECT *
                             FROM "warp_group_map_backup";
DROP TABLE "warp_group_map_backup";

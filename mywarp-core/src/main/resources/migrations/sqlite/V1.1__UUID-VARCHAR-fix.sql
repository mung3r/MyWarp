ALTER TABLE "player" RENAME TO "tmp_player";
CREATE TABLE "player"(
  "player_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK("player_id">=0),
  "uuid" VARCHAR(36) NOT NULL,
  CONSTRAINT "player_uuid_uq"
  UNIQUE("uuid")
);
INSERT INTO "player"("player_id", "uuid")
SELECT "player_id", "uuid"
FROM "tmp_player";
DROP TABLE "tmp_player";

ALTER TABLE "world" RENAME TO "tmp_world";
CREATE TABLE "world"(
  "world_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK("world_id">=0),
  "uuid" VARCHAR(36) NOT NULL,
  CONSTRAINT "world_uuid_uq"
  UNIQUE("uuid")
);
INSERT INTO "world"("world_id", "uuid")
SELECT "world_id", "uuid"
FROM "tmp_world";
DROP TABLE "tmp_world";


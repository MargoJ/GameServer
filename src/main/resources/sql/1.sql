-- 1. SCHEMA VERSION TABLE
CREATE TABLE `s_version` (
  `restrict` ENUM ('') NOT NULL DEFAULT '',
  `version`  INT       NOT NULL,

  PRIMARY KEY (`restrict`),
  UNIQUE (`restrict`)
);

-- 2. PLAYERS TABLE
CREATE TABLE `players` (
  -- BASE DATA
  `id`             BIGINT          NOT NULL,
  `accountId`      BIGINT          NOT NULL,
  `characterName`  VARCHAR(25)     NOT NULL,
  `profession`     CHAR            NOT NULL,
  `gender`         ENUM ('m', 'k') NOT NULL,
  `player_options` INT             NOT NULL   DEFAULT 0,
  
  -- XP
  `experience`     BIGINT          NOT NULL   DEFAULT 0,
  `level`          INT             NOT NULL   DEFAULT 1,

  -- LOCATION
  `map`            VARCHAR(127)    NULL       DEFAULT NULL,
  `x`              TINYINT         NULL       DEFAULT NULL,
  `y`              TINYINT         NULL       DEFAULT NULL,

  -- BASE STATS
  `baseStrength`   INT             NOT NULL   DEFAULT 4,
  `baseAgility`    INT             NOT NULL   DEFAULT 3,
  `baseIntellect`  INT             NOT NULL   DEFAULT 3,
  `statPoints`     INT             NOT NULL   DEFAULT 0,

  -- CURRENCY
  `gold`           BIGINT          NOT NULL   DEFAULT 0,

  -- STATUS
  `hp`             INT             NOT NULL   DEFAULT 1,
  `ttl`            INT             NOT NULL   DEFAULT 360,
  `dead_until`     DATETIME        NULL,

  -- CONSTRAINTS
  PRIMARY KEY (`id`),

  UNIQUE (`id`),
  UNIQUE (`characterName`)
);

-- 3. ITEMS TABLE
CREATE TABLE `items` (
  -- BASE DATA
  `id`         BIGINT       NOT NULL   AUTO_INCREMENT,
  `item_id`    VARCHAR(127) NOT NULL,

  -- ADDITONAL PROPERTIES
  `properties` BLOB         NULL,

  -- CONSTRAINTS
  PRIMARY KEY (`id`)
);

ALTER TABLE `items`
  AUTO_INCREMENT = 2000000;

-- 4. PLAYER INVENTORIES TABLE
CREATE TABLE `player_inventories` (
  -- BASE DATA
  `id`    BIGINT NOT NULL AUTO_INCREMENT,

  -- REFERENCES
  `index` INT    NOT NULL,
  `owner` BIGINT NOT NULL,
  `item`  BIGINT NOT NULL,

  -- CONSTRAINTS
  PRIMARY KEY (`id`),
  FOREIGN KEY (`owner`) REFERENCES players (`id`),
  FOREIGN KEY (`item`) REFERENCES items (`id`)
);

-- 5. MAP INVENTORIES TABLE
CREATE TABLE `map_inventories` (
  -- BASE DATA
  `id`    BIGINT       NOT NULL AUTO_INCREMENT,

  -- REFERENCES
  `index` INT          NOT NULL,
  `owner` VARCHAR(128) NOT NULL,
  `item`  BIGINT       NOT NULL,

  -- CONSTRAINTS
  PRIMARY KEY (`id`),
  FOREIGN KEY (`item`) REFERENCES items (`id`)
);

-- 6. SET VERSION
INSERT INTO `s_version` (`version`) VALUES (1);
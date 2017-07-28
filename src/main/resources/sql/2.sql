-- 1. MAP INVENTORIES TABLE
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

-- 2. SET VERSION
UPDATE `s_version`
SET `version` = 2;
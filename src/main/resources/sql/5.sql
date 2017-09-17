-- 1. ADD OPTIONS TO PLAYER TABLE
ALTER TABLE `players`
  ADD COLUMN `player_options` INT NOT NULL DEFAULT 0;

-- 2. SET VERSION
UPDATE `s_version`
SET `version` = 5;
DELIMITER //
CREATE PROCEDURE excuteSeckill(IN fadeSeckillId INT,IN fadeUserPhone VARCHAR (15),IN fadeKillTime TIMESTAMP ,OUT fadeResult INT)
  BEGIN
    DECLARE insertCount INT DEFAULT 0;
    START TRANSACTION ;
    INSERT IGNORE success_killed(seckill_id,user_phone,state,create_time) VALUES(fadeSeckillId,fadeUserPhone,0,fadeKillTime);
    SELECT ROW_COUNT() INTO insertCount;
    IF(insertCount = 0) THEN
      ROLLBACK ;
      SET fadeResult = -1;
    ELSEIF(insertCount < 0) THEN
      ROLLBACK ;
      SET fadeResult = -2;
    ELSE
      UPDATE seckill SET number = number -1 WHERE seckill_id = fadeSeckillId AND start_time < fadeKillTime AND end_time > fadeKillTime AND number > 0;
      SELECT ROW_COUNT() INTO insertCount;
      IF (insertCount = 0)  THEN
        ROLLBACK ;
        SET fadeResult = 0;
      ELSEIF (insertCount < 0) THEN
        ROLLBACK ;
        SET fadeResult = -2;
      ELSE
        COMMIT ;
        SET  fadeResult = 1;
      END IF;
    END IF;
  END;
//

DELIMITER ;

SET @fadeResult = -3;
CALL excuteSeckill(8,13813813822,NOW(),@fadeResult);
SELECT @fadeResult
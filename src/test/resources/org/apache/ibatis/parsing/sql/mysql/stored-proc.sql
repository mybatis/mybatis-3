--
--    Copyright 2009-2018 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- ========== ========== ========== ========== ========== ========== ==========

/*!50003 DROP PROCEDURE IF EXISTS `thread_query` */;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ALLOW_INVALID_DATES' */ ;

DELIMITER ||

CREATE DEFINER=`root`@`%` PROCEDURE `thread_query`(
  IN p_thread_id  BIGINT )
READS SQL DATA
  BEGIN
    SELECT DISTINCT
      thread_id,
      sql_text
        sql_text
    FROM
      DBA.saved_slow_log
    WHERE
      thread_id = p_thread_id AND
      DATE( start_time ) = DATE(  NOW() );
  END;
||

DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;

-- ========== ========== ========== ========== ========== ========== ==========

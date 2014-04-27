--
--    Copyright 2009-2012 the original author or authors.
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
CREATE PROCEDURE GET_PRODUCT(IN name VARCHAR(100))
  MODIFIES SQL DATA DYNAMIC RESULT SETS 1
  BEGIN ATOMIC
    DECLARE result CURSOR FOR SELECT * FROM product WHERE product.name = name;
    
    BEGIN ATOMIC
    	DECLARE result2 CURSOR FOR SELECT * FROM product WHERE product.name like concat(name, '%');
    	OPEN result2;
    END;
    
    OPEN result;
  END;
  
INSERT INTO product VALUES ('AV-XB-01','BIRDS','Amazon Parrot1','<image src="../images/bird2.gif">Great companion for up to 75 years');
INSERT INTO product VALUES ('AV-XB-02','BIRDS','Amazon Parrot2','<image src="../images/bird2.gif">Great companion for up to 75 years');
INSERT INTO product VALUES ('AV-XB-03','BIRDS','Amazon Parrot3','<image src="../images/bird2.gif">Great companion for up to 75 years');

CREATE PROCEDURE delete_product(IN productId_p VARCHAR(80)) 
 MODIFIES SQL DATA
 BEGIN ATOMIC
   DECLARE val_v    INTEGER;
   DECLARE prodId_v VARCHAR(10);
   
   SET val_v = 0;
  
   FOR SELECT productId FROM product WHERE product.productId like productId_p DO
     SET prodId_v = productId;
     
     DELETE FROM item WHERE item.productId = prodId_v;
     DELETE FROM product WHERE product.productId = prodId_v;
     SET val_v = val_v + 1;
   END FOR;
   
 END;
 
 
--CALL delete_product('AV-XB-%');



 

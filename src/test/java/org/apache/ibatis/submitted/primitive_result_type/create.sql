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

CREATE MEMORY TABLE PRODUCT(PRODUCTCODE NUMERIC,PRODUCTTYPE NUMERIC);
INSERT INTO PRODUCT(PRODUCTCODE,PRODUCTTYPE) VALUES(5,1);
INSERT INTO PRODUCT(PRODUCTCODE,PRODUCTTYPE) VALUES(6,2);
INSERT INTO PRODUCT(PRODUCTCODE,PRODUCTTYPE) VALUES(1000,NULL);
INSERT INTO PRODUCT(PRODUCTCODE,PRODUCTTYPE) VALUES(2000,NULL);

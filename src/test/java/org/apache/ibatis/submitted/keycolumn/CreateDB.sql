--
--    Copyright 2009-2019 the original author or authors.
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

DROP SCHEMA IF EXISTS mbtest CASCADE;

CREATE SCHEMA mbtest;

CREATE TABLE mbtest.test_identity
(
  first_name character varying(30),
  last_name character varying(30),
  name_id serial NOT NULL,
  CONSTRAINT test_identity_pkey PRIMARY KEY (name_id)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE mbtest.test_identity OWNER TO postgres;

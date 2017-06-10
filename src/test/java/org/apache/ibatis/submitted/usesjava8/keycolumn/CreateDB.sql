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

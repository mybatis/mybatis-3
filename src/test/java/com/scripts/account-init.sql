-- HSQL DATABASE

-- Dropping Tables

DROP TABLE ACCOUNT;

-- Creating Tables

CREATE TABLE ACCOUNT (
    ACC_ID             INT NOT NULL,
    ACC_FIRST_NAME     VARCHAR(255) NOT NULL,
    ACC_LAST_NAME      VARCHAR(255) NOT NULL,
    ACC_EMAIL          VARCHAR(255),
    ACC_AGE						 NUMERIC,
    ACC_BANNER_OPTION  VARCHAR(255),
    ACC_CART_OPTION    INT,
    ACC_DATE_ADDED     DATE,
    PRIMARY KEY (ACC_ID)
);

-- Creating Test Data

INSERT INTO ACCOUNT VALUES(1,'Clinton', 'Begin', 'clinton.begin@ibatis.com', 1, 'Aye', 200, CURRENT_DATE);
INSERT INTO ACCOUNT VALUES(2,'Jim', 'Smith', 'jim.smith@somewhere.com', 2, 'Aye', 200, CURRENT_DATE);
INSERT INTO ACCOUNT VALUES(3,'Elizabeth', 'Jones', null, 3, 'Nay', 100, CURRENT_DATE);
INSERT INTO ACCOUNT VALUES(4,'Bob', 'Jackson', 'bob.jackson@somewhere.com', 4, 'Nay', 100, CURRENT_DATE);
INSERT INTO ACCOUNT VALUES(5,'&manda', 'Goodman', null, 5, 'Aye', 100, CURRENT_DATE);



create table person (
  id int,
  firstName varchar(100),
  lastName varchar(100),
  jobTitle varchar(100),
  department varchar(100),
  personType varchar(100) DEFAULT NULL,
  employeeType varchar(100) DEFAULT NULL
);

INSERT INTO person (id, firstName, lastName, jobTitle, department, personType, employeeType)
VALUES (1, 'John', 'Smith', 'IT director', 'IT', 'EmployeeType', 'DirectorType');

INSERT INTO person (id, firstName, lastName, jobTitle, department, personType, employeeType)
VALUES (3, 'John', 'Smith', 'IT director', 'IT', 'EmployeeType', 'PersonType');

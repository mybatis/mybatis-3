drop table table1 if exists;
create table table1 (

  id int not null,
  name varchar(80) not null,
  age int not null,
  constraint pk_table1 primary key (id)

);
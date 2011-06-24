create table common (
id int,
name varchar(20)
);

create table hsql (
id int,
name varchar(20)
);

insert into common (id, name) values(1, 'common');

insert into hsql (id, name) values(1, 'hsql');

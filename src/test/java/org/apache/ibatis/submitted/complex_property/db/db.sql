create table user (
id int,
username varchar(32) not null,
password varchar(128) not null,
administrator int,
primary key (id)
);

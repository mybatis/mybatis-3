drop table blobtest.blobs is exists;
drop schema blobtest if exists;

create schema blobtest;

create table blobtest.blobs (
  id int not null,
  blob longvarbinary,
  primary key (id)
);

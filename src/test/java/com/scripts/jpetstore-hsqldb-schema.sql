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

drop table lineitem;
drop table orderstatus;
drop table orders;
drop table bannerdata;
drop table profile;
drop table signon;
drop table inventory;
drop table item;
drop table product;
drop table account;
drop table category;
drop table supplier;
drop table sequence;

create table supplier (
    suppid int not null,
    name VARCHAR(80),
    status VARCHAR(2) not null,
    addr1 VARCHAR(80),
    addr2 VARCHAR(80),
    city VARCHAR(80),
    state VARCHAR(80),
    zip VARCHAR(5),
    phone VARCHAR(80),
    constraint pk_supplier primary key (suppid)
);

create table signon (
    username VARCHAR(25) not null,
    password VARCHAR(25)  not null,
    constraint pk_signon primary key (username)
);

create table account (
    userid VARCHAR(80) not null,
    email VARCHAR(80) not null,
    firstname VARCHAR(80) not null,
    lastname VARCHAR(80) not null,
    status VARCHAR(2),
    addr1 VARCHAR(80) not null,
    addr2 VARCHAR(40),
    city VARCHAR(80) not  null,
    state VARCHAR(80) not null,
    zip VARCHAR(20) not null,
    country VARCHAR(20) not null,
    phone VARCHAR(80) not null,
    constraint pk_account primary key (userid)
);

create table profile (
    userid VARCHAR(80) not null,
    langpref VARCHAR(80) not null,
    favcategory VARCHAR(30),
    mylistopt int,
    banneropt int,
    constraint pk_profile primary key (userid)
);

create table bannerdata (
    favcategory VARCHAR(80) not null,
    bannername VARCHAR(255),
    constraint pk_bannerdata primary key (favcategory)
);

create table orders (
      orderid int not null,
      userid VARCHAR(80) not null,
      orderdate date not null,
      shipaddr1 VARCHAR(80) not null,
      shipaddr2 VARCHAR(80),
      shipcity VARCHAR(80) not null,
      shipstate VARCHAR(80) not null,
      shipzip VARCHAR(20) not null,
      shipcountry VARCHAR(20) not null,
      billaddr1 VARCHAR(80) not null,
      billaddr2 VARCHAR(80),
      billcity VARCHAR(80) not null,
      billstate VARCHAR(80) not null,
      billzip VARCHAR(20) not null,
      billcountry VARCHAR(20) not null,
      courier VARCHAR(80) not null,
      totalprice decimal(10,2) not null,
      billtofirstname VARCHAR(80) not null,
      billtolastname VARCHAR(80) not null,
      shiptofirstname VARCHAR(80) not null,
      shiptolastname VARCHAR(80) not null,
      creditcard VARCHAR(80) not null,
      exprdate VARCHAR(7) not null,
      cardtype VARCHAR(80) not null,
      locale VARCHAR(80) not null,
      constraint pk_orders primary key (orderid)
);

create table orderstatus (
      orderid int not null,
      linenum int not null,
      timestamp date not null,
      status VARCHAR(2) not null,
      constraint pk_orderstatus primary key (orderid, linenum)
);

create table lineitem (
      orderid int not null,
      linenum int not null,
      itemid VARCHAR(10) not null,
      quantity int not null,
      unitprice decimal(10,2) not null,
      constraint pk_lineitem primary key (orderid, linenum)
);

create table category (
	catid VARCHAR(10) not null,
	name VARCHAR(80),
	descn VARCHAR(255),
	constraint pk_category primary key (catid)
);

create table product (
    productid VARCHAR(10) not null,
    category VARCHAR(10) not null,
    name VARCHAR(80),
    descn VARCHAR(255),
    constraint pk_product primary key (productid),
        constraint fk_product_1 foreign key (category)
        references category (catid)
);

create table item (
    itemid VARCHAR(10) not null,
    productid VARCHAR(10) not null,
    listprice decimal(10,2),
    unitcost decimal(10,2),
    supplier int,
    status VARCHAR(2),
    attr1 VARCHAR(80),
    attr2 VARCHAR(80),
    attr3 VARCHAR(80),
    attr4 VARCHAR(80),
    attr5 VARCHAR(80),
    constraint pk_item primary key (itemid),
        constraint fk_item_1 foreign key (productid)
        references product (productid),
        constraint fk_item_2 foreign key (supplier)
        references supplier (suppid)
);

create table inventory (
    itemid VARCHAR(10) not null,
    qty int not null,
    constraint pk_inventory primary key (itemid)
);

CREATE TABLE sequence
(
    name               VARCHAR(30)  not null,
    nextid             int          not null,
    constraint pk_sequence primary key (name)
);

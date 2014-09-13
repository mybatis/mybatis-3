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

drop index productCat if exists;
drop index productName if exists;
drop index itemProd if exists;

drop table lineitem if exists;
drop table orderstatus if exists;
drop table orders if exists;
drop table bannerdata if exists;
drop table profile if exists;
drop table signon if exists;
drop table inventory if exists;
drop table item if exists;
drop table product if exists;
drop table account if exists;
drop table category if exists;
drop table supplier if exists;
drop table sequence if exists;

create table supplier (
suppid int not null,
name varchar(80) null,
status varchar(2) not null,
addr1 varchar(80) null,
addr2 varchar(80) null,
city varchar(80) null,
state varchar(80) null,
zip varchar(5) null,
phone varchar(80) null,
constraint pk_supplier primary key (suppid)
);

create table signon (
username varchar(25) not null,
password varchar(25)  not null,
constraint pk_signon primary key (username)
);

create table account (
userid varchar(80) not null,
email varchar(80) not null,
firstname varchar(80) not null,
lastname varchar(80) not null,
status varchar(2)  null,
addr1 varchar(80) not null,
addr2 varchar(40) null,
city varchar(80) not  null,
state varchar(80) not null,
zip varchar(20) not null,
country varchar(20) not null,
phone varchar(80) not null,
constraint pk_account primary key (userid)
);

create table profile (
userid varchar(80) not null,
langpref varchar(80) not null,
favcategory varchar(30),
mylistopt int,
banneropt int,
constraint pk_profile primary key (userid)
);

create table bannerdata (
favcategory varchar(80) not null,
bannername varchar(255)  null,
constraint pk_bannerdata primary key (favcategory)
);

create table orders (
orderid int not null,
userid varchar(80) not null,
orderdate date not null,
shipaddr1 varchar(80) not null,
shipaddr2 varchar(80) null,
shipcity varchar(80) not null,
shipstate varchar(80) not null,
shipzip varchar(20) not null,
shipcountry varchar(20) not null,
billaddr1 varchar(80) not null,
billaddr2 varchar(80)  null,
billcity varchar(80) not null,
billstate varchar(80) not null,
billzip varchar(20) not null,
billcountry varchar(20) not null,
courier varchar(80) not null,
totalprice decimal(10,2) not null,
billtofirstname varchar(80) not null,
billtolastname varchar(80) not null,
shiptofirstname varchar(80) not null,
shiptolastname varchar(80) not null,
creditcard varchar(80) not null,
exprdate varchar(7) not null,
cardtype varchar(80) not null,
locale varchar(80) not null,
constraint pk_orders primary key (orderid)
);

create table orderstatus (
orderid int not null,
linenum int not null,
timestamp date not null,
status varchar(2) not null,
constraint pk_orderstatus primary key (orderid, linenum)
);

create table lineitem (
orderid int not null,
linenum int not null,
itemid varchar(10) not null,
quantity int not null,
unitprice decimal(10,2) not null,
constraint pk_lineitem primary key (orderid, linenum)
);

create table category (
catid varchar(10) not null,
name varchar(80) null,
descn varchar(255) null,
constraint pk_category primary key (catid)
);

create table product (
productid varchar(10) not null,
category varchar(10) not null,
name varchar(80) null,
descn varchar(255) null,
constraint pk_product primary key (productid),
constraint fk_product_1 foreign key (category)
references category (catid)
);

create index productCat on product (category);
create index productName on product (name);

create table item (
itemid varchar(10) not null,
productid varchar(10) not null,
listprice decimal(10,2) null,
unitcost decimal(10,2) null,
supplier int null,
status varchar(2) null,
attr1 varchar(80) null,
attr2 varchar(80) null,
attr3 varchar(80) null,
attr4 varchar(80) null,
attr5 varchar(80) null,
constraint pk_item primary key (itemid),
constraint fk_item_1 foreign key (productid)
references product (productid),
constraint fk_item_2 foreign key (supplier)
references supplier (suppid)
);

create index itemProd on item (productid);

create table inventory (
itemid varchar(10) not null,
qty int not null,
constraint pk_inventory primary key (itemid)
);

CREATE TABLE sequence
(
name               varchar(30)  not null,
nextid             int          not null,
constraint pk_sequence primary key (name)
);


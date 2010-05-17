drop table product;

create table product (
productid varchar(10) not null,
category varchar(10) not null,
name varchar(80) null,
descn varchar(255) null,
constraint pk_product primary key (productid),
constraint fk_product_1 foreign key (category)
references category (catid)
)


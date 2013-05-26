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

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[fk_orders_1]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[orders] DROP CONSTRAINT fk_orders_1
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[fk_product_1]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[product] DROP CONSTRAINT fk_product_1
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[fk_lineitem_1]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[lineitem] DROP CONSTRAINT fk_lineitem_1
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[fk_orderstatus_1]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[orderstatus] DROP CONSTRAINT fk_orderstatus_1
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[fk_item_1]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[item] DROP CONSTRAINT fk_item_1
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[fk_item_2]') and OBJECTPROPERTY(id, N'IsForeignKey') = 1)
ALTER TABLE [dbo].[item] DROP CONSTRAINT fk_item_2
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[account]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[account]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[bannerdata]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[bannerdata]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[category]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[category]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[inventory]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[inventory]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[item]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[item]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[lineitem]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[lineitem]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[orders]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[orders]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[orderstatus]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[orderstatus]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[product]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[product]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[profile]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[profile]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[sequence]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[sequence]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[signon]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[signon]
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[supplier]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[supplier]
GO

CREATE TABLE [dbo].[account] (
	[userid] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[email] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[firstname] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[lastname] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[status] [varchar] (2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[addr1] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[addr2] [varchar] (40) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[city] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[state] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[zip] [varchar] (20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[country] [varchar] (20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[phone] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[bannerdata] (
	[favcategory] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[bannername] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[category] (
	[catid] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[name] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[descn] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[inventory] (
	[itemid] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[qty] [int] NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[item] (
	[itemid] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[productid] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[listprice] [decimal](10, 2) NULL ,
	[unitcost] [decimal](10, 2) NULL ,
	[supplier] [int] NULL ,
	[status] [varchar] (2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[attr1] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[attr2] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[attr3] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[attr4] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[attr5] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[lineitem] (
	[orderid] [int] NOT NULL ,
	[linenum] [int] NOT NULL ,
	[itemid] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[quantity] [int] NOT NULL ,
	[unitprice] [numeric](10, 2) NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[orders] (
	[orderid] [int] NOT NULL ,
	[userid] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[orderdate] [datetime] NOT NULL ,
	[shipaddr1] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[shipaddr2] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[shipcity] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[shipstate] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[shipzip] [varchar] (20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[shipcountry] [varchar] (20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[billaddr1] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[billaddr2] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[billcity] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[billstate] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[billzip] [varchar] (20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[billcountry] [varchar] (20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[courier] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[totalprice] [numeric](10, 2) NOT NULL ,
	[billtofirstname] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[billtolastname] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[shiptofirstname] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[shiptolastname] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[creditcard] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[exprdate] [varchar] (7) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[cardtype] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[locale] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[orderstatus] (
	[orderid] [int] NOT NULL ,
	[linenum] [int] NOT NULL ,
	[timestamp] [datetime] NOT NULL ,
	[status] [varchar] (2) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[product] (
	[productid] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[category] [varchar] (10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[name] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[descn] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[profile] (
	[userid] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[langpref] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[favcategory] [varchar] (30) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[mylistopt] [int] NULL ,
	[banneropt] [int] NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[sequence] (
	[name] [varchar] (30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[nextid] [int] NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[signon] (
	[username] [varchar] (25) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[password] [varchar] (25) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL 
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[supplier] (
	[suppid] [int] NOT NULL ,
	[name] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[status] [varchar] (2) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[addr1] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[addr2] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[city] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[state] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[zip] [varchar] (5) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[phone] [varchar] (80) COLLATE SQL_Latin1_General_CP1_CI_AS NULL 
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[account] WITH NOCHECK ADD 
	CONSTRAINT [pk_account] PRIMARY KEY  CLUSTERED 
	(
		[userid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[bannerdata] WITH NOCHECK ADD 
	CONSTRAINT [pk_bannerdata] PRIMARY KEY  CLUSTERED 
	(
		[favcategory]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[category] WITH NOCHECK ADD 
	CONSTRAINT [pk_category] PRIMARY KEY  CLUSTERED 
	(
		[catid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[inventory] WITH NOCHECK ADD 
	CONSTRAINT [pk_inventory] PRIMARY KEY  CLUSTERED 
	(
		[itemid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[item] WITH NOCHECK ADD 
	CONSTRAINT [pk_item] PRIMARY KEY  CLUSTERED 
	(
		[itemid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[lineitem] WITH NOCHECK ADD 
	CONSTRAINT [pk_lineitem] PRIMARY KEY  CLUSTERED 
	(
		[orderid],
		[linenum]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[orders] WITH NOCHECK ADD 
	CONSTRAINT [pk_orders] PRIMARY KEY  CLUSTERED 
	(
		[orderid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[orderstatus] WITH NOCHECK ADD 
	CONSTRAINT [pk_orderstatus] PRIMARY KEY  CLUSTERED 
	(
		[orderid],
		[linenum]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[product] WITH NOCHECK ADD 
	CONSTRAINT [pk_product] PRIMARY KEY  CLUSTERED 
	(
		[productid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[profile] WITH NOCHECK ADD 
	CONSTRAINT [pk_profile] PRIMARY KEY  CLUSTERED 
	(
		[userid]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[sequence] WITH NOCHECK ADD 
	CONSTRAINT [pk_sequence] PRIMARY KEY  CLUSTERED 
	(
		[name]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[signon] WITH NOCHECK ADD 
	CONSTRAINT [pk_signon] PRIMARY KEY  CLUSTERED 
	(
		[username]
	)  ON [PRIMARY] 
GO

ALTER TABLE [dbo].[supplier] WITH NOCHECK ADD 
	CONSTRAINT [pk_supplier] PRIMARY KEY  CLUSTERED 
	(
		[suppid]
	)  ON [PRIMARY] 
GO

 CREATE  INDEX [itemProd] ON [dbo].[item]([productid]) ON [PRIMARY]
GO

 CREATE  INDEX [productCat] ON [dbo].[product]([category]) ON [PRIMARY]
GO

 CREATE  INDEX [productName] ON [dbo].[product]([name]) ON [PRIMARY]
GO

ALTER TABLE [dbo].[item] ADD 
	CONSTRAINT [fk_item_1] FOREIGN KEY 
	(
		[productid]
	) REFERENCES [dbo].[product] (
		[productid]
	),
	CONSTRAINT [fk_item_2] FOREIGN KEY 
	(
		[supplier]
	) REFERENCES [dbo].[supplier] (
		[suppid]
	)
GO

ALTER TABLE [dbo].[lineitem] ADD 
	CONSTRAINT [fk_lineitem_1] FOREIGN KEY 
	(
		[orderid]
	) REFERENCES [dbo].[orders] (
		[orderid]
	)
GO

ALTER TABLE [dbo].[orders] ADD 
	CONSTRAINT [fk_orders_1] FOREIGN KEY 
	(
		[userid]
	) REFERENCES [dbo].[account] (
		[userid]
	)
GO

ALTER TABLE [dbo].[orderstatus] ADD 
	CONSTRAINT [fk_orderstatus_1] FOREIGN KEY 
	(
		[orderid]
	) REFERENCES [dbo].[orders] (
		[orderid]
	)
GO

ALTER TABLE [dbo].[product] ADD 
	CONSTRAINT [fk_product_1] FOREIGN KEY 
	(
		[category]
	) REFERENCES [dbo].[category] (
		[catid]
	)
GO


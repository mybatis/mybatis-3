/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ibatis.jpetstore.domain;

import java.math.BigDecimal;
import java.util.Date;

public class DomainFixture {

  public static Account newTestAccount() {
    Account account = new Account();

    account.setUsername("cbegin");
    account.setPassword("PASSWORD");

    account.setFirstName("Clinton");
    account.setLastName("Begin");

    account.setAddress1("123 Some Street");
    account.setAddress2("Apt B");
    account.setCity("Calgary");
    account.setState("AB");
    account.setCountry("Canada");
    account.setZip("90210");

    account.setEmail("someone@somewhere.com");
    account.setPhone("403.555.5555");

    account.setLanguagePreference("ENGLISH");
    account.setBannerName("DOGS");
    account.setBannerOption(true);
    account.setFavouriteCategoryId("DOGS");
    account.setListOption(true);
    account.setStatus("ACTIVE");

    return account;
  }

  public static Order newTestOrder() {
    Item item = new Item();
    item.setItemId("EST-2");

    LineItem lineItem = new LineItem();
    lineItem.setQuantity(100001);
    lineItem.setItem(item);
    lineItem.setItemId(item.getItemId());
    lineItem.setUnitPrice(new BigDecimal("99.99"));

    Order order = new Order();
    order.addLineItem(lineItem);

    order.setBillAddress1("123 Some Street");
    order.setBillAddress2("Apt B");
    order.setBillCity("Calgary");
    order.setBillCountry("Canada");
    order.setBillState("AB");
    order.setBillToFirstName("Clinton");
    order.setBillToLastName("Begin");
    order.setBillZip("12345");

    order.setShipAddress1("123 Some Street");
    order.setShipAddress2("Apt B");
    order.setShipCity("Calgary");
    order.setShipCountry("Canada");
    order.setShipState("AB");
    order.setShipToFirstName("Clinton");
    order.setShipToLastName("Begin");
    order.setShipZip("12345");

    order.setCardType("VISA");
    order.setCreditCard("1234-1123-1123");
    order.setExpiryDate("11/02");
    order.setLocale("CA");
    order.setCourier("B");
    order.setOrderDate(new Date());
    order.setStatus("A");
    order.setUsername("j2ee");
    order.setTotalPrice(new BigDecimal("99.99"));

    return order;
  }

}

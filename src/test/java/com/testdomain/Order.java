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
package com.testdomain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

  private int id;
  private Account account;
  private Date date;
  private String cardType;
  private String cardNumber;
  private String cardExpiry;
  private String street;
  private String city;
  private String province;
  private String postalCode;
  private Collection lineItems;
  private LineItem[] lineItemArray;
  private LineItem favouriteLineItem;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getCardType() {
    return cardType;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public String getCardExpiry() {
    return cardExpiry;
  }

  public void setCardExpiry(String cardExpiry) {
    this.cardExpiry = cardExpiry;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public List getLineItemsList() {
    return (List) lineItems;
  }

  public void setLineItemsList(List lineItems) {
    this.lineItems = lineItems;
  }

  public Collection getLineItems() {
    return lineItems;
  }

  public void setLineItems(Collection lineItems) {
    this.lineItems = lineItems;
  }

  public LineItem getFavouriteLineItem() {
    return favouriteLineItem;
  }

  public void setFavouriteLineItem(LineItem favouriteLineItem) {
    this.favouriteLineItem = favouriteLineItem;
  }

  public LineItem[] getLineItemArray() {
    return lineItemArray;
  }

  public void setLineItemArray(LineItem[] lineItemArray) {
    this.lineItemArray = lineItemArray;
  }

}

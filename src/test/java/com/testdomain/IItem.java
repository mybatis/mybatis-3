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

import java.math.BigDecimal;

/*
 * Used in testing the ResultObjectFactory
 *
 * @author Jeff Butler
 */
public interface IItem {
  String getAttribute1();

  void setAttribute1(String attribute1);

  String getAttribute2();

  void setAttribute2(String attribute2);

  String getAttribute3();

  void setAttribute3(String attribute3);

  String getAttribute4();

  void setAttribute4(String attribute4);

  String getAttribute5();

  void setAttribute5(String attribute5);

  String getItemId();

  void setItemId(String itemId);

  BigDecimal getListPrice();

  void setListPrice(BigDecimal listPrice);

  String getProductId();

  void setProductId(String productId);

  String getStatus();

  void setStatus(String status);

  ISupplier getSupplier();

  void setSupplier(ISupplier supplier);

  BigDecimal getUnitCost();

  void setUnitCost(BigDecimal unitCost);
}

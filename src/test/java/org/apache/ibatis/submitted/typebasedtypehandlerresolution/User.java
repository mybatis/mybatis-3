/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.typebasedtypehandlerresolution;

import java.time.LocalDate;
import java.util.List;

public class User {

  private Integer id;
  private FuzzyBean<String> strvalue;
  private FuzzyBean<Integer> intvalue;
  private LocalDate datevalue;
  private LocalDate datevalue2;
  private List<String> strings;
  private List<Integer> integers;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public FuzzyBean<String> getStrvalue() {
    return strvalue;
  }

  public void setStrvalue(FuzzyBean<String> strvalue) {
    this.strvalue = strvalue;
  }

  public FuzzyBean<Integer> getIntvalue() {
    return intvalue;
  }

  public void setIntvalue(FuzzyBean<Integer> intvalue) {
    this.intvalue = intvalue;
  }

  public LocalDate getDatevalue() {
    return datevalue;
  }

  public void setDatevalue(LocalDate datevalue) {
    this.datevalue = datevalue;
  }

  public LocalDate getDatevalue2() {
    return datevalue2;
  }

  public void setDatevalue2(LocalDate datevalue2) {
    this.datevalue2 = datevalue2;
  }

  public List<String> getStrings() {
    return strings;
  }

  public void setStrings(List<String> strings) {
    this.strings = strings;
  }

  public List<Integer> getIntegers() {
    return integers;
  }

  public void setIntegers(List<Integer> integers) {
    this.integers = integers;
  }
}

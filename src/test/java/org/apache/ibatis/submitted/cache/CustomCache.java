/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.cache;

import java.util.Date;

import org.apache.ibatis.cache.impl.PerpetualCache;

public class CustomCache extends PerpetualCache {

  private String stringValue;
  private Integer integerValue;
  private int intValue;
  private Long longWrapperValue;
  private long longValue;
  private Short shortWrapperValue;
  private short shortValue;
  private Float floatWrapperValue;
  private float floatValue;
  private Double doubleWrapperValue;
  private double doubleValue;
  private Byte byteWrapperValue;
  private byte byteValue;
  private Boolean booleanWrapperValue;
  private boolean booleanValue;
  private Date date;

  public CustomCache(String id) {
    super(id);
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  public void setIntegerValue(Integer integerValue) {
    this.integerValue = integerValue;
  }

  public long getLongValue() {
    return longValue;
  }

  public void setLongValue(long longValue) {
    this.longValue = longValue;
  }

  public int getIntValue() {
    return intValue;
  }

  public void setIntValue(int intValue) {
    this.intValue = intValue;
  }

  public Long getLongWrapperValue() {
    return longWrapperValue;
  }

  public void setLongWrapperValue(Long longWrapperValue) {
    this.longWrapperValue = longWrapperValue;
  }

  public Short getShortWrapperValue() {
    return shortWrapperValue;
  }

  public void setShortWrapperValue(Short shortWrapperValue) {
    this.shortWrapperValue = shortWrapperValue;
  }

  public short getShortValue() {
    return shortValue;
  }

  public void setShortValue(short shortValue) {
    this.shortValue = shortValue;
  }

  public Float getFloatWrapperValue() {
    return floatWrapperValue;
  }

  public void setFloatWrapperValue(Float floatWrapperValue) {
    this.floatWrapperValue = floatWrapperValue;
  }

  public float getFloatValue() {
    return floatValue;
  }

  public void setFloatValue(float floatValue) {
    this.floatValue = floatValue;
  }

  public Double getDoubleWrapperValue() {
    return doubleWrapperValue;
  }

  public void setDoubleWrapperValue(Double doubleWrapperValue) {
    this.doubleWrapperValue = doubleWrapperValue;
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public Byte getByteWrapperValue() {
    return byteWrapperValue;
  }

  public void setByteWrapperValue(Byte byteWrapperValue) {
    this.byteWrapperValue = byteWrapperValue;
  }

  public byte getByteValue() {
    return byteValue;
  }

  public void setByteValue(byte byteValue) {
    this.byteValue = byteValue;
  }

  public Boolean getBooleanWrapperValue() {
    return booleanWrapperValue;
  }

  public void setBooleanWrapperValue(Boolean booleanWrapperValue) {
    this.booleanWrapperValue = booleanWrapperValue;
  }

  public boolean isBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

}
